package br.com.fivecom.litoralfm.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.fivecom.litoralfm.R;

public class VisibleDevicesAdapter extends RecyclerView.Adapter<VisibleDevicesAdapter.ViewHolder> {

    private final List<BluetoothDevice> devices;
    private final Context context;
    private final VisibleDevicesFragment fragment;

    public VisibleDevicesAdapter(VisibleDevicesFragment fragment, Context context, List<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
        this.fragment = fragment;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        Button btnPair;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.device_name);
            btnPair = itemView.findViewById(R.id.bt_pair);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visible_device, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        String deviceName = device.getName() != null ? device.getName() : "Dispositivo desconhecido";
        holder.tvDeviceName.setText(deviceName + "\n" + device.getAddress());

        holder.btnPair.setOnClickListener(v -> {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Iniciando pareamento e conexão A2DP com " + device.getName(), Toast.LENGTH_SHORT).show()
            );
            BluetoothConnectionManager.getInstance().pairAndConnectA2dp(context, device, new BluetoothConnectionManager.PairingAndConnectionCallback() {
                @Override
                public void onPairingInitiated(BluetoothDevice device) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Pareamento iniciado com " + device.getName(), Toast.LENGTH_SHORT).show()
                    );
                }
                @Override
                public void onPairingFailed(BluetoothDevice device, Exception e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Falha no pareamento com " + device.getName(), Toast.LENGTH_SHORT).show()
                    );
                }
                @Override
                public void onConnected(BluetoothDevice device) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Conectado via A2DP a " + device.getName(), Toast.LENGTH_SHORT).show()
                    );
                }
                @Override
                public void onConnectionFailed(BluetoothDevice device, Exception e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Falha na conexão A2DP com " + device.getName(), Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });


    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
