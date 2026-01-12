package br.com.fivecom.litoralfm.ui.video;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.video.SharedVideoManager;

public class FullscreenVideoActivity extends AppCompatActivity {

    private FrameLayout videoContainer;
    private ImageView btnClose;
    private SharedVideoManager videoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tela cheia
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Esconde a barra de navegação
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_fullscreen_video);

        videoManager = SharedVideoManager.getInstance();

        initViews();
    }

    private void initViews() {
        videoContainer = findViewById(R.id.fullscreen_video_container);
        btnClose = findViewById(R.id.btn_close_fullscreen);

        btnClose.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoManager != null && videoContainer != null) {
            // Anexa o vídeo compartilhado a este container
            videoManager.attachToContainer(videoContainer, SharedVideoManager.VideoLocation.FULLSCREEN);
            videoManager.play(); // Garante que continue tocando
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Não precisamos fazer nada explícito aqui, pois o próximo onResume (da
        // MainFragment)
        // cuidará de mover o vídeo de volta (ou para background se for sair do app)
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Mantém tela cheia quando a janela ganha foco
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
