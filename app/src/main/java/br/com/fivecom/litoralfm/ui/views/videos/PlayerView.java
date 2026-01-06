package br.com.fivecom.litoralfm.ui.views.videos;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.devbrackets.android.exomedia.core.state.PlaybackState;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;

import br.com.fivecom.litoralfm.utils.core.Logger;

public class PlayerView extends VideoView implements OnErrorListener, OnCompletionListener, OnPreparedListener {

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerView(Context context) {
        super(context);
    }

    private boolean autoPlay = false;
    private boolean mRetry = true;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null)
                switch (intent.getAction()) {
                    case Intent.ACTION_SCREEN_OFF:
                        pause();
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        restart();
                        break;
                }
        }
    };

    public interface VideoListener {
        void onPlaybackPlaying();

        void onPlaybackPaused();

        void onPlayBuffering();

        void onError(boolean isError);

    }

    public void load(boolean autoPlay, String url, VideoListener videoListener) {
        this.autoPlay = autoPlay;
        setPlaybackListener(playbackState -> {
            switch (playbackState) {
                case PLAYING:
                    mRetry = true;
                    if (videoListener != null)
                        videoListener.onPlaybackPlaying();
                    break;
                case BUFFERING:
                    if (videoListener != null)
                        videoListener.onPlayBuffering();
                    break;
                case PAUSED:
                    if (videoListener != null)
                        videoListener.onPlaybackPaused();
                    break;
                case COMPLETED:
                case STOPPED:
                case ERROR:
                    if (mRetry) {
                        post(this::restart);
                        mRetry = false;
                    } else if (videoListener != null)
                        videoListener.onError(playbackState == PlaybackState.ERROR);
                    break;
            }
        });
        setOnErrorListener(this);
        setOnPreparedListener(this);
        setOnCompletionListener(this);
        setMedia(Uri.parse(url));
    }

    @Override
    public void onPrepared() {
        if (autoPlay) start();
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public boolean onError(@Nullable Exception e) {
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Logger.show(e);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        try {
            IntentFilter intent = new IntentFilter();
            intent.addAction(Intent.ACTION_SCREEN_OFF);
            intent.addAction(Intent.ACTION_SCREEN_ON);
            getContext().registerReceiver(broadcastReceiver, intent);
        } catch (Exception e) {
            Logger.show(e);
        }
        super.onAttachedToWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BACKSLASH:
            case KeyEvent.KEYCODE_BREAK:
            case KeyEvent.KEYCODE_BUTTON_B:
                ((Activity) getContext()).onBackPressed();
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                pause();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                start();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_BUTTON_START:
                if (isPlaying())
                    pause();
                else
                    start();
        }
        return super.onKeyDown(keyCode, event);
    }
}