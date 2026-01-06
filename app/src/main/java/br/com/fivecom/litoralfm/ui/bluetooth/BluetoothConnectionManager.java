package br.com.fivecom.litoralfm.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothConnectionManager {

    private static BluetoothConnectionManager instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket currentSocket;
    private BluetoothDevice currentDevice;
    private final Object connectionLock = new Object();
    private final String mA2dpConnect = "connect";

    private BluetoothConnectionManager() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothConnectionManager getInstance() {
        if (instance == null) {
            instance = new BluetoothConnectionManager();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void connect(BluetoothDevice device, ConnectionCallback callback) {
        new Thread(() -> {
            try {
                Log.d("BTConnection", "Tentando conectar com: " + device.getAddress());
                BluetoothSocket socket;
                try {
                    Log.d("BTConnection", "Tentando criar socket inseguro para " + device.getAddress());
                    socket = device.createInsecureRfcommSocketToServiceRecord(
                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (Exception ex) {
                    Log.e("BTConnection", "Falha ao criar socket inseguro, tentando fallback", ex);
                    socket = createBluetoothSocket(device);
                }
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                synchronized (connectionLock) {
                    currentSocket = socket;
                    currentDevice = device;
                }
                Log.d("BTConnection", "Conexão estabelecida com: " + device.getAddress());
                if (callback != null) {
                    callback.onConnected(device);
                }
            } catch (IOException e) {
                Log.e("BTConnection", "Erro ao conectar com: " + device.getAddress(), e);
                if (callback != null) {
                    callback.onConnectionFailed(device, e);
                }
            }
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void connectionA2dp(Context context, String option, BluetoothDevice device) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    Log.d("A2DP", "Perfil A2DP conectado para " + device.getAddress());
                    BluetoothA2dp a2dp = (BluetoothA2dp) proxy;
                    try {
                        Method method = BluetoothA2dp.class.getDeclaredMethod(option, BluetoothDevice.class);
                        method.setAccessible(true);
                        method.invoke(a2dp, device);
                        Log.d("A2DP", "Método A2DP " + option + " invocado para: " + device.getAddress());
                    } catch (Exception e) {
                        Log.e("A2DP", "Erro ao invocar método A2DP " + option + " para: " + device.getAddress(), e);
                        Toast.makeText(context, "Não foi possível realizar a operação A2DP!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onServiceDisconnected(int profile) {
                Log.d("A2DP", "Serviço A2DP desconectado");
            }
        };
        adapter.getProfileProxy(context, listener, BluetoothProfile.A2DP);
    }

    public void connectA2dp(Context context, BluetoothDevice device) {
        connectionA2dp(context, mA2dpConnect, device);
    }

    @SuppressLint("MissingPermission")
    public void pairAndConnectA2dp(Context context, BluetoothDevice device, PairingAndConnectionCallback callback) {
        pairDevice(device, new PairingCallback() {
            @Override
            public void onPairingInitiated(BluetoothDevice device) {
                callback.onPairingInitiated(device);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d("BTConnection", "Pareamento concluído com: " + device.getAddress());
                        connectA2dp(context, device);
                        callback.onConnected(device);
                    } else {
                        callback.onPairingFailed(device, new Exception("Dispositivo não emparelhado após o tempo esperado."));
                    }
                }, 5000);
            }

            @Override
            public void onPairingFailed(BluetoothDevice device, Exception e) {
                callback.onPairingFailed(device, e);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void unpairAndDisconnectA2dp(Context context, BluetoothDevice device, UnpairCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Method removeBondMethod = device.getClass().getMethod("removeBond");
                boolean success = (boolean) removeBondMethod.invoke(device);
                if (success) {
                    Log.d("BTConnection", "Bond removido para: " + device.getAddress());
                    callback.onUnpaired(device);
                } else {
                    callback.onUnpairingFailed(device, new Exception("Falha ao remover o bond"));
                }
            } catch (Exception e) {
                callback.onUnpairingFailed(device, e);
            }
        }, 1000);
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            return (BluetoothSocket) m.invoke(device, 1);
        } catch (Exception e) {
            Log.e("BTConnection", "Falha ao criar socket por fallback", e);
        }
        return device.createRfcommSocketToServiceRecord(
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    }

    @SuppressLint("MissingPermission")
    public void pairDevice(BluetoothDevice device, PairingCallback callback) {
        try {
            Log.d("BTConnection", "Tentando parear com: " + device.getAddress());
            Method method = device.getClass().getMethod("createBond");
            boolean initiated = (boolean) method.invoke(device);
            if (initiated) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onPairingInitiated(device)
                );
            } else {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onPairingFailed(device, new Exception("Falha ao iniciar o pareamento"))
                );
            }
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() ->
                    callback.onPairingFailed(device, e)
            );
        }
    }

    public interface ConnectionCallback {
        void onConnected(BluetoothDevice device);
        void onConnectionFailed(BluetoothDevice device, Exception e);
    }

    public interface PairingCallback {
        void onPairingInitiated(BluetoothDevice device);
        void onPairingFailed(BluetoothDevice device, Exception e);
    }

    public interface PairingAndConnectionCallback extends PairingCallback {
        void onConnected(BluetoothDevice device);
        void onConnectionFailed(BluetoothDevice device, Exception e);
    }

    public interface UnpairCallback {
        void onUnpaired(BluetoothDevice device);
        void onUnpairingFailed(BluetoothDevice device, Exception e);
    }
}
