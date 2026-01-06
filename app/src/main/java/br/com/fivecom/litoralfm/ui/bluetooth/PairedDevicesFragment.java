package br.com.fivecom.litoralfm.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Set;

import br.com.fivecom.litoralfm.R;

public class PairedDevicesFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> pairedDevicesList;
    private PairedDevicesAdapter pairedAdapter;
    public SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewPairedDevices;
    private ProgressBar progressRecycler;
    private BluetoothDialogFragment.OnDeviceSelectedListener listener;

    public PairedDevicesFragment() {
    }

    public static PairedDevicesFragment newInstance(BluetoothDialogFragment.OnDeviceSelectedListener listener) {
        PairedDevicesFragment fragment = new PairedDevicesFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(BluetoothDialogFragment.OnDeviceSelectedListener listener) {
        this.listener = listener;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerViewPairedDevices = view.findViewById(R.id.recyclerView);
        progressRecycler = view.findViewById(R.id.progress_recycler);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerViewPairedDevices.setLayoutManager(new LinearLayoutManager(getContext()));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevicesList = new ArrayList<>();

        pairedAdapter = new PairedDevicesAdapter(this, requireContext(), pairedDevicesList, listener);
        recyclerViewPairedDevices.setAdapter(pairedAdapter);

        showProgress(true);
        loadPairedDevices();
        swipeRefreshLayout.setOnRefreshListener(this::loadPairedDevices);

        return view;
    }

    public void refreshPairedDevices() {
        Log.d("Bluetooth", "refreshPairedDevices() chamado!");
        swipeRefreshLayout.setRefreshing(true);
        showProgress(true);
        loadPairedDevices();
    }

    private void showProgress(boolean show) {
        if (progressRecycler != null) {
            progressRecycler.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewPairedDevices != null) {
            recyclerViewPairedDevices.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @SuppressLint("MissingPermission")
    public void loadPairedDevices() {
        Log.d("Bluetooth", "Carregando dispositivos pareados...");
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter != null) {
                pairedDevicesList.clear();
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices != null) {
                    pairedDevicesList.addAll(pairedDevices);
                }
                pairedAdapter.notifyDataSetChanged();
            }
            showProgress(false);
            swipeRefreshLayout.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        } else {
            Toast.makeText(requireContext(), "Permissão de Bluetooth negada", Toast.LENGTH_SHORT).show();
            showProgress(false);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
