package br.com.fivecom.litoralfm.ui.news.network;

import br.com.fivecom.litoralfm.models.news.FeedResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NoticiaApi {

    // GET https://lab.agazeta.com.br/feed-ag-app-json.php?s=slug&q=50
    @GET("feed-ag-app-json.php")
    Call<FeedResponse> getNoticias(
            @Query("s") String sectionSlug,
            @Query("q") int quantidade
    );
    
    // Para paginação: quantidade é o offset/limite
    // Exemplo: primeira página q=20, segunda página q=40 (20+20)
}
