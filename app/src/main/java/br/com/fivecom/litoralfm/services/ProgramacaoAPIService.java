package br.com.fivecom.litoralfm.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.models.scheduler.ProgramacaoAPIResponse;

/**
 * Serviço para buscar programação da nova API
 * Convertido de ProgramacaoAPIService.swift
 */
public class ProgramacaoAPIService {

    private static final String TAG = "ProgramacaoAPIService";
    private static final String DEFAULT_VERSION = "12.1";
    private static final String DEFAULT_SERVER_URL = "https://devapi.virtueslab.app";

    private final OkHttpClient client;
    private final Gson gson;
    private final Handler handler;
    private final String version;
    private final String serverURL;

    private List<ProgramaAPI> programas;
    private boolean isLoading;
    private String errorMessage;
    private ProgramacaoAPICallback callback;

    // Cache de programação
    private static class ProgramacaoCache {
        private static class CacheEntry {
            List<ProgramaAPI> programas;
            long timestamp;
            
            CacheEntry(List<ProgramaAPI> programas, long timestamp) {
                this.programas = programas;
                this.timestamp = timestamp;
            }
        }
        
        private static final Map<String, CacheEntry> cache = new HashMap<>();
        private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutos
        
        public static List<ProgramaAPI> get(String radioId, String dia) {
            String key = radioId + "_" + (dia != null && !dia.isEmpty() ? dia : "all");
            CacheEntry entry = cache.get(key);
            if (entry != null && (System.currentTimeMillis() - entry.timestamp) < CACHE_DURATION_MS) {
                Log.d(TAG, "💾 Cache hit para: " + key);
                return new ArrayList<>(entry.programas); // Retorna cópia
            }
            Log.d(TAG, "❌ Cache miss para: " + key);
            return null;
        }
        
        public static void put(String radioId, String dia, List<ProgramaAPI> programas) {
            String key = radioId + "_" + (dia != null && !dia.isEmpty() ? dia : "all");
            cache.put(key, new CacheEntry(new ArrayList<>(programas), System.currentTimeMillis()));
            Log.d(TAG, "💾 Cache atualizado para: " + key + " (" + programas.size() + " programas)");
        }
        
        public static void clear(String radioId) {
            // Remove todas as entradas para uma rádio específica
            cache.entrySet().removeIf(entry -> entry.getKey().startsWith(radioId + "_"));
            Log.d(TAG, "🗑️ Cache limpo para rádio: " + radioId);
        }
        
        public static void clearAll() {
            cache.clear();
            Log.d(TAG, "🗑️ Todo o cache foi limpo");
        }
    }

    public interface ProgramacaoAPICallback {
        void onProgramasLoaded(List<ProgramaAPI> programas);
        void onLoadingChanged(boolean isLoading);
        void onError(String errorMessage);
    }

    public ProgramacaoAPIService() {
        this(DEFAULT_VERSION, DEFAULT_SERVER_URL);
    }

    public ProgramacaoAPIService(String version, String serverURL) {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.handler = new Handler(Looper.getMainLooper());
        this.version = version;
        this.serverURL = serverURL;
        this.programas = new ArrayList<>();
    }

    public void setCallback(ProgramacaoAPICallback callback) {
        this.callback = callback;
    }

    public List<ProgramaAPI> getProgramas() {
        return programas;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Busca a programação da API com os parâmetros de rádio e dia
     * @param radioId ID da rádio
     * @param dia Dia da semana (opcional, null ou vazio retorna todos os dias)
     */
    public void fetchProgramacao(String radioId, String dia) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL).newBuilder();
        urlBuilder.addPathSegment(version);
        urlBuilder.addPathSegment("programacao.php");
        urlBuilder.addQueryParameter("radio", radioId);
        
        // Se dia não for vazio, adiciona o parâmetro dia
        if (dia != null && !dia.isEmpty()) {
            urlBuilder.addQueryParameter("dia", dia);
        }

        HttpUrl url = urlBuilder.build();
        
        // Verifica cache antes de fazer request
        List<ProgramaAPI> cached = ProgramacaoCache.get(radioId, dia);
        if (cached != null && !cached.isEmpty()) {
            Log.d(TAG, "✅ Retornando programação do cache");
            programas = cached;
            errorMessage = null;
            
            if (callback != null) {
                callback.onProgramasLoaded(programas);
            }
            return;
        }
        
        Log.d(TAG, "🔄 Buscando programação da URL: " + url.toString());

        isLoading = true;
        errorMessage = null;
        
        if (callback != null) {
            callback.onLoadingChanged(true);
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> {
                    isLoading = false;
                    errorMessage = "Erro ao carregar: " + e.getLocalizedMessage();
                    Log.e(TAG, "❌ Erro: " + e.getLocalizedMessage(), e);
                    
                    if (callback != null) {
                        callback.onLoadingChanged(false);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : null;
                
                handler.post(() -> {
                    isLoading = false;
                    
                    if (callback != null) {
                        callback.onLoadingChanged(false);
                    }

                    if (responseBody == null || responseBody.isEmpty()) {
                        // Se não há dados, verifica se é porque não há programação para o dia
                        programas = new ArrayList<>();
                        errorMessage = null;
                        Log.w(TAG, "⚠️ Sem dados recebidos - assumindo que não há programação");
                        
                        if (callback != null) {
                            callback.onProgramasLoaded(programas);
                        }
                        return;
                    }

                    // Debug: imprimir resposta JSON
                    Log.d(TAG, "📦 Resposta JSON: " + responseBody);

                    // Verifica se a resposta está vazia ou é null
                    String trimmedJson = responseBody.trim();
                    if (trimmedJson.isEmpty() || 
                        trimmedJson.equals("null") || 
                        trimmedJson.equals("{}") || 
                        trimmedJson.equals("[]")) {
                        programas = new ArrayList<>();
                        errorMessage = null;
                        Log.i(TAG, "ℹ️ Resposta vazia - não há programação para este dia");
                        
                        if (callback != null) {
                            callback.onProgramasLoaded(programas);
                        }
                        return;
                    }

                    try {
                        ProgramacaoAPIResponse apiResponse = gson.fromJson(responseBody, ProgramacaoAPIResponse.class);
                        
                        if (apiResponse != null && apiResponse.getProgramas() != null) {
                            programas = apiResponse.getProgramas();
                            errorMessage = null;
                            Log.d(TAG, "✅ " + programas.size() + " programas carregados");
                            
                            // Salva no cache
                            ProgramacaoCache.put(radioId, dia, programas);
                        } else {
                            // Se a chave "programas" não foi encontrada, assume que não há programação
                            programas = new ArrayList<>();
                            errorMessage = null;
                            Log.i(TAG, "ℹ️ Chave 'programas' não encontrada - não há programação");
                            
                            // Salva lista vazia no cache também (evita requests repetidos)
                            ProgramacaoCache.put(radioId, dia, programas);
                        }
                        
                        if (callback != null) {
                            callback.onProgramasLoaded(programas);
                        }
                    } catch (Exception e) {
                        // Se não for um erro de dados faltando, mostra mensagem de erro genérico
                        errorMessage = "Erro ao carregar programação";
                        Log.e(TAG, "❌ Erro ao decodificar: " + e.getLocalizedMessage(), e);
                        
                        // Assume que não há programação em caso de erro
                        programas = new ArrayList<>();
                        
                        if (callback != null) {
                            callback.onProgramasLoaded(programas);
                        }
                    }
                });
            }
        });
    }
}
