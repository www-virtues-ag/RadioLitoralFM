package br.com.fivecom.litoralfm.ui.news.network;

import java.util.List;

import br.com.fivecom.litoralfm.models.news.Categoria;
import br.com.fivecom.litoralfm.models.news.Noticia;

public interface NoticiasCallback {
    void onSuccess(List<br.com.fivecom.litoralfm.models.news.Noticia> noticias);
    void onError(String errorMessage);
}

