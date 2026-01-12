package br.com.fivecom.litoralfm.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.fivecom.litoralfm.models.locutores.Locutor;
import br.com.fivecom.litoralfm.models.locutores.LocutorResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Serviço responsável por buscar dados dos locutores da API
 */
public class LocutorService {
    private static final String TAG = "LocutorService";
    private final String version;
    private final String serverURL;
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    /**
     * Callback para receber os resultados da API
     */
    public interface LocutorCallback {
        void onSuccess(List<Locutor> locutores);

        void onError(String errorMessage);
    }

    /**
     * Construtor padrão com configurações da rádio
     */
    public LocutorService() {
        this("12.1", "https://devapi.virtueslab.app");
    }

    /**
     * Construtor com parâmetros customizados
     */
    public LocutorService(String version, String serverURL) {
        this.version = version;
        this.serverURL = serverURL;

        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Busca os locutores da API
     */
    public void fetchLocutores(String radioId, final LocutorCallback callback) {
        // Construir URL
        String url = serverURL + "/" + version + "/locutores.php?objeto=" + radioId;

        Log.d(TAG, "🔄 [LocutorService] Buscando locutores da URL: " + url);

        // Fazer requisição em background
        executor.execute(() -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String error = "Erro na requisição: " + response.code();
                        Log.e(TAG, "❌ [LocutorService] " + error);
                        postError(callback, error);
                        return;
                    }

                    String jsonResponse = response.body().string();
                    Log.d(TAG, "📦 [LocutorService] JSON recebido:");
                    Log.d(TAG, jsonResponse);

                    // Decodificar JSON
                    LocutorResponse locutorResponse = gson.fromJson(jsonResponse, LocutorResponse.class);

                    if (locutorResponse == null) {
                        String error = "Resposta nula da API";
                        Log.e(TAG, "❌ [LocutorService] " + error);
                        postError(callback, error);
                        return;
                    }

                    Log.d(TAG, "✅ [LocutorService] Status: " + locutorResponse.getStatus());

                    if (!"success".equalsIgnoreCase(locutorResponse.getStatus())) {
                        String error = "Status diferente de success: " + locutorResponse.getStatus();
                        Log.e(TAG, "❌ [LocutorService] " + error);
                        postError(callback, error);
                        return;
                    }

                    List<LocutorResponse.LocutorData> data = locutorResponse.getData();
                    if (data == null || data.isEmpty()) {
                        String error = "Array de locutores vazio";
                        Log.w(TAG, "⚠️ [LocutorService] " + error);
                        postError(callback, error);
                        return;
                    }

                    Log.d(TAG, "📊 [LocutorService] Total de locutores: " + data.size());

                    // Converter para modelo interno
                    List<Locutor> locutores = new ArrayList<>();
                    for (int i = 0; i < data.size(); i++) {
                        LocutorResponse.LocutorData locutorData = data.get(i);

                        Locutor locutor = new Locutor(
                                locutorData.getLocutor().toLowerCase(), // ID em minúsculo
                                locutorData.getLocutor().toUpperCase(), // Nome em maiúsculo
                                locutorData.getFoto(),
                                locutorData.getDescricao(),
                                locutorData.getFacebook(),
                                locutorData.getInstagram(),
                                locutorData.getWhatsapp());

                        locutores.add(locutor);

                        Log.d(TAG, "   " + (i + 1) + ". " + locutor.getNome());
                        Log.d(TAG, "      - ID: " + locutor.getId());
                        Log.d(TAG, "      - Foto Raw: '" + locutorData.getFoto() + "'");
                        Log.d(TAG, "      - Foto URL: '" + locutor.getFotoUrl() + "'");
                        Log.d(TAG, "      - Descrição: " +
                                (locutor.getDescricao() != null ? "✅ Presente" : "❌ Ausente"));
                    }

                    Log.d(TAG, "✅ [LocutorService] Locutores carregados da API com sucesso");
                    postSuccess(callback, locutores);
                }
            } catch (IOException e) {
                String error = "Erro de I/O: " + e.getMessage();
                Log.e(TAG, "❌ [LocutorService] " + error, e);
                postError(callback, error);
            } catch (Exception e) {
                String error = "Erro ao decodificar JSON: " + e.getMessage();
                Log.e(TAG, "❌ [LocutorService] " + error, e);
                postError(callback, error);
            }
        });
    }

    /**
     * Posta o sucesso na thread principal
     */
    private void postSuccess(final LocutorCallback callback, final List<Locutor> locutores) {
        mainHandler.post(() -> callback.onSuccess(locutores));
    }

    /**
     * Posta o erro na thread principal
     */
    private void postError(final LocutorCallback callback, final String errorMessage) {
        mainHandler.post(() -> callback.onError(errorMessage));
    }

    /**
     * Libera recursos
     */
    public void shutdown() {
        executor.shutdown();
    }
}
