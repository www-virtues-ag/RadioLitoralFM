package br.com.fivecom.litoralfm.ui.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import br.com.fivecom.litoralfm.R;

public class VisibleDevicesFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> visibleDevicesList;
    private VisibleDevicesAdapter visibleAdapter;
    public SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressRecycler;
    private RecyclerView recyclerViewVisibleDevices;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    boolean alreadyAdded = false;
                    for (BluetoothDevice d : visibleDevicesList) {
                        if (d.getAddress().equals(device.getAddress())) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        visibleDevicesList.add(device);
                        visibleAdapter.notifyDataSetChanged();
                    }
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    };
    private final BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                int previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BondStateReceiver", "Bond state changed for " + device.getAddress()
                        + " from " + previousState + " to " + state);

                if (state == BluetoothDevice.BOND_BONDED && device != null) {
                    BluetoothConnectionManager.getInstance().connect(device, new BluetoothConnectionManager.ConnectionCallback() {
                        @Override
                        public void onConnected(BluetoothDevice device) {
                            Log.d("BondStateReceiver", "Conectado automaticamente a: " + device.getAddress());
                        }

                        @Override
                        public void onConnectionFailed(BluetoothDevice device, Exception e) {
                            Log.e("BondStateReceiver", "Falha ao conectar automaticamente: " + device.getAddress(), e);
                        }
                    });
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        visibleDevicesList = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        recyclerViewVisibleDevices = view.findViewById(R.id.recyclerView);
        progressRecycler = view.findViewById(R.id.progress_recycler);
        recyclerViewVisibleDevices.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerViewVisibleDevices.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        recyclerViewVisibleDevices.setNestedScrollingEnabled(true);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerViewVisibleDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        visibleAdapter = new VisibleDevicesAdapter(this, requireContext(), visibleDevicesList);
        recyclerViewVisibleDevices.setAdapter(visibleAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::startBluetoothDiscovery);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireContext().registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        requireContext().registerReceiver(bondStateReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        showProgress(true);
        startBluetoothDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(bluetoothReceiver);
        requireContext().unregisterReceiver(bondStateReceiver);
    }

    @SuppressLint("MissingPermission")
    public void forceRefreshVisibleDevices() {
        swipeRefreshLayout.setRefreshing(true);
        showProgress(true);
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        startBluetoothDiscovery();
    }

    private void showProgress(boolean show) {
        if (progressRecycler != null) {
            progressRecycler.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewVisibleDevices != null) {
            recyclerViewVisibleDevices.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void startBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permissão para busca de dispositivos negada", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(requireContext(), "Ative o Bluetooth para procurar dispositivos", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        swipeRefreshLayout.setRefreshing(false);
    }
}
