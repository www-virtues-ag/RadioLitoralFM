package br.com.fivecom.litoralfm.video;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Gerenciador de WebView única compartilhada para reprodução de vídeo.
 * Substituindo Dailymotion Player SDK por WebView padrão.
 */
public class SharedVideoManager {

    private static final String TAG = "SharedVideoManager";

    // Singleton instance
    private static SharedVideoManager instance;

    // WebView única compartilhada
    private WebView sharedWebView;
    private Context applicationContext;

    // Estado
    private VideoLocation currentLocation = VideoLocation.DETACHED;
    private boolean isPlaying = false;
    private boolean isVideoManuallyPaused = false;
    private String currentVideoId = "";

    // Variáveis para tratamento assíncrono
    private boolean isInitializing = false;
    private String pendingVideoId = null;
    private FrameLayout pendingContainer = null;
    private VideoLocation pendingLocation = null;

    // Constantes
    private static final String DEFAULT_VIDEO_ID = "k5imdGeVuZBajbE7oN8";

    // Container atual onde WebView está anexada
    private FrameLayout currentContainer;

    /**
     * Localização possíveis da WebView
     */
    public enum VideoLocation {
        MAIN_FRAGMENT, // Visível no MainFragment
        RADIO_FRAGMENT, // Visível no RadioFragment
        BACKGROUND, // Background invisível (MainActivity)
        FULLSCREEN, // Visível na Activity de tela cheia
        DETACHED // Não anexada a nenhum container
    }

    /**
     * Listener para mudanças de estado
     */
    public interface VideoStateListener {
        void onLocationChanged(VideoLocation newLocation);

        void onPlaybackStateChanged(boolean isPlaying);
    }

    private VideoStateListener stateListener;

    // Private constructor (Singleton)
    private SharedVideoManager() {
    }

    /**
     * Obtém instância singleton
     */
    public static synchronized SharedVideoManager getInstance() {
        if (instance == null) {
            instance = new SharedVideoManager();
        }
        return instance;
    }

    /**
     * Inicializa a WebView compartilhada.
     * Deve ser chamar apenas uma vez, no onCreate da MainActivity.
     */
    public void initialize(Context context) {
        if (sharedWebView != null || isInitializing) {
            Log.w(TAG, "WebView já inicializada ou em processo, ignorando...");
            return;
        }

        Log.d(TAG, "🎬 Inicializando WebView Shared");
        isInitializing = true;

        this.applicationContext = context.getApplicationContext();
        // Context wrapper para permitir troca de activity
        MutableContextWrapper contextWrapper = new MutableContextWrapper(this.applicationContext);

        try {
            sharedWebView = new WebView(contextWrapper);
            setupWebView(sharedWebView);

            Log.d(TAG, "✅ WebView criada com sucesso!");
            isInitializing = false;

            // Processa pendências
            processPendingActions();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar WebView: " + e.getMessage());
            e.printStackTrace();
            isInitializing = false;
        }
    }

    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setBackgroundColor(Color.BLACK);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "Carregando vídeo: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(TAG, "Erro na WebView: " + error.toString());
            }
        });
    }

    // Método auxiliar para processar ações pendentes após inicialização
    private void processPendingActions() {
        if (sharedWebView == null)
            return;

        // CRUCIAL: Anexar AO CONTAINER PRIMEIRO se houver um pendente
        if (pendingContainer != null && pendingLocation != null) {
            Log.d(TAG, "Processando attach pendente para: " + pendingLocation);
            attachToContainer(pendingContainer, pendingLocation);
            pendingContainer = null;
            pendingLocation = null;
        }

        // DEPOIS carregar o conteúdo
        if (pendingVideoId != null) {
            Log.d(TAG, "Processando vídeo pendente: " + pendingVideoId);
            loadVideo(pendingVideoId);
            pendingVideoId = null;
        } else {
            // Se não havia vídeo pendente, mas inicializou, carrega o default
            if (currentVideoId.isEmpty()) {
                currentVideoId = DEFAULT_VIDEO_ID;
                loadVideo(currentVideoId);
            }
        }
    }

    /**
     * Carrega Vídeo pelo ID no WebView.
     * 
     * @param videoId ID do vídeo Dailymotion (ex: k5imdGeVuZBajbE7oN8)
     */
    public void loadVideo(String videoId) {
        if (sharedWebView == null) {
            if (isInitializing) {
                Log.d(TAG, "⏳ WebView inicializando, vídeo enfileirado: " + videoId);
                pendingVideoId = videoId;
                return;
            }
            Log.e(TAG, "❌ WebView não inicializada! Chame initialize() primeiro.");
            return;
        }

        currentVideoId = videoId;

        // URL de Embed do Dailymotion
        // queue-enable=false: desabilita fila de reprodução automática
        // ui-start-screen-info=false: remove infos da tela inicial
        // sharing-enable=false: remove botões de compartilhamento
        // ui-logo=false: remove logo se possível (depende do plano)
        // autoplay=1 se deve tocar automaticamente (geralmente sim, controlamos pause
        // via JS ou ciclo de vida)
        String embedUrl = "https://www.dailymotion.com/embed/video/" + videoId +
                "?autoplay=1&mute=0&queue-enable=false&ui-start-screen-info=false&sharing-enable=false&ui-logo=false";

        try {
            sharedWebView.loadUrl(embedUrl);
            Log.d(TAG, "📹 Video carregado na WebView: " + videoId);
            isPlaying = true;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar vídeo: " + e.getMessage());
        }
    }

    /**
     * Anexa a WebView a um container específico.
     * 
     * @param container FrameLayout onde anexar a WebView
     * @param location  Localização (MAIN_FRAGMENT, RADIO_FRAGMENT, BACKGROUND)
     */
    public void attachToContainer(FrameLayout container, VideoLocation location) {
        if (sharedWebView == null) {
            if (isInitializing) {
                Log.d(TAG, "⏳ WebView inicializando, attach enfileirado para: " + location);
                pendingContainer = container;
                pendingLocation = location;
                return;
            }
            Log.e(TAG, "❌ WebView não inicializada!");
            return;
        }

        if (container == null) {
            Log.e(TAG, "❌ Container é null!");
            return;
        }

        // Se já está no container certo, não faz nada
        if (currentContainer == container && sharedWebView.getParent() == container) {
            return;
        }

        detachFromCurrentContainer();

        if (sharedWebView.getContext() instanceof MutableContextWrapper) {
            ((MutableContextWrapper) sharedWebView.getContext()).setBaseContext(container.getContext());
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        container.addView(sharedWebView, params);
        currentContainer = container;
        currentLocation = location;

        if (location == VideoLocation.BACKGROUND) {
            sharedWebView.setVisibility(View.INVISIBLE);
        } else {
            sharedWebView.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "🔄 WebView movida para " + location.name());

        if (stateListener != null) {
            stateListener.onLocationChanged(location);
        }
    }

    /**
     * Remove a WebView do container atual.
     */
    public void detachFromCurrentContainer() {
        if (sharedWebView == null)
            return;

        ViewGroup parent = (ViewGroup) sharedWebView.getParent();
        if (parent != null) {
            parent.removeView(sharedWebView);
            Log.d(TAG, "🔌 WebView desanexada de " + currentLocation.name());
        }

        if (sharedWebView != null && sharedWebView.getContext() instanceof MutableContextWrapper
                && applicationContext != null) {
            ((MutableContextWrapper) sharedWebView.getContext()).setBaseContext(applicationContext);
        }

        currentContainer = null;
        currentLocation = VideoLocation.DETACHED;
    }

    public void moveToBackground(FrameLayout backgroundContainer) {
        attachToContainer(backgroundContainer, VideoLocation.BACKGROUND);
    }

    public void moveToForeground(FrameLayout foregroundContainer, VideoLocation location) {
        if (location == VideoLocation.BACKGROUND) {
            throw new IllegalArgumentException("Use moveToBackground() para background");
        }
        attachToContainer(foregroundContainer, location);
    }

    public void play() {
        if (sharedWebView == null)
            return;

        // Tenta dar play via script se possível, ou resume a WebView
        sharedWebView.onResume();
        // Injeta script para play se necessário (depende da API do player carregado)
        // Para simplify, assumimos que onResume + autoplay url funciona ou script
        // básico
        sharedWebView.evaluateJavascript("if(player) player.play();", null);

        isPlaying = true;
        isVideoManuallyPaused = false;
        if (stateListener != null)
            stateListener.onPlaybackStateChanged(true);
    }

    public void pause(boolean manualPause) {
        if (sharedWebView == null)
            return;

        // Se for background, não chama onPause() da WebView para não parar o
        // processamento (áudio)
        // Mas se quisermos realmente pausar o vídeo:
        // sharedWebView.onPause();
        // Nota: onPause() na WebView pode parar timers e áudio.

        // Injeta script para pause
        sharedWebView.evaluateJavascript("if(player) player.pause();", null);

        isPlaying = false;
        isVideoManuallyPaused = manualPause;
        if (stateListener != null)
            stateListener.onPlaybackStateChanged(false);
    }

    public void pause() {
        pause(false);
    }

    public void stop() {
        if (sharedWebView == null)
            return;
        pause();
        currentVideoId = "";
        sharedWebView.loadUrl("about:blank");
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isVideoManuallyPaused() {
        return isVideoManuallyPaused;
    }

    public void resetManualPauseFlag() {
        isVideoManuallyPaused = false;
    }

    public String getCurrentVideoId() {
        return currentVideoId;
    }

    public void setStateListener(VideoStateListener listener) {
        this.stateListener = listener;
    }

    public void onPause() {
        if (sharedWebView == null)
            return;
        // Não pausamos a WebView automaticamente aqui para permitir play em background
        // se desejado
        // Apenas se for pausado manualmente
        if (!isVideoManuallyPaused) {
            // Se quisermos continuar tocando em background (áudio), não chamamos onPause da
            // WebView
            // Se quisermos economizar recurso quando o app sai de foco:
            // sharedWebView.onPause();
        }
    }

    public void onResume() {
        if (sharedWebView == null)
            return;

        sharedWebView.onResume();
        if (!isVideoManuallyPaused && isPlaying) {
            sharedWebView.evaluateJavascript("if(player) player.play();", null);
        }
    }

    public void destroy() {
        if (sharedWebView == null)
            return;

        sharedWebView.loadUrl("about:blank");
        detachFromCurrentContainer();
        sharedWebView.destroy();
        sharedWebView = null;

        currentLocation = VideoLocation.DETACHED;
        isPlaying = false;
        isVideoManuallyPaused = false;
        currentVideoId = "";
    }
}
