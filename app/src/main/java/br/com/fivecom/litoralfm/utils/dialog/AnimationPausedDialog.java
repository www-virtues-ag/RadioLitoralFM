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
 * Dialog para mostrar quando as animações são pausadas (modo estático)
 */
public class AnimationPausedDialog {

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

        // Usa o ícone de animação
        if (imgIcon != null) {
            try {
                imgIcon.setImageResource(R.drawable.icon_anima_config);
            } catch (Exception e) {
                // Se não encontrar o ícone, usa o padrão do layout
            }
        }

        if (title != null && !title.isEmpty()) {
            if (txtTitle != null) {
                txtTitle.setText(title);
            }
        } else {
            if (txtTitle != null) {
                txtTitle.setText("Animações Pausadas");
            }
        }

        if (message != null && !message.isEmpty()) {
            if (txtMessage != null) {
                txtMessage.setText(message);
            }
        } else {
            if (txtMessage != null) {
                txtMessage.setText("As animações foram pausadas para economizar bateria. Você pode reativá-las nas configurações.");
            }
        }

        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static void showWithMessage(Context context, String message) {
        show(context, null, message);
    }
}
