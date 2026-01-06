package br.com.fivecom.litoralfm.utils.core;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import br.com.fivecom.litoralfm.BuildConfig;
import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.utils.constants.Constants;

public class Votation {

    public void send(@NonNull Context context, @NonNull String artist, @NonNull String music, int like) {
        new OkHttpClient().newCall(new Request.Builder()
                .url(Intents.decode(context.getString(R.string.like)))
                .post(new FormBody.Builder()
                        .add("musica", music)
                        .add("artista", artist)
                        .add("objeto", Constants.data.radios.get(Constants.ID).id)
                        .add("like", String.valueOf(like))
                        .add("sistema", "Android - " + Build.VERSION.RELEASE)
                        .add("dispositivo", Build.MANUFACTURER.replace(" ", "-") + " - " + Build.MODEL.replace(" ", "-"))
                        .add("versao_app", BuildConfig.VERSION_NAME)
                        .build())
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {}
        });
    }
}
