package br.com.fivecom.litoralfm.utils.requests;

import br.com.fivecom.litoralfm.models.email.Email;
import br.com.fivecom.litoralfm.models.promotion.Promotions;
import br.com.fivecom.litoralfm.models.weather.Weather;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TasksAPI {
    @GET("gpt.php")
    Call<String> chat(@Query("prompt") String prompt, @Query("chat") String appid);

    @GET("weather.php")
    Call<Weather> weather(@Query("latitude") Double latitude, @Query("longitude") Double longitude);

    @POST("videoid.php")
    Call<String> youtubeId(@Query("artist") String artist, @Query("song") String music);

    @FormUrlEncoded
    @POST("promocao.php")
    Call<Promotions> promotions(@Field("radio") String radio_id, @Field("modelo") String modelo);

    // Comentado - classes Scheduler/Schedulers não existem
    /*@FormUrlEncoded
    @POST("programaAtual.php")
    Call<Schedulers> schedulerNow(@Field("objeto") String radio_id);

    @FormUrlEncoded
    @POST("programacao.php")
    Call<Schedulers> schedulers(@Field("radio") String radio_id);

    @FormUrlEncoded
    @POST("programacao.php")
    Call<Schedulers> schedulers(@Field("radio") String radio_id, @Field("dia") String day);

    @FormUrlEncoded
    @POST("programa.php")
    Call<Schedulers> scheduler(@Field("radio") String radio_id);*/

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> email_promotion(@Field("nome") String name, @Field("email") String email, @Field("promocao") String subject,
                                @Field("mensagem") String message, @Field("telefone") String phone, @Field("cidade") String city,
                                @Field("bairro") String district, @Field("estado") String state,
                                @Field("cliente") String client, @Field("radio") String radio,
                                @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> email(@Field("nome") String name, @Field("email") String email, @Field("assunto") String subject,
                      @Field("mensagem") String message, @Field("telefone") String phone, @Field("cliente") String client, @Field("radio") String radio,
                      @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> support(@Field("nome") String name, @Field("email") String email, @Field("suporte") String subject,
                        @Field("mensagem") String message, @Field("cliente") String client, @Field("radio") String radio,
                        @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> advertising(@Field("nome") String name, @Field("email") String email, @Field("anuncie") String subject,
                            @Field("mensagem") String message, @Field("cliente") String client, @Field("radio") String radio,
                            @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> music(@Field("nome") String name, @Field("email") String email, @Field("musica") String subject,
                      @Field("mensagem") String message, @Field("cliente") String client, @Field("radio") String radio,
                      @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> prayer(@Field("nome") String name, @Field("email") String email, @Field("oracao") String subject,
                       @Field("mensagem") String message, @Field("cliente") String client, @Field("radio") String radio,
                       @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> testimony(@Field("nome") String name, @Field("email") String email, @Field("testemunho") String subject,
                          @Field("mensagem") String message, @Field("cliente") String client, @Field("radio") String radio,
                          @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("contato_radio_salvar.php")
    Call<Email> suggestion(@Field("nome") String name, @Field("email") String email, @Field("sugestao") String subject,
                           @Field("mensagem") String message, @Field("cliente") String client, @Field("radio") String radio,
                           @Field("sistema") String system, @Field("dispositivo") String device);

    @FormUrlEncoded
    @POST("mailbox.php")
    Call<Email> quiz(@Field("token") String token, @Field("RADIOID") String id, @Field("quiz") String quiz,
                     @Field("mensagem") String mensagem, @Field("plataforma") String plataforma, @Field("versao") String versao,
                     @Field("nome") String nome, @Field("email") String email
            , @Field("dispositivo") String dispositivo, @Field("sistema") String sistema);

    @FormUrlEncoded
    @POST("enqueteResposta.php")
    Call<Email> enqueteResposta(@Field("pergunta") String pergunta, @Field("opcao") String opcao,
                                @Field("sistema") String sistema, @Field("dispositivo") String dispositivo);


}
