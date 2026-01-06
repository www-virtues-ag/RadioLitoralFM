package br.com.fivecom.litoralfm.news;

import br.com.fivecom.litoralfm.news.FeedResponse;
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
}
