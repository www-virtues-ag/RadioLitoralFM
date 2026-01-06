package br.com.fivecom.litoralfm.services;

import static br.com.fivecom.litoralfm.utils.constants.Constants.ID;
import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.util.EventLogger;

import com.devbrackets.android.exomedia.AudioPlayer;
import com.devbrackets.android.exomedia.core.listener.MetadataListener;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.BuildConfig;
import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.Data;
import br.com.fivecom.litoralfm.services.metadata.EntryMetadata;
import br.com.fivecom.litoralfm.services.notification.NotificationReceiver;
import br.com.fivecom.litoralfm.services.validator.PackageValidator;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.constants.Extras;
import br.com.fivecom.litoralfm.utils.core.Logger;

public class MediaService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener,
        OnCompletionListener, OnPreparedListener, OnErrorListener, NotificationReceiver.NotificationCallback {

    private static final String TAG = "MediaService";

    private PackageValidator mPackageValidator;
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;
    private AudioManager audioManager;
    private static int audioSessionToken = -1;
    private AudioPlayer mediaPlayer;
    private NotificationReceiver notificationManager;
    private boolean isRedundant = false, isCall = false, isRestorePlay = false, isForeground = false;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mController != null && mediaPlayer != null) {
                mController.getTransportControls().pause();
                isRestorePlay = mediaPlayer.isPlaying();
            }
        }
    };
    private final BroadcastReceiver customTelephonyReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onReceive(Context context, Intent intent) {
            registerCustomTelephonyCallback(context);
        }
    };
    //public static AudioObject audioObject = new AudioObject();

    @Override
    public void onCreate() {
        super.onCreate();
        mPackageValidator = new PackageValidator(this);
        mSession = new MediaSessionCompat(this, getPackageName() + "." + getClass().getName());
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mController = mSession.getController();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        notificationManager = new NotificationReceiver(this, mController, mSession.getSessionToken());
        notificationManager.setListener(this);
        mediaPlayer = new AudioPlayer(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setMetadataListener(new MetadataListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onMetadata(@NonNull androidx.media3.common.Metadata metadata) {
                new EntryMetadata(metadata.get(0), false)
                        .fetch((music, artist, meta, urlAlbum, album) -> {
                            MediaMetadataCompat.Builder mediaMetadata = new MediaMetadataCompat.Builder();
                            boolean b = music == null || music.isBlank();
                            boolean b2 = artist == null || artist.isBlank();
                            
                            String musicFixed = b ? data.radios.get(ID).name : (music != null ? music.toString() : "");
                            String artistFixed = b2 ? getPlaybackStateMetadata() : (artist != null ? artist.toString() : "");
                            String metaFixed = b && b2 ? (meta != null ? meta.toString() : "") : getString(R.string.app_name);
                            
                            mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, musicFixed);
                            mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artistFixed);
                            mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, metaFixed);
                            mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, urlAlbum != null ? urlAlbum.toString() : "");
                            mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album != null ? album.toString() : "");
                            mediaMetadata.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                            mSession.setMetadata(mediaMetadata.build());
                        });
            }
        });
        mediaPlayer.setAnalyticsListener(new EventLogger(getPackageName() + "." + getClass().getName()));
        mediaPlayer.setWakeLevel(PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                //.setFlags(C.FLAG_AUDIBILITY_ENFORCED)
                .build());
        audioSessionToken = mediaPlayer.getAudioSessionId();
        registerBroadcasters();
    }

    private String getPlaybackStateMetadata() {
        return getString(mController != null
                && mController.getPlaybackState() != null
                && mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING
                ? R.string.string_playing : R.string.string_paused);
    }

    private void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (audioManager != null)
            audioManager.abandonAudioFocus(this);
        //audioObject = null;
        setPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        sendBroadcast(new Intent(Extras.ACTION_UPDATE_MEDIA.name()));
        unregisterBroadcasters();
        stopForegroundN();
        mSession.release();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onUnsubscribe(String id) {
        super.onUnsubscribe(id);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        release();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid))
            return new BrowserRoot("__EMPTY_ROOT__", null);
        return new BrowserRoot(getPackageName() + "." + getClass().getName(), null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaItem>> result) {
        List<MediaItem> mediaItems = new ArrayList<>();
        if (data == null)
            data = new Data.Builder().build();
        for (Data.Radios radios : data.radios) {
            Bundle bundle = new Bundle();
            bundle.putString(Extras.URL.name(), radios.url_stream_audio + ";" + radios.url_stream_audio_secondary);
            bundle.putString(Extras.URL_VIDEO.name(), radios.url_stream_video);
            MediaDescriptionCompat mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                    .setIconBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setMediaId(String.valueOf(mediaItems.size()))
                    .setSubtitle(getString(R.string.string_playing))
                    .setTitle(radios.name)
                    .setMediaUri(Uri.parse(radios.url_stream_audio))
                    .setExtras(bundle)
                    .build();
            MediaItem item = new MediaItem(mediaDescriptionCompat, MediaItem.FLAG_PLAYABLE);
            mediaItems.add(item);
        }
        result.sendResult(mediaItems);
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback {

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onPlay() {
            super.onPlay();
            Log.d(TAG, "onPlay: Called");
            Log.d(TAG, "onPlay: isRedundant=" + isRedundant);
            Log.d(TAG, "onPlay: data.license=" + data.license);

            requestAudioFocus();
            if (data.license)
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    Log.d(TAG, "onPlay: MediaPlayer is not playing, starting buffering");
                    mediaPlayer.setVolume(1f);
                    setPlaybackState(PlaybackStateCompat.STATE_BUFFERING);
                    if (Constants.ID1 > -2) {
                        String streamUrl = !isRedundant ? data.radios.get(ID).url_stream_audio : data.radios.get(ID).url_stream_audio_secondary;
                        Uri uri = Uri.parse(streamUrl);

                        // Log para debug
                        Log.d(TAG, "onPlay: Stream URL: " + streamUrl);
                        Log.d(TAG, "onPlay: Using " + (isRedundant ? "secondary (HTTP)" : "primary (HTTPS)") + " stream");
                        Logger.show("Attempting to play stream: " + streamUrl);
                        Logger.show("Using " + (isRedundant ? "secondary (HTTP)" : "primary (HTTPS)") + " stream");
                        Logger.show("User-Agent: " + getStreamingUserAgent());

                        HttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                                .setAllowCrossProtocolRedirects(true)
                                .setKeepPostFor302Redirects(true)
                                .setConnectTimeoutMs(10000) // 10 segundos timeout de conexão
                                .setReadTimeoutMs(30000) // 30 segundos timeout de leitura
                                .setUserAgent(getStreamingUserAgent());

                        Log.d(TAG, "onPlay: Setting media source...");
                        mediaPlayer.setMedia(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(androidx.media3.common.MediaItem.fromUri(uri)));
                        Log.d(TAG, "onPlay: Media source set, waiting for onPrepared callback");

                        mSession.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, data.radios.get(ID).name)
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, getString(R.string.string_playing))
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, data.radios.get(ID).name)
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "")
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                .build());
                    }
                } else if (mediaPlayer != null) {
                    Log.d(TAG, "onPlay: MediaPlayer already playing, setting volume and state");
                    mediaPlayer.setVolume(1f);
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    Log.e(TAG, "onPlay: MediaPlayer is null!");
                }
            else {
                Log.w(TAG, "onPlay: License check failed - data.license=" + data.license);
            }
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            super.onSkipToQueueItem(queueId);
            if (queueId > -1 && queueId < data.radios.size()) {
                ID = (int) queueId;
                onPlay();
            }
        }

        @Override
        public void onSeekTo(long position) {
            super.onSeekTo(position);
            mediaPlayer.seekTo(position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            ID = Integer.parseInt(mediaId);
            onRewind();
            sendBroadcast(new Intent(Extras.ACTION_UPDATE_MEDIA.name()));
        }

        @Override
        public void onPause() {
            super.onPause();
            mediaPlayer.pause();
            setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
        }

        @Override
        public void onStop() {
            super.onStop();
            release();
        }

        @Override
        public void onRewind() {
            super.onRewind();
            onPause();
            onPlay();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            if (ID == data.radios.size() - 1) ID = 0;
            else ID++;
            onRewind();
            sendBroadcast(new Intent(Extras.ACTION_UPDATE_MEDIA.name()));
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            if (ID == 0) ID = data.radios.size() - 1;
            else ID--;
            onRewind();
            sendBroadcast(new Intent(Extras.ACTION_UPDATE_MEDIA.name()));
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            super.onMediaButtonEvent(mediaButtonEvent);
            KeyEvent mKeyEvent = (KeyEvent) mediaButtonEvent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (mKeyEvent != null && mController.getPlaybackState() != null)
                switch (mKeyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)
                            onPause();
                        else
                            onPlay();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        onPause();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        onPlay();
                        break;
                }
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            if (action != null && action.equalsIgnoreCase("MUTE")) {
                mediaPlayer.setVolume(0.01f);
                setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            } else if (action != null && action.equalsIgnoreCase("RESET")
                    && mediaPlayer != null && mediaPlayer.getVolume() == 0.01f) {
                onPlay();
            }
            /*if (action.equals("UPDATE")) {
                onPause();
                audioObject = extras.getParcelable(Constants.Extra.ID.name());
                onPlay();
            }*/
        }

        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            super.onPlayFromSearch(query, extras);
        }
    }

    @Override
    public void onPrepared() {
        Log.d(TAG, "onPrepared: Called");
        Log.d(TAG, "onPrepared: mediaPlayer.isPlaying()=" + mediaPlayer.isPlaying());
        Log.d(TAG, "onPrepared: Current state=" + (mController != null && mController.getPlaybackState() != null ? mController.getPlaybackState().getState() : "null"));

        if (!mediaPlayer.isPlaying() && mController.getPlaybackState() != null && mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_BUFFERING) {
            Log.d(TAG, "onPrepared: Starting playback, changing state to PLAYING");
            setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            mediaPlayer.start();
            Log.d(TAG, "onPrepared: Playback started successfully");
        } else {
            Log.w(TAG, "onPrepared: Conditions not met - not starting playback");
            Log.w(TAG, "onPrepared: isPlaying=" + mediaPlayer.isPlaying() + ", state=" + (mController.getPlaybackState() != null ? mController.getPlaybackState().getState() : "null"));
        }
    }

    @Override
    public boolean onError(@Nullable Exception e) {
        Logger.show(e);

        // Log detalhado do erro para debug
        if (e != null) {
            Logger.show("Streaming Error: " + e.getMessage());
            Logger.show("Error Type: " + e.getClass().getSimpleName());

            // Tentar alternar entre HTTPS e HTTP se houver erro de rede
            if (e.getMessage() != null &&
                    (e.getMessage().contains("SSL") ||
                            e.getMessage().contains("certificate") ||
                            e.getMessage().contains("handshake"))) {
                Logger.show("SSL/HTTPS error detected, trying HTTP fallback...");
                isRedundant = true; // Forçar uso do link HTTP
                mController.getTransportControls().play();
                return true; // Consumir o erro e tentar novamente
            }
        }

        onCompletion();
        return false;
    }

    @Override
    public void onCompletion() {
        if (!isRedundant) {
            isRedundant = true;
            mController.getTransportControls().play();
        } else {
            isRedundant = false;
            mController.getTransportControls().pause();
            setPlaybackState(PlaybackStateCompat.STATE_ERROR);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!requestAudioFocus() || intent == null || intent.getAction() == null
                || mController == null || mController.getPlaybackState() == null)
            return START_NOT_STICKY;
        else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(Extras.STATE.name(), mController.getPlaybackState().getState()));
        } else {
            switch (Integer.parseInt(intent.getAction())) {
                case PlaybackStateCompat.STATE_PLAYING:
                    if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)
                        mController.getTransportControls().pause();
                    else
                        mController.getTransportControls().play();
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                    mController.getTransportControls().skipToNext();
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                    mController.getTransportControls().skipToPrevious();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                if (!isCall && isRestorePlay && !mediaPlayer.isPlaying()) {
                    mController.getTransportControls().play();
                    isRestorePlay = mediaPlayer.isPlaying();
                } else
                    mediaPlayer.setVolume(1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                isRestorePlay = mediaPlayer.isPlaying();
                mController.getTransportControls().pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            case AudioManager.AUDIOFOCUS_NONE:
                mediaPlayer.setVolume(0.1f);
                break;
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    public void registerCustomTelephonyCallback(Context context) {
        telephonyManager.registerTelephonyCallback(context.getMainExecutor(), new CustomTelephonyCallback(this::telephonyManager));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private class CustomTelephonyCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        private final CallBack mCallBack;

        public CustomTelephonyCallback(CallBack callBack) {
            mCallBack = callBack;
        }

        @Override
        public void onCallStateChanged(int state) {
            mCallBack.callStateChanged(state);
        }
    }

    interface CallBack {
        void callStateChanged(int state);
    }

    private void obsoleteTelephony() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                telephonyManager(state);
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void telephonyManager(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isRestorePlay = mediaPlayer.isPlaying();
                mController.getTransportControls().pause();
                isCall = true;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                mController.getTransportControls().pause();
                isCall = true;
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (isCall && isRestorePlay)
                    mController.getTransportControls().play();
                isRestorePlay = false;
                isCall = false;
                break;
        }
    }

    private void registerBroadcasters() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), Context.RECEIVER_EXPORTED);
            registerReceiver(customTelephonyReceiver, new IntentFilter("android.intent.action.PHONE_STATE"), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                registerReceiver(customTelephonyReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
            else obsoleteTelephony();
        }
        notificationManager.registerReceiver();
    }

    private void unregisterBroadcasters() {
        try {
            notificationManager.unregisterReceiver();
        } catch (Exception e) {
            Logger.show("Error unregistering notification receiver: " + e.getMessage());
        }

        try {
            unregisterReceiver(becomingNoisyReceiver);
        } catch (Exception e) {
            Logger.show("Error unregistering becomingNoisyReceiver: " + e.getMessage());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                unregisterReceiver(customTelephonyReceiver);
            } catch (Exception e) {
                Logger.show("Error unregistering customTelephonyReceiver: " + e.getMessage());
            }
        } else {
            try {
                if (phoneStateListener != null && telephonyManager != null) {
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                }
            } catch (Exception e) {
                Logger.show("Error stopping telephony listener: " + e.getMessage());
            }
        }
    }

    private void setPlaybackState(int i) {
        PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_STOP)
                .setState(i, 0, 1)
                .build();
        mSession.setPlaybackState(playbackStateCompat);
        sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(Extras.STATE.name(), i));
    }

    private String getUserAgent() {
        return String.format(
                "APPRADIO.PRO / MOBILE APP / ANDROID %s / %s - %s / APP: %s / VERSION: %s - %s",
                Build.VERSION.RELEASE,
                Build.MODEL,
                Build.DEVICE,
                getPackageName(),
                BuildConfig.VERSION_CODE,
                BuildConfig.VERSION_NAME);
    }

    private String getStreamingUserAgent() {
        // User-Agent mais compatível com servidores de streaming
        return String.format(
                "Mozilla/5.0 (Linux; Android %s; %s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36",
                Build.VERSION.RELEASE,
                Build.MODEL);
    }

    private boolean selectCommandVoice(@ArrayRes int array, @NonNull String query) {
        String[] strings = getResources().getStringArray(array);
        for (String string : strings)
            return query.contains(string);
        return false;
    }

    @Override
    public void startForegroundN(int id, @NonNull Notification notification) {
        if (!isForeground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            else startForeground(id, notification);
            isForeground = true;
        }
    }

    @Override
    public void stopForegroundN() {
        isForeground = false;
        stopForeground(true);
    }

    public MediaSessionCompat getMediaSession() {
        return mSession;
    }

    public static int getAudioSessionToken() {
        return audioSessionToken;
    }

    private boolean requestAudioFocus() {
        return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

}

