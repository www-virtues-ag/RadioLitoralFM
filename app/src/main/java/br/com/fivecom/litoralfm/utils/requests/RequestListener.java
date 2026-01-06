package br.com.fivecom.litoralfm.utils.requests;

import androidx.annotation.Nullable;

public interface RequestListener<T> {
    default void onRequest(){}

    void onResponse(@Nullable T object, boolean isSuccessful);

    default void onError(@Nullable Throwable t){}
}
