package br.com.fivecom.litoralfm.utils.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public final class Update {
    public Update(@NonNull AppCompatActivity activity) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(activity);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)
                appUpdateManager.startUpdateFlow(appUpdateInfo, activity, AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE));
        });
    }
}
