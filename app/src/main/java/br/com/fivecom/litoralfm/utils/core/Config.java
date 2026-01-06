package br.com.fivecom.litoralfm.utils.core;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.io.FileWriter;
import java.util.Objects;

import br.com.fivecom.litoralfm.BuildConfig;
import br.com.fivecom.litoralfm.models.Data;
import br.com.fivecom.litoralfm.utils.constants.Constants;

public class Config {

    public interface OnSuccessListener {
        void TaskResult();
    }

    public void fetch(@NonNull Activity activity, @NonNull OnSuccessListener listener) {
        FirebaseRemoteConfig.getInstance().fetchAndActivate().addOnSuccessListener(runnable -> {
            if (runnable) {
                new Thread(() -> {
                    writeFile(activity);
                    Constants.data = new Data.Builder().build();
                    activity.runOnUiThread(listener::TaskResult);
                }).start();
            } else listener.TaskResult();
        });
    }

    private void writeFile(@NonNull Context context) {
        try {
            String json = FirebaseRemoteConfig.getInstance().getString(BuildConfig.PACKAGE).replace("\"", "\\\"");
            if (json.isEmpty() || json.equals("{}"))
                throw new NullPointerException("JSON is null!");
            File dir = new File(context.getFilesDir().getPath());
            File file = Objects.requireNonNull(dir.listFiles(f -> f.getName().contains("firebase_activate")))[0];
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write("{\"configs_key\":{\"" + BuildConfig.PACKAGE + "\":\"" + json + "\"},\"fetch_time_key\":" + System.currentTimeMillis() + ",\"abt_experiments_key\":[]}");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            Logger.show(e);
        }
    }
}
