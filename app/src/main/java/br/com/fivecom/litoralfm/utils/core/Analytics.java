package br.com.fivecom.litoralfm.utils.core;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

    private final FirebaseAnalytics mFirebaseAnalytics;

    public Analytics(@NonNull Context context) {
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void registerEvent(String event, String content, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT, content);
        bundle.putString(FirebaseAnalytics.Param.VALUE, value);
        bundle.putString(content, value);
        mFirebaseAnalytics.logEvent(event, bundle);
    }
}
