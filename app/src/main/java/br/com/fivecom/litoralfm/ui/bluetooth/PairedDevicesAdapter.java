package br.com.fivecom.litoralfm.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

public class PairedDevicesAdapter extends RecyclerView.Adapter<PairedDevicesAdapter.ViewHolder> {

    private final List<BluetoothDevice> devices;
    private final Context context;
    private final BluetoothDialogFragment.OnDeviceSelectedListener listener;
    private final PairedDevicesFragment pairedFragment;

    public PairedDevicesAdapter(PairedDevicesFragment pairedFragment, Context context, List<BluetoothDevice> devices,
                                BluetoothDialogFragment.OnDeviceSelectedListener listener) {
        this.pairedFragment = pairedFragment;
        this.context = context;
        this.devices = devices;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        Button btnConnect;
        Button btnDispair;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.device_name);
            btnConnect = itemView.findViewById(R.id.bt_connect);
            btnDispair = itemView.findViewById(R.id.bt_dispair);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_paired_device, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.tvDeviceName.setText(device.getName() != null ? device.getName() : "Dispositivo desconhecido");

        holder.btnConnect.setOnClickListener(v -> {
            Log.d("PairedDevicesAdapter", "Botão conectar A2DP pressionado para: " + device.getAddress());
            Toast.makeText(context, "Tentando conectar A2DP a " + device.getName(), Toast.LENGTH_SHORT).show();
            BluetoothConnectionManager.getInstance().connectA2dp(context, device);
        });


        holder.btnDispair.setOnClickListener(v -> {
            BluetoothConnectionManager.getInstance().unpairAndDisconnectA2dp(context, device, new BluetoothConnectionManager.UnpairCallback() {
                @Override
                public void onUnpaired(BluetoothDevice device) {
                    Toast.makeText(context, "Dispositivo despareado com sucesso!", Toast.LENGTH_SHORT).show();
                    pairedFragment.swipeRefreshLayout.setRefreshing(true);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        pairedFragment.loadPairedDevices();
                    }, 2000);
                }
                @Override
                public void onUnpairingFailed(BluetoothDevice device, Exception e) {
                    Toast.makeText(context, "Falha ao desparear o dispositivo", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
