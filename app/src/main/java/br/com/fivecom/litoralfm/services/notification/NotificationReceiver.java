package br.com.fivecom.litoralfm.services.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.util.UnstableApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.constants.Extras;

public class NotificationReceiver extends BroadcastReceiver {

    private final String CHANNEL_ID = "br.com.fivecom.litoralfm.RADIO_CHANNEL_ID";
    private final String ACTION_PAUSE = "br.com.fivecom.litoralfm.pause";
    private final String ACTION_PLAY = "br.com.fivecom.litoralfm.play";
    private final String ACTION_PREV = "br.com.fivecom.litoralfm.prev";
    private final String ACTION_NEXT = "br.com.fivecom.litoralfm.next";
    private final String ACTION_STOP = "br.com.fivecom.litoralfm.stop";
    private final int NOTIFICATION_ID = 412, REQUEST_CODE = 100;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mStopIntent;
    private Context context;
    private NotificationManager mNotificationManager;
    private MediaControllerCompat mController;
    private MediaSessionCompat.Token mSessionToken;
    private NotificationCallback notificationCallback;
    private NotificationCompat.Builder builder;

    public interface NotificationCallback {
        void startForegroundN(int id, @NonNull Notification notification);

        void stopForegroundN();
    }

    public NotificationReceiver(@NonNull Context context, @NonNull MediaControllerCompat mController, @NonNull MediaSessionCompat.Token mSessionToken) {
        this.context = context;
        this.mController = mController;
        this.mSessionToken = mSessionToken;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel(CHANNEL_ID) != null) {
                mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
                mNotificationManager.cancelAll();
            }
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            mChannel.setSound(null, null);
            mChannel.enableLights(true);
            mChannel.setLightColor(parseColor(R.color.background_dark));
            mChannel.setDescription(context.getString(R.string.app_name));
            mNotificationManager.createNotificationChannel(mChannel);
        }
        mPauseIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(context.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mPlayIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(context.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mPreviousIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(context.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mNextIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(context.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mStopIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(context.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        createNotification();
    }

    public void setListener(@NonNull NotificationCallback notificationCallback) {
        this.notificationCallback = notificationCallback;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void registerReceiver() {
        mController.registerCallback(mCb);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);
        filter.addAction(ACTION_STOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED);
        else context.registerReceiver(this, filter);
    }

    public void unregisterReceiver() {
        mController.unregisterCallback(mCb);
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null)
            switch (action) {
                case ACTION_STOP:
                    mController.getTransportControls().stop();
                    break;
                case ACTION_PAUSE:
                    mController.getTransportControls().pause();
                    break;
                case ACTION_PLAY:
                    mController.getTransportControls().play();
                    break;
                case ACTION_NEXT:
                    mController.getTransportControls().skipToNext();
                    break;
                case ACTION_PREV:
                    mController.getTransportControls().skipToPrevious();
                    break;
            }
    }

    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_STOPPED:
                    mNotificationManager.cancel(NOTIFICATION_ID);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                            && mNotificationManager.getNotificationChannel(CHANNEL_ID) != null)
                        mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
                    notificationCallback.stopForegroundN();
                    break;
                default:
                    int icon;
                    String label;
                    PendingIntent intent;
                    if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        icon = android.R.drawable.ic_media_pause;
                        label = context.getString(R.string.string_playing);
                        intent = mPauseIntent;
                    } else {
                        icon = android.R.drawable.ic_media_play;
                        label = context.getString(R.string.string_paused);
                        intent = mPlayIntent;
                    }
                    builder.setContentText(label);
                    if (Constants.data != null && Constants.data.radios != null &&
                            Constants.data.radios.get(Constants.ID) != null &&
                            Constants.data.radios.get(Constants.ID).name != null) {
                        builder.setContentTitle(Constants.data.radios.get(Constants.ID).name);
                    }
                    builder.clearActions();
                    //builder.addAction(android.R.drawable.ic_media_previous, mService.getString(R.string.string_previous), mPreviousIntent);
                    builder.addAction(icon, context.getString(R.string.string_play), intent);
                    //builder.addAction(android.R.drawable.ic_media_next, mService.getString(R.string.string_next), mNextIntent);
                    builder.addAction(android.R.drawable.ic_delete, context.getString(R.string.string_close), mPauseIntent);

                    Notification notification = builder.build();
                    notificationCallback.startForegroundN(NOTIFICATION_ID, notification);
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (builder == null) return;
            CharSequence uri = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
            builder.setContentTitle(metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE));
            builder.setContentText(metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE));
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
            if (uri == null) {
                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                mNotificationManager.notify(NOTIFICATION_ID, builder.build());
            } else Glide.with(context).asBitmap().load(uri.toString())
                    .error(R.mipmap.ic_launcher)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (builder != null) {
                                builder.setLargeIcon(resource);
                                mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            context.getApplicationContext().sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(Extras.STATE.name(), PlaybackStateCompat.STATE_NONE));
            notificationCallback.stopForegroundN();
        }
    };

    @OptIn(markerClass = UnstableApi.class)
    private void createNotification() {
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setChannelId(CHANNEL_ID)
                .setDeleteIntent(mPauseIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setAllowSystemGeneratedContextualActions(true)
                .setAutoCancel(false)
                .setSound(null)
                .setColorized(true)
                .setColor(parseColor(R.color.background_dark))
                .setContentIntent(PendingIntent.getActivity(context, REQUEST_CODE,
                        new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mSessionToken)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(mPauseIntent)
                        .setShowActionsInCompactView(0, 1));
    }

    @ColorInt
    private int parseColor(int color){
        TypedValue typedValue = new TypedValue();
        return typedValue.data;
    }

}
