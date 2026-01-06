package br.com.fivecom.litoralfm.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.util.ScheduleNotificationHelper;

/**
 * BroadcastReceiver that handles scheduled notifications for saved programs.
 */
public class ScheduleNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "ScheduleNotifReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(ScheduleNotificationHelper.EXTRA_TITLE);
        String key = intent.getStringExtra(ScheduleNotificationHelper.EXTRA_KEY);

        if (title == null || title.isEmpty()) {
            title = "Programa";
        }

        Log.d(TAG, "Received notification for: " + title);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("go_to_schedule", true);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                key != null ? key.hashCode() : 0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, ScheduleNotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.bt_notification)
                .setContentTitle("Litoral FM - Programa Iniciando")
                .setContentText(title + " começa em 5 minutos!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = key != null ? key.hashCode() : (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification shown for: " + title);
        }
    }
}
