package br.com.fivecom.litoralfm.ui.splash;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.RepeatModeUtil;
import androidx.media3.common.util.UnstableApi;

import java.io.IOException;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.databinding.ActivitySplashBinding;
import br.com.fivecom.litoralfm.ui.main.MainActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String PREFS = "app_prefs";
    private static final String KEY_NOTIF_SCREEN_DONE = "notif_screen_done";

    private ActivitySplashBinding binding;
    private Handler animationHandler;
    private ObjectAnimator rotateAnim;
    private MediaPlayer mediaPlayer;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Status bar full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        EdgeToEdge.enable(this);

        initComponents();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initComponents() {
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handler da animação
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
        animationHandler = new Handler(Looper.getMainLooper());

        // Após alguns segundos mostra o loading e começa a animação
        animationHandler.postDelayed(() -> {
            if (binding != null) {
                binding.rlCopyright.setVisibility(View.VISIBLE);
                binding.rlLoading.setVisibility(View.VISIBLE);
                startLoadingAnimation();
            }
        }, 6000);

        // Carrega o vídeo de splash (mutado - audio = false)
        binding.videoView.load(
                "file:///android_asset/" + getString(R.string.splash_video),
                R.drawable.bg_splash,
                RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE,
                false, // Muda de true para false para mutar o vídeo
                // Callback quando o vídeo termina
                this::navigateNextScreen
        );

        // Inicia o áudio de fundo (vinheta_splash.mp3)
        startBackgroundAudio();
    }

    /**
     * Inicia o áudio de fundo (vinheta_splash.mp3)
     */
    private void startBackgroundAudio() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("vinheta_splash.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepare();
            mediaPlayer.setLooping(false); // Não repetir o áudio
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navega para a tela de escolha de rádio (ChooseFragment).
     */
    @OptIn(markerClass = UnstableApi.class)
    private void navigateNextScreen() {
        // Para o áudio antes de navegar
        stopBackgroundAudio();

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        // Sempre vai para ChooseFragment após splash
        intent.putExtra("go_to_choose", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Para e libera o áudio de fundo
     */
    private void stopBackgroundAudio() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /**
     * Inicia a animação do ícone de loading (rotação infinita).
     */
    private void startLoadingAnimation() {
        if (binding == null || binding.icLoading == null) return;

        if (rotateAnim != null) {
            rotateAnim.cancel();
        }

        rotateAnim = ObjectAnimator.ofFloat(binding.icLoading, "rotation", 0f, 360f);
        rotateAnim.setDuration(1500);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnim.start();
    }

    @Override
    protected void onDestroy() {
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }

        if (rotateAnim != null) {
            rotateAnim.cancel();
        }

        // Para e libera o áudio de fundo
        stopBackgroundAudio();

        super.onDestroy();
    }

    @Override
    public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
    }
}