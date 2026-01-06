package br.com.fivecom.litoralfm.ui.bluetooth;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BluetoothPagerAdapter extends FragmentStateAdapter {

    private final BluetoothDialogFragment.OnDeviceSelectedListener listener;
    private PairedDevicesFragment pairedFragment;
    private VisibleDevicesFragment visibleFragment;

    public BluetoothPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                 BluetoothDialogFragment.OnDeviceSelectedListener listener) {
        super(fragmentActivity);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            pairedFragment = PairedDevicesFragment.newInstance(listener);
            return pairedFragment;
        } else {
            visibleFragment = new VisibleDevicesFragment();
            return visibleFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public PairedDevicesFragment getPairedFragment() {
        return pairedFragment;
    }

    public VisibleDevicesFragment getVisibleFragment() {
        return visibleFragment;
    }
}
