package br.com.fivecom.litoralfm.utils.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import br.com.fivecom.litoralfm.R;

public class NextVersionDialogSimple {
    public static void show(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Em Breve!")
                .setMessage("Disponível na próxima versão")
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    public static void showCustom(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_next_version, null);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btn_ok).setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }
    public static void showWithMessage(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Em Breve!")
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    public static void show(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher  )
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}
