package br.com.fivecom.litoralfm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import androidx.multidex.MultiDexApplication;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

import br.com.fivecom.litoralfm.models.notification.Notification;
import br.com.fivecom.litoralfm.ui.notification.NotificationDialogActivity;
import br.com.fivecom.litoralfm.ui.splash.SplashActivity;
import br.com.fivecom.litoralfm.utils.constants.Extras;
import br.com.fivecom.litoralfm.util.ScheduleNotificationHelper;


public class Initialize extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.remote_config);
        remoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0).build());

        OneSignal.getDebug().setLogLevel(LogLevel.DEBUG);
        OneSignal.initWithContext(this, "517e953a-e8d5-4838-9075-072d63feca04");
        OneSignal.getNotifications().addClickListener(iNotificationClickEvent -> {
            if (!isAppRunning())
                startActivity(new Intent(Initialize.this, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            startActivity(new Intent(Initialize.this, NotificationDialogActivity.class)
                    .putExtra(Extras.DATA.name(), new Notification(iNotificationClickEvent.getNotification()))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });

        // Cria o canal de notificação para programas salvos
        ScheduleNotificationHelper.createNotificationChannel(this);
    }

    private boolean isAppRunning() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            return activityManager.getRunningTasks(Integer.MAX_VALUE).size() > 2;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }
}