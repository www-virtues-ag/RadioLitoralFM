// NoticiaService.java
package br.com.fivecom.litoralfm.ui.news.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.models.news.Categoria;
import br.com.fivecom.litoralfm.models.news.FeedItem;
import br.com.fivecom.litoralfm.models.news.FeedResponse;
import br.com.fivecom.litoralfm.models.news.Noticia;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NoticiaService {

    private static final String TAG = "NoticiaService";
    private static final String BASE_URL = "https://lab.agazeta.com.br/";

    private static NoticiaService instance;
    private final NoticiaApi api;

    // Equivalente ao sectionsMap do Swift (slug -> nome)
    private final Map<String, String> sectionsMap = new LinkedHashMap<>();

    // Lista de categorias e mapa (id -> nome)
    private final List<Categoria> categorias = new ArrayList<>();
    private final Map<Integer, String> categoriaMap = new HashMap<>();

    // Cache de notícias: slug -> (noticias, timestamp)
    private static class CacheEntry {
        List<Noticia> noticias;
        long timestamp;
        
        CacheEntry(List<Noticia> noticias, long timestamp) {
            this.noticias = noticias;
            this.timestamp = timestamp;
        }
    }
    
    private final Map<String, CacheEntry> cache = new HashMap<>();
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000; // 10 minutos

    private NoticiaService() {
        // Inicializa Retrofit
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        api = retrofit.create(NoticiaApi.class);

        initSectionsMap();
        initCategoriasFromSections();
    }

    public static NoticiaService getInstance() {
        if (instance == null) {
            synchronized (NoticiaService.class) {
                if (instance == null) {
                    instance = new NoticiaService();
                }
            }
        }
        return instance;
    }

    private void initSectionsMap() {
        sectionsMap.put("opiniao", "Opinião");
        sectionsMap.put("cotidiano", "Cotidiano");
        sectionsMap.put("economia", "Economia");
        sectionsMap.put("politica", "Política");
        sectionsMap.put("mundo", "Mundo");
        sectionsMap.put("hz-cultura", "HZ Cultura");
        sectionsMap.put("hz-agenda-cultural", "HZ Agenda Cultural");
        sectionsMap.put("hz-gastronomia", "HZ Gastronomia");
        sectionsMap.put("hz-turismo", "HZ Turismo");
        sectionsMap.put("hz-turismos", "HZ Turismo"); // Fallback para compatibilidade
        sectionsMap.put("hz-tv-e-famosos", "HZ TV + Famosos");
        sectionsMap.put("esportes", "Esportes");
    }

    /**
     * Equivalente ao fetchCategorias() do Swift.
     */
    private void initCategoriasFromSections() {
        categorias.clear();
        categoriaMap.clear();

        // Adiciona categoria "Todas" em primeiro lugar (ID 0)
        Categoria categoriaTodas = new Categoria(
                0,
                "Todas",
                "todas",
                null
        );
        categorias.add(categoriaTodas);
        categoriaMap.put(0, "Todas");

        int index = 1;
        for (Map.Entry<String, String> entry : sectionsMap.entrySet()) {
            Categoria categoria = new Categoria(
                    index,
                    entry.getValue(),
                    entry.getKey(),
                    null
            );
            categorias.add(categoria);
            categoriaMap.put(index, entry.getValue());
            index++;
        }

        Log.d(TAG, "✅ Categorias carregadas: " + categorias.size());
    }

    public void getCategorias(CategoriasCallback callback) {
        callback.onSuccess(new ArrayList<>(categorias));
    }

    // =====================
    //   FETCH NOTICIAS
    // =====================

    /**
     * Versão "genérica": busca notícias, podendo filtrar por categorias (IDs internos).
     * Implementa cache de 10 minutos.
     */
    public void fetchNoticias(List<Integer> categoriaIds, final NoticiasCallback callback) {
        String sectionSlug = null;
        String cacheKey = "todas"; // Chave padrão para cache

        // Se "Todas" (ID 0) está selecionada, sectionSlug fica null
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            int firstId = categoriaIds.get(0);
            
            // Ignora se for "Todas" (ID 0)
            if (firstId == 0) {
                sectionSlug = null;
                cacheKey = "todas";
                Log.d(TAG, "📰 Buscando todas as notícias (categoria 'Todas' selecionada)");
            } else {
                Log.d(TAG, "🔍 Buscando categoria ID: " + firstId);
                
                Categoria selected = null;
                for (Categoria cat : categorias) {
                    if (cat.getId() == firstId) {
                        selected = cat;
                        break;
                    }
                }
                if (selected != null && sectionsMap.containsKey(selected.getSlug())) {
                    sectionSlug = selected.getSlug();
                    cacheKey = sectionSlug;
                    Log.d(TAG, "✅ Categoria encontrada: " + selected.getNome() + " (slug: " + sectionSlug + ")");
                } else {
                    if (selected == null) {
                        Log.w(TAG, "⚠️ Categoria ID " + firstId + " não encontrada");
                    } else {
                        Log.w(TAG, "⚠️ Slug '" + selected.getSlug() + "' não encontrado no sectionsMap");
                    }
                }
            }
        } else {
            Log.d(TAG, "📰 Buscando todas as notícias (nenhuma categoria especificada)");
        }

        // Verifica cache
        final String finalCacheKey = cacheKey; // Torna final para uso no callback
        CacheEntry cached = cache.get(finalCacheKey);
        final long currentTime = System.currentTimeMillis();
        
        if (cached != null && (currentTime - cached.timestamp) < CACHE_DURATION_MS) {
            Log.d(TAG, "💾 Retornando do cache: " + finalCacheKey + " (" + cached.noticias.size() + " notícias)");
            callback.onSuccess(new ArrayList<>(cached.noticias));
            return;
        }

        // quantidade padrão = 50
        int quantidade = 50;

        Log.d(TAG, "🔄 Buscando notícias da API. sectionSlug=" + sectionSlug + " q=" + quantidade);

        Call<FeedResponse> call = api.getNoticias(sectionSlug, quantidade);
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                if (!response.isSuccessful()) {
                    String msg = "Erro HTTP: " + response.code();
                    Log.e(TAG, "❌ " + msg);
                    callback.onError(msg);
                    return;
                }

                FeedResponse body = response.body();
                if (body == null || body.getItem() == null) {
                    Log.w(TAG, "⚠️ Nenhum dado recebido ou array de itens vazio");
                    callback.onError("Nenhuma notícia disponível");
                    return;
                }

                List<FeedItem> feedItems = body.getItem();
                Log.d(TAG, "✅ Total de itens: " + feedItems.size());

                if (feedItems.isEmpty()) {
                    callback.onError("Nenhuma notícia disponível");
                    return;
                }

                List<Noticia> noticias = new ArrayList<>();
                for (FeedItem feedItem : feedItems) {
                    Noticia noticia = Noticia.fromFeedItem(feedItem);
                    noticias.add(noticia);
                }

                // Salva no cache
                cache.put(finalCacheKey, new CacheEntry(new ArrayList<>(noticias), currentTime));
                Log.d(TAG, "💾 Cache atualizado: " + finalCacheKey + " (" + noticias.size() + " notícias)");

                // Log das 3 primeiras, igual ao Swift
                for (int i = 0; i < Math.min(3, noticias.size()); i++) {
                    Noticia n = noticias.get(i);
                    Log.d(TAG, "   " + (i + 1) + ". " + n.getTitulo());
                    Log.d(TAG, "      - Data: " + n.getDataFormatada());
                    Log.d(TAG, "      - Categoria: " + n.getCategoriaFormatada());
                }
                if (noticias.size() > 3) {
                    Log.d(TAG, "   ... e mais " + (noticias.size() - 3) + " notícias");
                }

                callback.onSuccess(noticias);
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                String msg = "Erro: " + t.getLocalizedMessage();
                Log.e(TAG, "❌ Erro na requisição: " + msg, t);
                callback.onError(msg);
            }
        });
    }

    /**
     * Equivalente ao fetchNoticiasPorCategorias(_ categoriaIds: [Int])
     */
    public void fetchNoticiasPorCategorias(List<Integer> categoriaIds, NoticiasCallback callback) {
        fetchNoticias(categoriaIds, callback);
    }

    /**
     * Equivalente ao fetchTodasNoticias() do Swift.
     */
    public void fetchTodasNoticias(NoticiasCallback callback) {
        fetchNoticias(new ArrayList<Integer>(), callback);
    }

    /**
     * Busca notícias com paginação.
     * @param sectionSlug Slug da categoria (null para "Todas")
     * @param page Número da página (começa em 1)
     * @param itemsPerPage Quantidade de itens por página
     * @param callback Callback para retornar os resultados
     */
    public void fetchNoticiasPaginated(String sectionSlug, int page, int itemsPerPage, final NoticiasCallback callback) {
        // Calcula a quantidade total de itens a buscar (offset + limit)
        // Para página 1: q=20, página 2: q=40, página 3: q=60, etc.
        int quantidade = page * itemsPerPage;
        
        String cacheKey = sectionSlug != null ? sectionSlug : "todas";
        // Para paginação, não usamos cache (ou apenas para primeira página)
        // Vamos buscar diretamente da API
        
        Log.d(TAG, "🔄 Buscando página " + page + " da categoria: " + cacheKey + " (q=" + quantidade + ")");

        Call<FeedResponse> call = api.getNoticias(sectionSlug, quantidade);
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                if (!response.isSuccessful()) {
                    String msg = "Erro HTTP: " + response.code();
                    Log.e(TAG, "❌ " + msg);
                    callback.onError(msg);
                    return;
                }

                FeedResponse body = response.body();
                if (body == null || body.getItem() == null) {
                    Log.w(TAG, "⚠️ Nenhum dado recebido ou array de itens vazio");
                    callback.onError("Nenhuma notícia disponível");
                    return;
                }

                List<FeedItem> feedItems = body.getItem();
                Log.d(TAG, "✅ Total de itens recebidos: " + feedItems.size());

                if (feedItems.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Converte apenas os itens necessários (mais eficiente)
                int offset = (page - 1) * itemsPerPage;
                int endIndex = Math.min(offset + itemsPerPage, feedItems.size());
                
                List<Noticia> pageNoticias = new ArrayList<>();
                
                if (offset < feedItems.size()) {
                    // Converte apenas os itens da página atual
                    for (int i = offset; i < endIndex; i++) {
                        FeedItem feedItem = feedItems.get(i);
                        Noticia noticia = Noticia.fromFeedItem(feedItem);
                        pageNoticias.add(noticia);
                    }
                }

                Log.d(TAG, "📄 Página " + page + ": " + pageNoticias.size() + " itens (offset=" + offset + ", total recebido=" + feedItems.size() + ")");

                callback.onSuccess(pageNoticias);
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                String msg = "Erro: " + t.getLocalizedMessage();
                Log.e(TAG, "❌ Erro na requisição: " + msg, t);
                callback.onError(msg);
            }
        });
    }
}
