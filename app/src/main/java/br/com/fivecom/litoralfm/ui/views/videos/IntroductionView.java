package br.com.fivecom.litoralfm.ui.views.videos;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.devbrackets.android.exomedia.core.video.scale.ScaleType;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;

public class IntroductionView extends VideoView implements OnErrorListener, OnPreparedListener, OnCompletionListener {

    public IntroductionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IntroductionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntroductionView(Context context) {
        super(context);
    }

    private Handler handler;
    private VideoListener videoListener;
    private boolean audio = false;

    public interface VideoListener {
        void onCompleted();
    }

    public void load(@NonNull String url, @DrawableRes int previewImage, int repeatMode, boolean audio, @Nullable VideoListener videoListener) {
        this.videoListener = videoListener;
        if (repeatMode < 1) {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(videoListener::onCompleted, 5000);
        } else
            setRepeatMode(repeatMode);
        this.audio = audio;
        setHandleAudioFocus(false);
        setOnErrorListener(this);
        setOnPreparedListener(this);
        if (getVideoControls() != null)
            getVideoControls().onDetachedFromView(this);
        setPreviewImage(previewImage);
        setMedia(Uri.parse(url));
    }

    public void loadAsset(@NonNull String assetPath, @DrawableRes int previewImage, int repeatMode, boolean audio, boolean loop, @Nullable VideoListener videoListener) {
        String url = "asset:///" + assetPath;
        if (videoListener != null) {
            load(url, previewImage, repeatMode, audio, videoListener);
        } else {
            load(url, previewImage, repeatMode, audio, null);
        }
    }

    @Override
    public void onPrepared() {
        setHandleAudioFocus(false);
        setMeasureBasedOnAspectRatioEnabled(false);
        setScaleType(ScaleType.FIT_XY);
        setFilterTouchesWhenObscured(false);
        if (!audio)
            setVolume(0f);
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> videoListener.onCompleted(), getDuration());
        }
        start();
    }

    @Override
    public void onCompletion() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            videoListener.onCompleted();
        } else restart();
    }

    @Override
    public boolean onError(@Nullable Exception e) {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        if (videoListener != null)
            videoListener.onCompleted();
        return false;
    }
}
