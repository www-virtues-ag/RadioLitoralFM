package br.com.fivecom.litoralfm.utils.core;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import br.com.fivecom.litoralfm.R;

public class Review {

    public Review(@NonNull AppCompatActivity activity) {
        ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnSuccessListener(reviewInfo -> manager.launchReviewFlow(activity, reviewInfo)
                .addOnCompleteListener(task -> toPlayStore(activity)));
        request.addOnFailureListener(runnable -> toPlayStore(activity));
    }

    private void toPlayStore(@NonNull Context context) {
        try {
            Intents.website_external(context, "market://details?id=" + context.getPackageName());
        } catch (Exception ignored) {
            Intents.website_external(context, context.getString(R.string.url_playstore) + context.getPackageName());
        }
    }

}
