package br.com.fivecom.litoralfm.utils.core;

import android.util.Log;

import androidx.annotation.NonNull;

import br.com.fivecom.litoralfm.BuildConfig;

public class Logger {
    public static void show(@NonNull Exception e) {
        if (BuildConfig.DEBUG)
            Log.e("LOGGER-DEBUG-TEST", "Cause: " + e.getCause() + " Local: " + e.getLocalizedMessage() + " Error: " + e.getMessage());
    }

    public static void show(@NonNull Throwable e) {
        if (BuildConfig.DEBUG)
            Log.e("LOGGER-DEBUG-TEST", "Cause: " + e.getCause() + " Local: " + e.getLocalizedMessage() + " Error: " + e.getMessage());
    }

    public static void show(@NonNull String e) {
        if (BuildConfig.DEBUG)
            Log.e("LOGGER-DEBUG-TEST", "Error: " + e);
    }
}
