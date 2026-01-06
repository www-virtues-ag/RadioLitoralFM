package br.com.fivecom.litoralfm.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.fivecom.litoralfm.receiver.ScheduleNotificationReceiver;

/**
 * Helper class for scheduling notifications for saved schedule items.
 */
public class ScheduleNotificationHelper {

    private static final String TAG = "ScheduleNotifHelper";
    public static final String CHANNEL_ID = "schedule_notifications";
    public static final String CHANNEL_NAME = "Programação Salva";

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_KEY = "extra_key";

    private final Context context;
    private final AlarmManager alarmManager;

    public ScheduleNotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Create notification channel for Android 8.0+
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificações para programas salvos na programação");
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Schedule a notification for a saved program.
     *
     * @param programKey Unique key for the program
     * @param title      Program title
     * @param hrInicio   Start time string (e.g., "10h00", "10:00", "10h30")
     * @param dayOfWeek  Day of week (1=Sunday, 2=Monday, etc.) or null for today
     */
    public void scheduleNotification(String programKey, String title, String hrInicio, String dayOfWeek) {
        scheduleNotification(programKey, title, hrInicio, dayOfWeek, 5); // 5 minutos antes por padrão
    }

    /**
     * Schedule a notification for a saved program with custom minutes before.
     *
     * @param programKey Unique key for the program
     * @param title      Program title
     * @param hrInicio   Start time string (e.g., "10h00", "10:00", "10h30")
     * @param dayOfWeek  Day of week (1=Sunday, 2=Monday, etc.) or null for today
     * @param minutesBefore Minutes before the program starts to show notification (default: 5)
     */
    public void scheduleNotification(String programKey, String title, String hrInicio, String dayOfWeek, int minutesBefore) {
        if (hrInicio == null || hrInicio.trim().isEmpty()) {
            Log.w(TAG, "Cannot schedule notification: hrInicio is empty");
            return;
        }

        int[] timeParts = parseTime(hrInicio);
        if (timeParts == null) {
            Log.w(TAG, "Cannot parse time: " + hrInicio);
            return;
        }

        int hour = timeParts[0];
        int minute = timeParts[1];

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean dayOfWeekSpecified = false;
        // If day of week is specified, set it
        if (dayOfWeek != null && !dayOfWeek.trim().isEmpty()) {
            try {
                int day = Integer.parseInt(dayOfWeek.trim());
                if (day >= 1 && day <= 7) {
                    dayOfWeekSpecified = true;
                    // Calcula o próximo dia da semana correto
                    int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int daysToAdd = day - currentDayOfWeek;
                    if (daysToAdd < 0) {
                        daysToAdd += 7; // Próxima semana
                    } else if (daysToAdd == 0) {
                        // Se for hoje, verifica se o horário já passou
                        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                            daysToAdd = 7; // Se já passou hoje, agenda para próxima semana
                        }
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid day of week: " + dayOfWeek);
            }
        }

        // Subtrai os minutos antes (5 minutos por padrão)
        calendar.add(Calendar.MINUTE, -minutesBefore);

        // If the time has already passed today (e não foi especificado dia da semana), schedule for next occurrence
        if (!dayOfWeekSpecified && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // Se já passou e não foi especificado dia da semana, agenda para a próxima semana
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        } else if (dayOfWeekSpecified && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // Se dia da semana foi especificado mas ainda passou (após subtrair minutos), agenda para próxima semana
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        Intent intent = new Intent(context, ScheduleNotificationReceiver.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_KEY, programKey);

        int requestCode = programKey.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            }
            Log.d(TAG, "Scheduled notification for " + title + " at " + calendar.getTime());
        } catch (SecurityException e) {
            Log.e(TAG, "Cannot schedule exact alarm: " + e.getMessage());
        }
    }

    /**
     * Cancel a scheduled notification.
     */
    public void cancelNotification(String programKey) {
        Intent intent = new Intent(context, ScheduleNotificationReceiver.class);
        int requestCode = programKey.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Cancelled notification for key: " + programKey);
    }

    /**
     * Parse time string in various formats (e.g., "10h00", "10:00", "1000")
     */
    private int[] parseTime(String timeStr) {
        if (timeStr == null)
            return null;

        String normalized = timeStr.trim().toLowerCase();

        // Try pattern: 10h00, 10h30
        Pattern patternH = Pattern.compile("(\\d{1,2})h(\\d{2})");
        Matcher matcherH = patternH.matcher(normalized);
        if (matcherH.find()) {
            return new int[] { Integer.parseInt(matcherH.group(1)), Integer.parseInt(matcherH.group(2)) };
        }

        // Try pattern: 10:00, 10:30
        Pattern patternColon = Pattern.compile("(\\d{1,2}):(\\d{2})");
        Matcher matcherColon = patternColon.matcher(normalized);
        if (matcherColon.find()) {
            return new int[] { Integer.parseInt(matcherColon.group(1)), Integer.parseInt(matcherColon.group(2)) };
        }

        // Try pattern: 1000, 1030 (4 digits)
        Pattern pattern4Digit = Pattern.compile("^(\\d{2})(\\d{2})$");
        Matcher matcher4Digit = pattern4Digit.matcher(normalized);
        if (matcher4Digit.find()) {
            return new int[] { Integer.parseInt(matcher4Digit.group(1)), Integer.parseInt(matcher4Digit.group(2)) };
        }

        return null;
    }
}
