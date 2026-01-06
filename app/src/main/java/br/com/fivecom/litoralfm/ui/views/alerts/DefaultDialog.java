package br.com.fivecom.litoralfm.ui.views.alerts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import br.com.fivecom.litoralfm.utils.core.Intents;

public class DefaultDialog extends androidx.appcompat.app.AlertDialog.Builder {

    public static final int DEFAULT = 0;
    public static final int FINISH = 1;
    public static final int RECREATE = 2;
    public static final int REDIRECT_CLASS = 3;
    public static final int REDIRECT_URL = 4;
    public static final int REDIRECT_CLASS_FINISH = 5;
    public static final int BACK = 6;

    private int type = 0;
    private Context context;
    private Class activity = null;
    private Intent intent = null;
    private String url = null;

    /**
     * Start Alert
     *
     * @param context (Context)
     */
    public DefaultDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        setCancelable(false);
    }

    /**
     * Select Type Alert
     *
     * @param type (int)
     * @return (AlertDialog)
     */
    public DefaultDialog type(int type) {
        this.type = type;
        return this;
    }

    public DefaultDialog setDesc(@StringRes int message) {
        setMessage(message);
        return this;
    }

    public DefaultDialog setName(@StringRes int title) {
        setTitle(title);
        return this;
    }

    /**
     * SET Url from redirect
     *
     * @param url (String)
     * @return (AlertDialog)
     */
    public DefaultDialog url(@NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * SET Class from redirect
     *
     * @param activity (Class)
     * @return (AlertDialog)
     */
    public DefaultDialog redirect(@NonNull Class activity) {
        this.activity = activity;
        return this;
    }

    /**
     * SET Intent from redirect
     *
     * @param intent (Intent)
     * @return (AlertDialog)
     */
    public DefaultDialog redirect(@NonNull Intent intent) {
        this.intent = intent;
        return this;
    }

    public void start(@StringRes int positive, @StringRes int negative) {
        setPositiveButton(positive, (dialog, which) -> click(dialog));
        setNegativeButton(negative, (dialog, which) -> dialog.dismiss());
        show();
    }

    public void start(@StringRes int positive) {
        setPositiveButton(positive, (dialog, which) -> click(dialog));
        show();
    }

    @NonNull
    @Override
    public androidx.appcompat.app.AlertDialog create() {
        setPositiveButton(android.R.string.ok, (dialog, which) -> click(dialog));
        return super.create();
    }

    public void start() {
        setPositiveButton(android.R.string.ok, (dialog, which) -> click(dialog));
        show();
    }

    private void click(DialogInterface dialog) {
        switch (type) {
            case DEFAULT:
                dialog.dismiss();
                break;
            case FINISH:
                dialog.dismiss();
                ((AppCompatActivity) context).finish();
                break;
            case BACK:
                dialog.dismiss();
                ((AppCompatActivity) context).getOnBackPressedDispatcher().onBackPressed();
                break;
            case RECREATE:
                dialog.dismiss();
                ((AppCompatActivity) context).recreate();
                break;
            case REDIRECT_CLASS:
                context.startActivity((intent != null) ? intent : new Intent(context, activity));
                dialog.dismiss();
                break;
            case REDIRECT_CLASS_FINISH:
                context.startActivity((intent != null) ? intent : new Intent(context, activity));
                dialog.dismiss();
                ((AppCompatActivity) context).finish();
                break;
            case REDIRECT_URL:
                Intents.website_internal(context, url);
                dialog.dismiss();
                break;
        }
    }
}