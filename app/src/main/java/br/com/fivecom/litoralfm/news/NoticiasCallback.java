package br.com.fivecom.litoralfm.news;

import java.util.List;

import br.com.fivecom.litoralfm.news.Noticia;

public interface NoticiasCallback {
    void onSuccess(List<Noticia> noticias);
    void onError(String errorMessage);
}
