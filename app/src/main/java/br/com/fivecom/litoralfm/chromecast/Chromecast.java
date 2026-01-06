package br.com.fivecom.litoralfm.chromecast;

import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import br.com.fivecom.litoralfm.R;

public class Chromecast {

    //Cast
    private CastContext mCastContext;
    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;
    //Activity
    private AppCompatActivity activity;
    //Tutorial Intro
    private IntroductoryOverlay mIntroductoryOverlay;
    //Buttons
    private MenuItem mediaRouteMenuItem;
    private MediaRouteButton mediaRouteButton;
    private ChromecastListener castCallBacks;
    private MediaInfo.Builder media;
    private String idMedia = "", castID;

    //TODO Init cast: new SimpleCast
    public Chromecast(AppCompatActivity activity, String aDefault) {
        try {
            this.activity = activity;
            new CastStateListener() {
                @Override
                public void onCastStateChanged(int newState) {
                    if (newState != CastState.NO_DEVICES_AVAILABLE) {
                        showIntroductoryOverlay();
                    }
                }
            };
            this.castID = castID;
            mCastContext = CastContext.getSharedInstance(activity);
            if (castID != null)
                mCastContext.setReceiverApplicationId(castID);
            mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
            setupCastListener();
        } catch (Exception e) {
        }
    }

    public SessionManager getSessionManager() {
        return mCastContext.getSessionManager();
    }

    public boolean updateSession(@NonNull String castID) {
        if (!this.castID.equals(castID)) {
            mCastContext.setReceiverApplicationId(castID);
            mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
            this.castID = castID;
            return true;
        }
        return false;
    }

    public void setListener(@Nullable ChromecastListener castCallBacks) {
        this.castCallBacks = castCallBacks;
        if (castCallBacks != null)
            if (isConnected()) castCallBacks.onConnected();
            else castCallBacks.onDisconected();
    }

    //TODO Init cast: Set Menu Item
    public void setMediaButton(Menu menu, int resId) {
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(activity.getApplicationContext(), menu, resId);
    }

    //TODO Init cast: Set Button
    public void setMediaButton(MediaRouteButton mediaRouteButton) {
        this.mediaRouteButton = mediaRouteButton;
        CastButtonFactory.setUpMediaRouteButton(activity.getApplicationContext(), this.mediaRouteButton);
    }

    //TODO Init cast: Intro
    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if (mediaRouteMenuItem != null && mediaRouteMenuItem.isVisible()) {
            builder(new IntroductoryOverlay.Builder(activity, mediaRouteMenuItem)).build().show();
            return;
        }
        if (mediaRouteButton != null && mediaRouteButton.getVisibility() == View.VISIBLE) {
            builder(new IntroductoryOverlay.Builder(activity, mediaRouteButton)).build().show();
        }
    }

    //TODO Init cast: Intro Builder
    private IntroductoryOverlay.Builder builder(IntroductoryOverlay.Builder builder) {
        return builder.setTitleText(activity.getString(R.string.cast_tutorial_title))
                .setSingleTime()
                .setOnOverlayDismissedListener(new IntroductoryOverlay.OnOverlayDismissedListener() {
                    @Override
                    public void onOverlayDismissed() {
                        mIntroductoryOverlay = null;
                    }
                });
    }

    //TODO Init cast: Use in onPause(Activity)
    public void onPause() {
        try {
            mCastContext.getSessionManager().removeSessionManagerListener(
                    mSessionManagerListener, CastSession.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO Init cast: Use in onResume(Activity)
    public void onResume() {
        try {
            mCastContext.getSessionManager().addSessionManagerListener(
                    mSessionManagerListener, CastSession.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO Init cast: Stop casting
    public void onClose() {
        try {
            mCastContext.getSessionManager().endCurrentSession(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO isConnected: if conected
    public boolean isConnected() {
        return mCastSession != null && mCastSession.isConnected();
    }

    public boolean isPlaying() {
        RemoteMediaClient remoteMediaClient = mCastContext.getSessionManager().getCurrentCastSession().getRemoteMediaClient();
        return remoteMediaClient != null && remoteMediaClient.isPlaying();
    }

    public boolean isPlaying(String idMedia) {
        return this.idMedia.equalsIgnoreCase(idMedia) && isPlaying();
    }

    //TODO Play: Play cast
    public void play(MediaOptions mediaOptions) {
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        idMedia = mediaOptions.getMedia();
        /*remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                super.onStatusUpdated();
                //castCallBacks.onConnected();
                remoteMediaClient.unregisterCallback(this);
            }

            @Override
            public void onMetadataUpdated() {
                super.onMetadataUpdated();
            }

            @Override
            public void onQueueStatusUpdated() {
                super.onQueueStatusUpdated();
            }

            @Override
            public void onPreloadStatusUpdated() {
                super.onPreloadStatusUpdated();
            }

            @Override
            public void onSendingRemoteMediaRequest() {
                super.onSendingRemoteMediaRequest();
            }

            @Override
            public void onAdBreakStatusUpdated() {
                super.onAdBreakStatusUpdated();
            }
        });*/
        MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder().setAutoplay(true).build();
        remoteMediaClient.load(buildMediaInfo(mediaOptions), mediaLoadOptions);
    }

    //TODO Setup: Session Manager
    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
                Log.e("CAST", "onSessionEnded");
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
                Log.e("CAST", "onSessionResumed");
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
                Log.e("CAST", "onSessionResumeFailed");
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
                Log.e("CAST", "onSessionStarted");
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
                Log.e("CAST", "onSessionStartFailed");
            }

            @Override
            public void onSessionStarting(CastSession session) {
                Log.e("CAST", "onSessionStarting");
            }

            @Override
            public void onSessionEnding(CastSession session) {
                Log.e("CAST", "onSessionEnding");
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
                mCastSession = session;
                Log.e("CAST", "onSessionResuming");
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
                Log.e("CAST", "onSessionSuspended");
                onApplicationDisconnected();
            }

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                if (castCallBacks != null)
                    castCallBacks.onConnected();
                Log.e("CAST", "onApplicationConnected");
            }

            private void onApplicationDisconnected() {
                if (castCallBacks != null)
                    castCallBacks.onDisconected();
                Log.e("CAST", "onApplicationDisconnected");
            }
        };
    }

    //TODO MediaInfo: Config cast media
    private MediaInfo buildMediaInfo(MediaOptions mediaOptions) {
        MediaMetadata movieMetadata = new MediaMetadata(mediaOptions.getMediaType());

        //TV
        movieMetadata.putString(MediaMetadata.KEY_SERIES_TITLE, mediaOptions.getSubtitle());

        //Music
        movieMetadata.putString(MediaMetadata.KEY_ARTIST, mediaOptions.getSubtitle());

        //Generic
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mediaOptions.getSubtitle());
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mediaOptions.getTitle());

        // Usar logo do drawable se a imagem for null ou vazia
        String imageUrl = mediaOptions.getImage();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "android.resource://" + activity.getPackageName() + "/" + R.mipmap.ic_launcher;
        }
        movieMetadata.addImage(new WebImage(Uri.parse(imageUrl)));

        media = new MediaInfo.Builder(mediaOptions.getMedia())
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(mediaOptions.getContentType())
                .setMetadata(movieMetadata)
                .setStreamDuration(MediaInfo.UNKNOWN_DURATION);
        return media.build();
    }

    public void updateMetadata(MediaOptions mediaOptions) {
        if (media != null) {
            MediaMetadata movieMetadata = new MediaMetadata(mediaOptions.getMediaType());
            //TV
            movieMetadata.putString(MediaMetadata.KEY_SERIES_TITLE, mediaOptions.getSubtitle());
            //Music
            movieMetadata.putString(MediaMetadata.KEY_ARTIST, mediaOptions.getSubtitle());
            //Generic
            movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mediaOptions.getSubtitle());
            movieMetadata.putString(MediaMetadata.KEY_TITLE, mediaOptions.getTitle());
            // Usar logo do drawable se a imagem for null ou vazia
            String imageUrl = mediaOptions.getImage();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imageUrl = "android.resource://" + activity.getPackageName() + "/" + R.mipmap.ic_launcher;
            }
            movieMetadata.addImage(new WebImage(Uri.parse(imageUrl)));
            media.setMetadata(movieMetadata);
            MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder().setAutoplay(true).build();
            mCastSession.getRemoteMediaClient().load(media.build(), mediaLoadOptions);
        }
    }

}
