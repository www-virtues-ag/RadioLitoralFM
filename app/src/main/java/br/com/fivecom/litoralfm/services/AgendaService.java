package br.com.fivecom.litoralfm.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.models.agenda.AgendaItem;
import br.com.fivecom.litoralfm.models.agenda.AgendaResponse;
import br.com.fivecom.litoralfm.utils.requests.HttpClient;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class AgendaService {

    private static final String TAG = "AgendaService";
    private static final String DEFAULT_APP_ID = "13588";
    private static final String DEFAULT_VERSION = "12.1";
    private static final String DEFAULT_SERVER_URL = "https://devapi.virtueslab.app";

    private final String appId;
    private final String version;
    private final String serverURL;
    private final HttpClient httpClient;
    private final Gson gson;

    // ✅ Handler da Main Thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Cache de agenda (static para compartilhar entre instâncias)
    private static class AgendaCacheEntry {
        List<AgendaItem> agenda;
        long timestamp;
        
        AgendaCacheEntry(List<AgendaItem> agenda, long timestamp) {
            this.agenda = agenda;
            this.timestamp = timestamp;
        }
    }
    
    private static AgendaCacheEntry agendaCache = null;
    private static final long AGENDA_CACHE_DURATION_MS = 10 * 60 * 1000; // 10 minutos

    public interface AgendaCallback {
        void onSuccess(List<AgendaItem> agenda);
        void onError(String error);
    }

    public AgendaService() {
        this(DEFAULT_APP_ID, DEFAULT_VERSION, DEFAULT_SERVER_URL);
    }

    public AgendaService(String appId, String version, String serverURL) {
        this.appId = appId;
        this.version = version;
        this.serverURL = serverURL;
        this.httpClient = new HttpClient();
        this.gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create();
    }

    public void fetchAgenda(AgendaCallback callback) {
        if (callback == null) return;

        // Verifica cache antes de fazer request
        if (agendaCache != null && (System.currentTimeMillis() - agendaCache.timestamp) < AGENDA_CACHE_DURATION_MS) {
            Log.d(TAG, "💾 Retornando agenda do cache (" + agendaCache.agenda.size() + " itens)");
            deliverSuccess(callback, new ArrayList<>(agendaCache.agenda));
            return;
        }

        String url = buildURL();
        Log.d(TAG, "🔄 Buscando agenda da URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.okhttp().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Erro desconhecido";
                Log.e(TAG, "❌ Erro na requisição: " + msg, e);
                deliverError(callback, "Erro: " + msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Garantir fechamento do body
                try {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "❌ Resposta não bem-sucedida: " + response.code());
                        deliverError(callback, "Erro HTTP: " + response.code());
                        return;
                    }

                    String jsonString = (response.body() != null) ? response.body().string() : null;
                    if (jsonString == null || jsonString.trim().isEmpty()) {
                        Log.e(TAG, "❌ Nenhum dado recebido");
                        deliverError(callback, "Nenhum dado recebido");
                        return;
                    }

                    // Preview do JSON
                    String preview = jsonString.length() > 500 ? jsonString.substring(0, 500) + "..." : jsonString;
                    Log.d(TAG, "📦 JSON recebido (preview):");
                    Log.d(TAG, preview);

                    try {
                        AgendaResponse agendaResponse = gson.fromJson(jsonString, AgendaResponse.class);

                        if (agendaResponse == null) {
                            Log.e(TAG, "❌ AgendaResponse é null após parse");
                            deliverError(callback, "Erro ao processar resposta da API");
                            return;
                        }

                        if (agendaResponse.agenda == null) {
                            Log.w(TAG, "⚠️ Campo 'agenda' é null na resposta");
                            deliverError(callback, "Formato de resposta inválido");
                            return;
                        }

                        if (agendaResponse.agenda.isEmpty()) {
                            Log.w(TAG, "⚠️ Array de agenda vazio");
                            deliverError(callback, "Nenhum evento disponível");
                            return;
                        }

                        // Ordenar por data (mais recente primeiro)
                        List<AgendaItem> agendaOrdenada = new ArrayList<>(agendaResponse.agenda);
                        Collections.sort(agendaOrdenada, new Comparator<AgendaItem>() {
                            @Override
                            public int compare(AgendaItem item1, AgendaItem item2) {
                                Date date1 = item1 != null ? item1.getInicioDate() : null;
                                Date date2 = item2 != null ? item2.getInicioDate() : null;

                                if (date1 == null && date2 == null) return 0;
                                if (date1 == null) return 1;
                                if (date2 == null) return -1;

                                return date2.compareTo(date1); // mais recente primeiro
                            }
                        });

                        Log.d(TAG, "✅ Total de itens na agenda: " + agendaOrdenada.size());

                        // Salva no cache
                        agendaCache = new AgendaCacheEntry(new ArrayList<>(agendaOrdenada), System.currentTimeMillis());
                        Log.d(TAG, "💾 Cache de agenda atualizado");

                        // Entrega sucesso na Main Thread
                        deliverSuccess(callback, agendaOrdenada);

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erro ao decodificar JSON: " + e.getMessage(), e);
                        Log.e(TAG, "JSON completo recebido:");
                        Log.e(TAG, jsonString);
                        deliverError(callback, "Falha ao decodificar JSON: " + (e.getMessage() != null ? e.getMessage() : "Erro"));
                    }

                } finally {
                    // response.body().string() já consome e fecha internamente,
                    // mas manter o close do response é uma boa prática.
                    response.close();
                }
            }
        });
    }

    // ===================== Helpers (sempre Main Thread) =====================

    private void deliverSuccess(AgendaCallback callback, List<AgendaItem> agenda) {
        mainHandler.post(() -> callback.onSuccess(agenda));
    }

    private void deliverError(AgendaCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    private String buildURL() {
        return serverURL + "/" + version + "/agenda.php?APPID=" + appId;
    }
}
