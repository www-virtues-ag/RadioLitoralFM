package br.com.fivecom.litoralfm.utils.core;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class Image {

    public static void loadBackground(@NonNull View view, @DrawableRes int idRes) {
        Glide.with(view).load(idRes).placeholder(idRes).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                view.setBackground(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                view.setBackground(placeholder);
            }
        });
    }

    public static void load(@Nullable ImageView view, @DrawableRes int idResGif, @DrawableRes int idRes) {
        if (view != null)
            Glide.with(view).asGif().load(idResGif).error(idRes).into(view);
    }

    public static void loadGif(@Nullable ImageView view, @DrawableRes int imageGif) {
        if (view != null)
            Glide.with(view).asGif().load(imageGif).into(view);
    }

    public static void load(@Nullable ImageView view, @DrawableRes int image) {
        if (view != null)
            Glide.with(view).load(image).into(view);
    }
}
