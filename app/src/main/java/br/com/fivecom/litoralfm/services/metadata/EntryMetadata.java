package br.com.fivecom.litoralfm.services.metadata;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.Metadata;
import androidx.media3.common.util.UnstableApi;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import br.com.fivecom.litoralfm.utils.core.Logger;

public class EntryMetadata {

    public interface MetadataCallback {
        void onResponse(String music, String artist, String metadata, String urlAlbum, String album);
    }

    private String mMetadata;

    @OptIn(markerClass = UnstableApi.class)
    public EntryMetadata(@NonNull Metadata.Entry entry, boolean convertUTF8) {
        mMetadata = getData(entry.toString());
        if (!(mMetadata != null && !mMetadata.isEmpty() && !mMetadata.contains("now playing info goes here") && !mMetadata.contains("hi-fi stream")));
        else if (convertUTF8) mMetadata = decoder(mMetadata);
    }

    private String getData(String input) {
        int keyIndex = input.indexOf("title" + "=\"");
        if (keyIndex == -1)
            return null;
        int start = keyIndex + "title".length() + 2;
        int end = input.indexOf("\"", start);
        return input.substring(start, end);
    }

    private String decoder(@NonNull String string) {
        try {
            return new String(string.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return string;
        }
    }

    private String getArtist() {
        if (mMetadata != null && mMetadata.contains("-"))
            return remove(mMetadata.split("-")[0], "\\s?P\\d+\\s");
        else return null;
    }

    private String getMusic() {
        if (mMetadata == null) return null;
        String[] strings = mMetadata.split("-");
        if (strings.length >= 2)
            return remove(mMetadata.split("-")[1], "\\s?P\\d+\\s");
        else return mMetadata;
    }

    private String remove(@NonNull String string, @NonNull String regex) {
        return string.replaceAll(regex, " ").trim();
    }

    public void fetch(@NonNull MetadataCallback callback) {
        new OkHttpClient().newCall(new Request.Builder().url("https://itunes.apple.com/search?term="
                + remove(mMetadata, "\\s?P\\d+\\s") + "&media=music&limit=1").build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                callback.onResponse(getMusic(), getArtist(), mMetadata, "", "Unknown Album");
                Logger.show(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    EntryAlbum.Result result = new Gson().fromJson(response.body().string(), EntryAlbum.class).results.get(0);
                    callback.onResponse(getMusic(), getArtist(), mMetadata,
                            result.artworkUrl100.replace("100x100bb", "600x600bb"),
                            result.collectionCensoredName);
                } catch (Exception e) {
                    callback.onResponse(getMusic(), getArtist(), mMetadata, "", "Unknown Album");
                    Logger.show(e);
                }
            }
        });
    }
}