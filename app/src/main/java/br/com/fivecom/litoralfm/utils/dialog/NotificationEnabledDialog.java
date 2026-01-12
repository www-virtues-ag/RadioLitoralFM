package br.com.fivecom.litoralfm.utils.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.fivecom.litoralfm.R;

/**
 * Dialog para mostrar quando as notificações são ativadas
 */
public class NotificationEnabledDialog {

    public static void show(Context context) {
        show(context, null, null);
    }

    public static void show(Context context, String title, String message) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_notification_enabled, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView imgIcon = view.findViewById(R.id.img_icon);
        TextView txtTitle = view.findViewById(R.id.txt_title);
        TextView txtMessage = view.findViewById(R.id.txt_message);
        Button btnOk = view.findViewById(R.id.btn_ok);

        if (title != null && !title.isEmpty()) {
            txtTitle.setText(title);
        }
        if (message != null && !message.isEmpty()) {
            txtMessage.setText(message);
        }

        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static void showWithMessage(Context context, String message) {
        show(context, null, message);
    }
}
