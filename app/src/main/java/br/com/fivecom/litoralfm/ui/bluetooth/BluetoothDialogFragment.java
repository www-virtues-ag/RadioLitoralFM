package br.com.fivecom.litoralfm.ui.bluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import br.com.fivecom.litoralfm.R;

public class BluetoothDialogFragment extends DialogFragment {

    private OnDeviceSelectedListener listener;

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(BluetoothDevice device);
        void onDeviceDisconnected(BluetoothDevice device);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelectedListener) {
            listener = (OnDeviceSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnDeviceSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_bluetooth_devices, null);

        TabLayout tabLayout = view.findViewById(R.id.tab);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        BluetoothPagerAdapter pagerAdapter = new BluetoothPagerAdapter(requireActivity(), listener);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Pareados" : "Visíveis");
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    PairedDevicesFragment pairedFrag = pagerAdapter.getPairedFragment();
                    if (pairedFrag != null) {
                        pairedFrag.swipeRefreshLayout.setRefreshing(true);
                        pairedFrag.loadPairedDevices();
                    }
                } else {
                    VisibleDevicesFragment visibleFrag = pagerAdapter.getVisibleFragment();
                    if (visibleFrag != null) {
                        visibleFrag.swipeRefreshLayout.setRefreshing(true);
                        visibleFrag.forceRefreshVisibleDevices();
                    }
                }
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) { }
        });

        Button btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        builder.setView(view)
                .setTitle("Dispositivos Bluetooth");

        return builder.create();
    }
}

