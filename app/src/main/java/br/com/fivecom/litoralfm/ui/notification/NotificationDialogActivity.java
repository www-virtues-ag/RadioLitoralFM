package br.com.fivecom.litoralfm.ui.notification;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.notification.Notification;
import br.com.fivecom.litoralfm.utils.constants.Extras;
import br.com.fivecom.litoralfm.utils.core.Intents;

public class NotificationDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Notification notification = getIntent().getParcelableExtra(Extras.DATA.name());
        if (notification == null) {
            finish();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(notification.title);
        builder.setCancelable(false);
        NestedScrollView scrollView = new NestedScrollView(this);
        LinearLayout view = new LinearLayout(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(view);
        if (notification.image != null && !notification.image.isEmpty()) {
            ImageView imageView = new ImageView(new ContextThemeWrapper(this, R.style.ImageViewNotification));
            Glide.with(imageView.getContext()).load(notification.image)
                    .error(R.mipmap.ic_launcher)
                    .placeholder(R.mipmap.ic_launcher).into(imageView);
            view.addView(imageView);
        }
        TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextViewNotification));
        textView.setText(notification.description);
        view.addView(textView);
        builder.setView(scrollView);
        builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
        if (notification.link != null) {
            builder.setNeutralButton(getResources().getString(R.string.string_access), (dialog, id) -> {
                Intents.website_internal(this, notification.link);
                dialog.dismiss();
                finish();
            });
        }
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
