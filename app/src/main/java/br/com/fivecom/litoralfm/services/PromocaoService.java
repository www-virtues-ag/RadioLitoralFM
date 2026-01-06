package br.com.fivecom.litoralfm.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.fivecom.litoralfm.models.promocao.Promocao;
import br.com.fivecom.litoralfm.models.promocao.WordPressPost;
import br.com.fivecom.litoralfm.utils.requests.HttpClient;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Serviço responsável por buscar promoções da API WordPress
 * Equivalente ao PromocaoService do Swift
 */
public class PromocaoService {
    
    private static final String TAG = "PromocaoService";
    private static final String BASE_URL = "https://www.litoralfm.com.br/wp-json/wp/v2";
    
    private final HttpClient httpClient;
    private final Gson gson;
    private final Handler mainHandler;
    private final ExecutorService executorService;
    
    public interface PromocaoCallback {
        void onSuccess(List<Promocao> promocoes);
        void onError(String error);
    }
    
    public PromocaoService() {
        this.httpClient = new HttpClient();
        this.gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newFixedThreadPool(5);
    }
    
    /**
     * Busca a categoria "promocoes" pelo slug
     */
    private void buscarCategoriaPromocoes(CategoriaCallback callback) {
        String url = BASE_URL + "/categories?slug=promocoes";
        Log.d(TAG, "🔄 Buscando categoria 'promocoes' da URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        httpClient.okhttp().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Erro desconhecido";
                Log.e(TAG, "❌ Erro ao buscar categoria: " + msg, e);
                deliverCategoriaError(callback, "Erro: " + msg);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "❌ Resposta não bem-sucedida: " + response.code());
                        deliverCategoriaError(callback, "Erro HTTP: " + response.code());
                        return;
                    }
                    
                    String jsonString = (response.body() != null) ? response.body().string() : null;
                    if (jsonString == null || jsonString.trim().isEmpty()) {
                        Log.e(TAG, "❌ Nenhum dado recebido");
                        deliverCategoriaError(callback, "Nenhum dado recebido");
                        return;
                    }
                    
                    JsonArray categorias = gson.fromJson(jsonString, JsonArray.class);
                    if (categorias == null || categorias.size() == 0) {
                        Log.w(TAG, "⚠️ Categoria 'promocoes' não encontrada");
                        deliverCategoriaError(callback, "Categoria 'promocoes' não encontrada");
                        return;
                    }
                    
                    JsonObject categoria = categorias.get(0).getAsJsonObject();
                    int categoriaId = categoria.get("id").getAsInt();
                    Log.d(TAG, "✅ Categoria encontrada com ID: " + categoriaId);
                    deliverCategoriaSuccess(callback, categoriaId);
                    
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erro ao processar categoria: " + e.getMessage(), e);
                    deliverCategoriaError(callback, "Erro ao processar categoria: " + e.getMessage());
                } finally {
                    response.close();
                }
            }
        });
    }
    
    /**
     * Busca as promoções da API WordPress
     */
    public void fetchPromocoes(PromocaoCallback callback) {
        if (callback == null) return;
        
        Log.d(TAG, "🔄 Iniciando busca de promoções");
        
        // Primeiro, buscar o ID da categoria "promocoes"
        buscarCategoriaPromocoes(new CategoriaCallback() {
            @Override
            public void onSuccess(int categoriaId) {
                buscarPromocoesPorCategoria(categoriaId, callback);
            }
            
            @Override
            public void onError(String error) {
                deliverError(callback, error);
            }
        });
    }
    
    /**
     * Busca promoções por ID da categoria
     */
    private void buscarPromocoesPorCategoria(int categoriaId, PromocaoCallback callback) {
        String url = BASE_URL + "/posts?status=publish&categories=" + categoriaId + "&per_page=100";
        Log.d(TAG, "🔄 Buscando promoções da URL: " + url);
        
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
                    
                    WordPressPost[] posts = gson.fromJson(jsonString, WordPressPost[].class);
                    
                    if (posts == null || posts.length == 0) {
                        Log.w(TAG, "⚠️ Array de posts vazio");
                        deliverError(callback, "Nenhuma promoção disponível");
                        return;
                    }
                    
                    Log.d(TAG, "✅ Total de posts: " + posts.length);
                    
                    // Converter WordPressPost para Promocao de forma assíncrona
                    executorService.execute(() -> {
                        List<Promocao> promocoes = convertPostsToPromocoes(posts);
                        
                        mainHandler.post(() -> {
                            Log.d(TAG, "📊 Promoções carregadas: " + promocoes.size());
                            for (int i = 0; i < Math.min(3, promocoes.size()); i++) {
                                Promocao p = promocoes.get(i);
                                Log.d(TAG, "   " + (i + 1) + ". " + p.getTitulo());
                                Log.d(TAG, "      - Data: " + p.getDataFormatada());
                            }
                            if (promocoes.size() > 3) {
                                Log.d(TAG, "   ... e mais " + (promocoes.size() - 3) + " promoções");
                            }
                            deliverSuccess(callback, promocoes);
                        });
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erro ao decodificar JSON: " + e.getMessage(), e);
                    deliverError(callback, "Falha ao decodificar JSON: " + (e.getMessage() != null ? e.getMessage() : "Erro"));
                } finally {
                    response.close();
                }
            }
        });
    }
    
    /**
     * Converte WordPressPost para Promocao, buscando imagens
     */
    private List<Promocao> convertPostsToPromocoes(WordPressPost[] posts) {
        List<Promocao> promocoes = new ArrayList<>();
        
        for (WordPressPost post : posts) {
            // Buscar URL da imagem destacada
            String imagemURL = null;
            if (post.featuredMedia > 0) {
                imagemURL = fetchMediaURL(post.featuredMedia);
            }
            
            Promocao promocao = new Promocao(post, imagemURL);
            promocoes.add(promocao);
        }
        
        return promocoes;
    }
    
    /**
     * Busca a URL da mídia destacada
     */
    private String fetchMediaURL(int mediaId) {
        String url = BASE_URL + "/media/" + mediaId;
        
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            Response response = httpClient.okhttp().newCall(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                String jsonString = response.body().string();
                JsonObject json = gson.fromJson(jsonString, JsonObject.class);
                
                if (json != null && json.has("source_url")) {
                    String sourceURL = json.get("source_url").getAsString();
                    response.close();
                    return sourceURL;
                }
            }
            
            if (response != null) {
                response.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar mídia " + mediaId + ": " + e.getMessage());
        }
        
        return null;
    }
    
    // ===================== Callbacks =====================
    
    private interface CategoriaCallback {
        void onSuccess(int categoriaId);
        void onError(String error);
    }
    
    private void deliverCategoriaSuccess(CategoriaCallback callback, int categoriaId) {
        mainHandler.post(() -> callback.onSuccess(categoriaId));
    }
    
    private void deliverCategoriaError(CategoriaCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
    
    private void deliverSuccess(PromocaoCallback callback, List<Promocao> promocoes) {
        mainHandler.post(() -> callback.onSuccess(promocoes));
    }
    
    private void deliverError(PromocaoCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
}

