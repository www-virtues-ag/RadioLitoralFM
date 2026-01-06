package br.com.fivecom.litoralfm.news;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.news.Categoria;
import br.com.fivecom.litoralfm.news.CategoriasCallback;
import br.com.fivecom.litoralfm.news.FeedItem;
import br.com.fivecom.litoralfm.news.FeedResponse;
import br.com.fivecom.litoralfm.news.Noticia;
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
        // Categorias principais
        sectionsMap.put("opiniao", "Opinião");
        sectionsMap.put("cotidiano", "Cotidiano");
        sectionsMap.put("economia", "Economia");
        sectionsMap.put("politica", "Política");
        sectionsMap.put("mundo", "Mundo");
        sectionsMap.put("esportes", "Esportes");
        
        // Categorias HZ
        sectionsMap.put("hz-cultura", "HZ Cultura");
        sectionsMap.put("hz-agenda-cultural", "HZ Agenda Cultural");
        sectionsMap.put("hz-gastronomia", "HZ Gastronomia");
        sectionsMap.put("hz-turismos", "HZ Turismo");
        sectionsMap.put("hz-tv-e-famosos", "HZ TV + Famosos");
        
        // Categorias adicionais do menu
        sectionsMap.put("educacao", "Educação");
        sectionsMap.put("brasil", "Brasil");
        sectionsMap.put("cidade", "Cidade");
        sectionsMap.put("policia", "Polícia");
        sectionsMap.put("saude", "Saúde");
        sectionsMap.put("geral", "Geral");
        sectionsMap.put("estrangeiro", "Estrangeiro");
        sectionsMap.put("eventos", "Eventos");
        sectionsMap.put("transito", "Trânsito");
        sectionsMap.put("municipio", "Município");
        sectionsMap.put("estado", "Estado");
        sectionsMap.put("tempo", "Tempo");
        sectionsMap.put("agro", "Agro");
        sectionsMap.put("ciencia", "Ciência");
    }

    /**
     * Equivalente ao fetchCategorias() do Swift.
     */
    private void initCategoriasFromSections() {
        categorias.clear();
        categoriaMap.clear();

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

    /**
     * Busca o nome da categoria por slug
     */
    public String getNomeCategoriaPorSlug(String slug) {
        return sectionsMap.get(slug);
    }

    // =====================
    //   FETCH NOTICIAS
    // =====================

    /**
     * Versão "genérica": busca notícias, podendo filtrar por categorias (IDs internos).
     */
    public void fetchNoticias(List<Integer> categoriaIds, final NoticiasCallback callback) {
        String sectionSlug = null;

        // Busca a categoria selecionada, equivalente ao que você faz no Swift
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            int firstId = categoriaIds.get(0);
            Categoria selected = null;
            for (Categoria cat : categorias) {
                if (cat.getId() == firstId) {
                    selected = cat;
                    break;
                }
            }
            if (selected != null && sectionsMap.containsKey(selected.getSlug())) {
                sectionSlug = selected.getSlug();
            }
        }

        // quantidade padrão = 50
        int quantidade = 50;

        Log.d(TAG, "🔄 Buscando notícias. sectionSlug=" + sectionSlug + " q=" + quantidade);

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
}
