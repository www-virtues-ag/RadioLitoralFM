package br.com.fivecom.litoralfm.ui.news.network;

import java.util.List;

import br.com.fivecom.litoralfm.models.news.Categoria;

public interface CategoriasCallback {
    void onSuccess(List<Categoria> categorias);
}

