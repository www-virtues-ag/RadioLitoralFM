package br.com.fivecom.litoralfm.utils.core;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Base64;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.Data;
import br.com.fivecom.litoralfm.ui.views.alerts.DefaultDialog;

public class Intents {

    /**
     * Share App
     *
     * @param context     (Context)
     * @param title       (int)
     * @param description (int)
     */
    public static void share(@NonNull Context context, @StringRes int title, @StringRes int description) {
        share(context, context.getString(title), context.getString(description));
    }

    /**
     * Share App
     *
     * @param context     (Context)
     * @param title       (String)
     * @param description (String)
     */
    public static void share(@NonNull Context context, @NonNull String title, @NonNull String description) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, description);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.nav_share)));
    }

    /**
     * Send Emails
     *
     * @param context (Context)
     * @param emails  (String[])
     * @param message (int)
     */
    public static void email(@NonNull Context context, @NonNull String[] emails, @StringRes int message) {
        email(context, emails, context.getString(message));
    }

    /**
     * Send Emails
     *
     * @param context (Context)
     * @param emails  (String[])
     * @param message (String)
     */
    public static void email(@NonNull Context context, @NonNull String[] emails, @NonNull String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("message/rfc822");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.string_email)));
    }

    /**
     * Open Site in App
     *
     * @param context (Context)
     * @param url     (int)
     */
    public static void website_internal(@NonNull Context context, @StringRes int url) {
        website_internal(context, context.getString(url));
    }

    /**
     * Open Site in App
     *
     * @param context (Context)
     * @param url     (String)
     */
    public static void website_internal(@NonNull Context context, @NonNull String url) {
        try {
            if (isEmpty(context, url)) return;
            TypedValue typedValue = new TypedValue();
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(typedValue.data);
            builder.setSecondaryToolbarColor(typedValue.data);
            builder.build().launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            Logger.show(e);
            Toast.makeText(context, context.getString(R.string.alert_action), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open Site in Browser
     *
     * @param context (Context)
     * @param url     (int)
     */
    public static void website_external(@NonNull Context context, @StringRes int url) {
        website_external(context, context.getString(url));
    }

    /**
     * Open Site in Browser
     *
     * @param context (Context)
     * @param url     (String)
     */
    public static void website_external(@NonNull Context context, @NonNull String url) {
        if (isEmpty(context, url)) return;
        Intent intent = get(url);
        context.startActivity(intent);
    }

    /**
     * Open Others Apps
     *
     * @param context (Context)
     * @param social  (Social)
     * @param url     (int)
     */
    public static void app(@NonNull Context context, @NonNull Social social, @StringRes int url) {
        app(context, social, context.getString(url));
    }

    /**
     * Open Others Apps
     *
     * @param context (Context)
     * @param social  (Social)
     * @param url     (String)
     */
    public static void app(@NonNull Context context, @NonNull Social social, @NonNull String url) {
        String[] s = social.get().split(";");
        boolean b = s.length > 1 ? install(context, s[0]) || install(context, s[1]) : install(context, s[0]);
        if (b) website_external(context, url);
        else if (social == Social.WHATSAPP)
            alert(context, "com.whatsapp");
        else website_internal(context, url);
    }

    /**
     * Open Others Apps
     *
     * @param context (Context)
     * @param social  (Social)
     * @param list    (List<Links>)
     */
    public static void app(@NonNull Context context, @NonNull Social social, @NonNull List<Data.Links> list) {
        app(context, social, list.get(0));
    }

    public static void app(@NonNull Context context, @NonNull Social social, @NonNull Data.Links list) {
        String[] s = social.get().split(";");
        boolean b = s.length > 1 ? install(context, s[0]) || install(context, s[1]) : install(context, s[0]);
        try {
            switch (social) {
                case WHATSAPP:
                    if (b)
                        context.startActivity(get("whatsapp://" + (list.type.equals("group") ? "chat?code=" : "send?phone=") + list.scheme));
                    else alert(context, "com.whatsapp");
                    break;
                case FACEBOOK:
                    if (b)
                        website_external(context, "fb://" + list.type + "/" + list.scheme);
                    else website_internal(context, list.url);
                    break;
                case SNAPCHAT:
                    if (b)
                        website_external(context, "https://snapchat.com/add/" + list.scheme);
                    else website_internal(context, list.url);
                    break;
                default:
                    if (b) website_external(context, list.url);
                    else website_internal(context, list.url);
                    break;
            }
        } catch (ActivityNotFoundException e) {
            website_internal(context, list.url);
            Logger.show(e);
        }
    }

    /**
     * Open Whatsapp
     *
     * @param context (Context)
     * @param uri     (int)
     * @param type    (Whatsapp)
     */
    public static void whatsapp(@NonNull Context context, @StringRes int uri, Whatsapp type) {
        whatsapp(context, context.getString(uri), type);
    }

    public static void whatsapp(@NonNull Context context, @NonNull String uri, @NonNull Whatsapp type) {
        app(context, Social.WHATSAPP, "whatsapp://" + (type == Whatsapp.GROUP ? "chat?code=" : "send?phone=") + uri);
    }

    /**
     * Open Skype
     *
     * @param context (context)
     * @param uri     (int)
     */
    public static void skype(@NonNull Context context, @StringRes int uri) throws Exception {
        skype(context, context.getString(uri));
    }

    /**
     * Open Skype
     *
     * @param context (Context)
     * @param uri     (String)
     */
    public static void skype(@NonNull Context context, @NonNull String uri) {
        try {
            if (!install(context, "com.skype.raider")) {
                alert(context, "com.skype.raider");
                return;
            }
            Intent intent = get("skype:" + uri + "?chat");
            //intent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
            context.startActivity(intent);
        } catch (Exception e) {
            alert(context, "com.skype.raider");
        }
    }

    /**
     * Call Phone
     *
     * @param context (Context)
     * @param phone   (int)
     */
    public static void call(@NonNull Context context, @StringRes int phone) {
        call(context, context.getString(phone));
    }

    /**
     * Call Phone
     *
     * @param context (Context)
     * @param phone   (String)
     */
    public static void call(@NonNull Context context, @NonNull String phone) {
        try {
            Intent intent = get("tel:" + phone);
            intent.setAction(Intent.ACTION_CALL);
            context.startActivity(intent);
        } catch (Exception e) {
            Logger.show(e);
            Toast.makeText(context, context.getString(R.string.alert_action), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Installs Apps
     *
     * @param context (Context)
     * @param path    (Package)
     * @return (boolean)
     */
    private static boolean install(@NonNull Context context, @NonNull String path) {
        try {
            PackageManager packageManager = context.getPackageManager();
            packageManager.getPackageInfo(path, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.show(e);
            return false;
        }
    }

    /**
     * Return Intent
     *
     * @param uri (String)
     * @return (Intent)
     */
    private static Intent get(@NonNull String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndNormalize(Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    /**
     * Show Alert IF No App Installed
     *
     * @param context (Context)
     * @param path    (String)
     */
    private static void alert(@NonNull Context context, @NonNull String path) {
        new DefaultDialog(context).type(DefaultDialog.REDIRECT_URL).url("market://details?id=" + path).setDesc(R.string.alert_download).start();
    }

    /**
     * Show Alert Url is empty
     *
     * @param context (Context)
     * @param url     (String)
     * @return (boolean)
     */
    private static boolean isEmpty(@NonNull Context context, @NonNull String url) {
        if (!url.isEmpty()) return false;
        new DefaultDialog(context).type(DefaultDialog.DEFAULT).setDesc(R.string.string_avaliable).start();
        return true;
    }

    public enum Whatsapp {
        GROUP, PHONE;
    }

    public enum Social {
        FACEBOOK("com.facebook.katana;com.facebook.lite"),
        WHATSAPP("com.whatsapp;com.whatsapp.w4b"), AMAZON("com.amazon.mp3;com.amazon.music.tv;com.amazon.mp3.automotiveOS"),
        INSTAGRAM("com.instagram.android"), TWITTER("com.twitter.android"), TWITCH("tv.twitch.android.app"),
        SOUNDCLOUD("com.soundcloud.android"), TUNEIN("tunein.player"), TIKTOK("com.zhiliaoapp.musically.go,com.zhiliaoapp.musically"),
        LINKEDIN("com.linkedin.android;com.linkedin.android.lite"), SNAPCHAT("com.snapchat.android"),
        YOUTUBE("com.google.android.youtube"), VIMEO("com.vimeo.android.videoapp"), SPOTIFY("com.spotify.music;com.spotify.lite"),
        GOOGLE_MAPS("com.google.android.apps.maps");
        String s;

        Social(String s1) {
            s = s1;
        }

        public String get() {
            return s;
        }
    }

    public static String decode(@NonNull String string) {
        byte[] data = Base64.decode(string, Base64.DEFAULT);
        return new String(data);
    }

}