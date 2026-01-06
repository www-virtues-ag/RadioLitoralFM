package br.com.fivecom.litoralfm.ui.main.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

public class TickerReceiver extends BroadcastReceiver {

    private final Observer<Void> observer;
    private long mUpdateTimer;
    private long minTimerMillis = 3600000L;

    public TickerReceiver(@NonNull Observer<Void> observer) {
        this.observer = observer;
    }

    public TickerReceiver(long minTimerMillis, @NonNull Observer<Void> observer) {
        this.observer = observer;
        this.minTimerMillis = minTimerMillis;
    }

    public void register(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_TICK), Context.RECEIVER_EXPORTED);
        else
            context.registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_TICK));
        onReceive(context, null);
    }

    public void unRegister(@NonNull Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mUpdateTimer == 0 || System.currentTimeMillis() >= mUpdateTimer) {
            mUpdateTimer = System.currentTimeMillis() + minTimerMillis;
            observer.onChanged(null);
        }
    }
}
