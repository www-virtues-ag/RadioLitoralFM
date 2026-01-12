package br.com.fivecom.litoralfm.ui.main.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import java.util.Calendar;
import java.util.List;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.services.ProgramacaoAPIService;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.core.WebViewCacheManager;
import br.com.fivecom.litoralfm.utils.LottieHelper;
import br.com.fivecom.litoralfm.utils.ServiceManager;
import br.com.fivecom.litoralfm.ui.video.FullscreenVideoActivity;

public class RadioFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RadioFragment";
    private static final long METADATA_UPDATE_INTERVAL = 10000;

    // WebViews
    // Video
    private FrameLayout videoContainer;
    private br.com.fivecom.litoralfm.video.SharedVideoManager videoManager;
    private ImageView btnExpandVideo;
    private WebView webView; // Para o banner

    // Botões do topo
    private ImageView btBack;
    private ImageView btMenu;
    private ImageView btHome;
    private ImageView btNotif;

    // TextViews de informações
    private TextView nameProgram;
    private TextView nameLocutor;
    private TextView musicName;

    // Botões
    private ImageView btnListenNow;
    // Bottom nav (agora são LinearLayout no include)
    private View btPromotion;
    private View btNews;
    private View btRadio;
    private View btProgram;
    private View btWpp;

    // Dados do programa e música
    private String currentProgram = "Radio Litoral FM";
    private String currentHost = "Na litoral eu to legal";
    private String currentSong = "Na Litoral eu tô legal";

    // Serviços e handlers
    private ProgramacaoAPIService programacaoAPIService;
    private Handler metadataHandler;
    private Runnable metadataRunnable;

    // Media controller
    private MediaControllerCompat controller;
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            // Pode adicionar lógica de estado de reprodução se necessário
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            updateMetadataFromController();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_radio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViews(view);
        if (videoContainer != null) {
            setupVideoManager();
        }
        setupWebViewBanner();
        setupClickListeners();
        initializePlaceholders();
        attachMediaController();
        startMetadataUpdates();

        // Aplica o estado de animação nos Lotties
        applyLottieAnimationState();
    }

    @Override
    public void onStart() {
        super.onStart();
        attachMediaController();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Retoma o vídeo se estiver visível
        // Anexa vídeo ao container deste fragment
        if (videoManager != null && videoContainer != null) {
            try {
                videoManager.moveToForeground(videoContainer,
                        br.com.fivecom.litoralfm.video.SharedVideoManager.VideoLocation.RADIO_FRAGMENT);

                // Se não foi pausado manualmente, garante que está tocando
                if (!videoManager.isVideoManuallyPaused()) {
                    videoManager.play();
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao mover vídeo para foreground", e);
            }
        }

        // Retoma o WebView do banner e verifica se precisa recarregar
        if (webView != null) {
            webView.onResume();
            // Se o WebView está visível mas perdeu o conteúdo, recarrega
            if (webView.getVisibility() == View.VISIBLE &&
                    (webView.getUrl() == null || webView.getUrl().isEmpty())) {
                Log.d(TAG, "🔄 WebView do banner perdeu conteúdo, recarregando...");
                setupWebViewBanner();
            }
        }
        // Atualiza o estado dos Lotties quando o fragment volta a ficar visível
        applyLottieAnimationState();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Controla o vídeo quando o fragment é pausado
        // Move vídeo para background se a activity ainda existe
        if (videoManager != null && getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.getBackgroundVideoContainer() != null) {
                try {
                    videoManager.moveToBackground(activity.getBackgroundVideoContainer());
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao mover vídeo para background", e);
                }
            }
        }

        // Pausa o WebView do banner quando o fragment é pausado
        if (webView != null && webView.getVisibility() == View.VISIBLE) {
            webView.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopMetadataUpdates();
        detachMediaController();
    }

    @Override
    public void onDestroyView() {
        // Cancela todos os handlers
        if (metadataHandler != null) {
            metadataHandler.removeCallbacksAndMessages(null);
            metadataHandler = null;
        }
        metadataRunnable = null;

        stopMetadataUpdates();

        // Pausa o player de vídeo mas não destrói (será destruído automaticamente
        // quando fragment for removido)
        // Não precisamos fazer nada com o vídeo aqui, o SharedVideoManager cuida disso
        // quando movemos para background no onPause

        // Pausa o WebView do banner mas NÃO destrói para manter o carregamento entre
        // navegações
        if (webView != null) {
            webView.onPause();
            // Não destrói aqui para manter o banner carregado entre navegações
            // webView = null;
        }

        super.onDestroyView();
    }

    private void initViews(@NonNull View view) {
        // WebView do player de vídeo
        // Container para vídeo compartilhado
        videoContainer = view.findViewById(R.id.video_container);
        btnExpandVideo = view.findViewById(R.id.btn_expand_video);

        // WebView do banner
        webView = view.findViewById(R.id.webView);

        // Botões do topo
        btBack = view.findViewById(R.id.bt_back);
        btMenu = view.findViewById(R.id.bt_menu);
        btHome = view.findViewById(R.id.bt_home);
        btNotif = view.findViewById(R.id.bt_notif);

        // TextViews de informações
        nameProgram = view.findViewById(R.id.name_program);
        nameLocutor = view.findViewById(R.id.name_locutor);
        musicName = view.findViewById(R.id.music_name);

        // Configurar TextViews para marquee (TextRun style)
        if (musicName != null) {
            musicName.setSelected(true);
            musicName.setFocusable(true);
            musicName.setFocusableInTouchMode(true);
        }
        if (nameProgram != null) {
            nameProgram.setSelected(true);
            nameProgram.setFocusable(true);
            nameProgram.setFocusableInTouchMode(true);
        }
        if (nameLocutor != null) {
            nameLocutor.setSelected(true);
            nameLocutor.setFocusable(true);
            nameLocutor.setFocusableInTouchMode(true);
        }

        // Botões
        btnListenNow = view.findViewById(R.id.btn_listen_now);
        btPromotion = view.findViewById(R.id.bt_promotion);
        btNews = view.findViewById(R.id.bt_news);
        btRadio = view.findViewById(R.id.bt_radio);
        btProgram = view.findViewById(R.id.bt_program);
        btWpp = view.findViewById(R.id.bt_whatsapp);

        // Inicializar ProgramacaoAPIService
        // Usa ServiceManager para compartilhar instância
        programacaoAPIService = ServiceManager.getProgramacaoService();
        programacaoAPIService.setCallback(new ProgramacaoAPIService.ProgramacaoAPICallback() {
            @Override
            public void onProgramasLoaded(List<ProgramaAPI> programas) {
                if (getActivity() == null)
                    return;
                requireActivity().runOnUiThread(() -> {
                    encontrarProgramaAtual(programas);
                });
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                // Pode ser usado para mostrar indicador de carregamento se necessário
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Erro ao buscar programação: " + errorMessage);
                if (getActivity() == null)
                    return;
                requireActivity().runOnUiThread(() -> {
                    currentProgram = "Radio Litoral FM";
                    currentHost = "Na litoral eu to legal";
                    updateProgramInfo();
                });
            }
        });

        // Handler para atualizações de metadados
        metadataHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Configura o SharedVideoManager
     * NÃO inicializa aqui - a MainActivity já inicializou
     * Apenas verifica se precisa carregar o vídeo
     */
    private void setupVideoManager() {
        videoManager = br.com.fivecom.litoralfm.video.SharedVideoManager.getInstance();

        // NÃO inicializa aqui - a MainActivity já inicializou no onCreate
        // Apenas verifica se precisa carregar o vídeo pela primeira vez
        String videoId = "k5imdGeVuZBajbE7oN8";

        if (videoManager.getCurrentVideoId().isEmpty() || !videoManager.getCurrentVideoId().equals(videoId)) {
            videoManager.loadVideo(videoId);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebViewBanner() {
        if (webView == null)
            return;

        webView.setBackgroundColor(0x00000000);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); // Usa cache quando disponível
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (getContext() != null && isAdded()) {
                    Intents.website_internal(getContext(), url);
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (view != null && isAdded()) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (view != null && isAdded()) {
                    view.setVisibility(View.GONE);
                }
            }
        });

        // Carrega a URL do banner
        if (data != null && data.radios != null && !data.radios.isEmpty() && Constants.ID >= 0
                && Constants.ID < data.radios.size()) {
            String pubUrl = null;

            // Usa a string "pub" do urls.xml
            try {
                int pubResId = getResources().getIdentifier("pub", "string", requireContext().getPackageName());
                if (pubResId != 0) {
                    String pubTemplate = getString(pubResId);
                    pubUrl = String.format(
                            Intents.decode(pubTemplate),
                            data.radios.get(Constants.ID).id,
                            "Android " + Build.VERSION.RELEASE,
                            Build.MANUFACTURER + " - " + Build.MODEL);
                    Log.d(TAG, "🔍 URL do banner formatada: " + pubUrl);
                } else {
                    Log.w(TAG, "⚠️ String resource 'pub' não encontrado");
                }
            } catch (Exception e) {
                Log.w(TAG, "❌ Erro ao obter string resource 'pub'", e);
            }

            // Se não encontrou o string resource, oculta a WebView
            if (pubUrl == null || pubUrl.isEmpty()) {
                Log.d(TAG, "⚠️ URL do banner não configurada. Ocultando WebView.");
                webView.setVisibility(View.GONE);
                return;
            }

            // Verifica se a URL já está carregada na WebView
            String currentUrl = webView.getUrl();
            // Se já tem conteúdo e a URL é a mesma, não precisa recarregar
            if (currentUrl != null && !currentUrl.isEmpty() && currentUrl.equals(pubUrl)) {
                Log.d(TAG, "✅ WebView do banner já tem o conteúdo correto, mantendo carregado: " + currentUrl);
                return; // Já tem o conteúdo correto, não precisa recarregar
            }
            boolean urlChanged = !pubUrl.equals(currentUrl);

            // Verifica se precisa recarregar baseado no cache manager
            boolean shouldReload = WebViewCacheManager.shouldReload(requireContext(), pubUrl);

            // Só recarrega se a URL mudou ou se o cache expirou
            if (urlChanged || shouldReload) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(pubUrl);
                Log.d(TAG, "✅ WebView do banner carregando URL: " + pubUrl
                        + (urlChanged ? " (URL mudou)" : " (cache expirado)"));
            } else {
                // URL já está carregada e cache ainda válido, apenas torna visível
                webView.setVisibility(View.VISIBLE);
                Log.d(TAG, "⏭️ WebView usando cache para URL: " + pubUrl);
            }
        } else {
            Log.w(TAG, "⚠️ Dados da rádio não disponíveis para carregar o banner");
            webView.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        if (btBack != null)
            btBack.setOnClickListener(this);
        if (btMenu != null)
            btMenu.setOnClickListener(this);
        if (btHome != null)
            btHome.setOnClickListener(this);
        if (btNotif != null)
            btNotif.setOnClickListener(this);
        if (btnListenNow != null)
            btnListenNow.setOnClickListener(this);
        if (btPromotion != null)
            btPromotion.setOnClickListener(this);
        if (btNews != null)
            btNews.setOnClickListener(this);
        if (btRadio != null)
            btRadio.setOnClickListener(this);
        if (btProgram != null)
            btProgram.setOnClickListener(this);
        if (btWpp != null)
            btWpp.setOnClickListener(this);
        if (btnExpandVideo != null)
            btnExpandVideo.setOnClickListener(this);

    }

    private void initializePlaceholders() {
        // Define os valores padrão (placeholders) para os TextViews
        updateProgramInfo();
        updateSongInfo();
    }

    // ===================== METADADOS E PROGRAMA =====================

    private void startMetadataUpdates() {
        updateMetadata("Radio Litoral FM", "Na litoral eu to legal", "Na Litoral eu tô legal");

        metadataRunnable = new Runnable() {
            @Override
            public void run() {
                fetchCurrentProgram();
                fetchCurrentSong();
                metadataHandler.postDelayed(this, METADATA_UPDATE_INTERVAL);
            }
        };
        metadataHandler.post(metadataRunnable);
    }

    private void stopMetadataUpdates() {
        if (metadataHandler != null && metadataRunnable != null) {
            metadataHandler.removeCallbacks(metadataRunnable);
        }
    }

    private void fetchCurrentProgram() {
        if (programacaoAPIService == null || data == null || data.radios == null)
            return;

        try {
            // Usa Constants.ID como índice do array
            if (Constants.ID < 0 || Constants.ID >= data.radios.size()) {
                Log.w(TAG, "⚠️ Índice Constants.ID inválido: " + Constants.ID);
                return;
            }

            // Pega o ID da rádio (String) para passar na API
            String radioIdForApi = data.radios.get(Constants.ID).id;

            Log.d(TAG, "Consultando programa atual - Radio ID: " + radioIdForApi);

            // Busca todos os programas (sem filtro de dia) para encontrar o programa atual
            programacaoAPIService.fetchProgramacao(radioIdForApi, "");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao montar requisição do programa atual", e);
            currentProgram = "Radio Litoral FM";
            currentHost = "Na litoral eu to legal";
            updateProgramInfo();
        }
    }

    /**
     * Encontra o programa atual baseado no horário e dia da semana
     */
    private void encontrarProgramaAtual(List<ProgramaAPI> programas) {
        if (programas == null || programas.isEmpty()) {
            currentProgram = "Radio Litoral FM";
            currentHost = "Na litoral eu to legal";
            updateProgramInfo();
            Log.d(TAG, "⚠️ Nenhum programa encontrado, usando placeholders");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int diaSemanaAtual = calendar.get(Calendar.DAY_OF_WEEK);
        int horaAtual = calendar.get(Calendar.HOUR_OF_DAY);
        int minutoAtual = calendar.get(Calendar.MINUTE);
        int minutosAtuais = horaAtual * 60 + minutoAtual;

        ProgramaAPI programaAtual = null;

        // Converte Calendar.DAY_OF_WEEK para o formato da API (1=Domingo, 2=Segunda,
        // etc.)
        // A API usa: 1=Domingo, 2=Segunda, 3=Terça, 4=Quarta, 5=Quinta, 6=Sexta,
        // 7=Sábado
        String diaSemanaAPI = String.valueOf(diaSemanaAtual);

        for (ProgramaAPI programa : programas) {
            // Verifica se o programa é do dia atual
            if (programa.getNrDiaSemana() == null || !programa.getNrDiaSemana().equals(diaSemanaAPI)) {
                continue;
            }

            // Parse do horário de início
            String hrInicio = programa.getHrInicio();
            String hrFinal = programa.getHrFinal();

            if (hrInicio == null || hrFinal == null) {
                continue;
            }

            try {
                // Formato esperado: "HH:mm" ou "HHmm"
                int horaInicio = 0;
                int minutoInicio = 0;
                int horaFinal = 0;
                int minutoFinal = 0;

                // Parse do horário de início
                if (hrInicio.contains(":")) {
                    String[] parts = hrInicio.split(":");
                    horaInicio = Integer.parseInt(parts[0]);
                    minutoInicio = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                } else if (hrInicio.length() >= 2) {
                    horaInicio = Integer.parseInt(hrInicio.substring(0, Math.min(2, hrInicio.length())));
                    if (hrInicio.length() > 2) {
                        minutoInicio = Integer.parseInt(hrInicio.substring(2));
                    }
                }

                // Parse do horário final
                if (hrFinal.contains(":")) {
                    String[] parts = hrFinal.split(":");
                    horaFinal = Integer.parseInt(parts[0]);
                    minutoFinal = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                } else if (hrFinal.length() >= 2) {
                    horaFinal = Integer.parseInt(hrFinal.substring(0, Math.min(2, hrFinal.length())));
                    if (hrFinal.length() > 2) {
                        minutoFinal = Integer.parseInt(hrFinal.substring(2));
                    }
                }

                int minutosInicio = horaInicio * 60 + minutoInicio;
                int minutosFinal = horaFinal * 60 + minutoFinal;

                // Verifica se o horário atual está dentro do intervalo do programa
                // Se o programa termina no dia seguinte (ex: 23:00 - 01:00), ajusta
                if (minutosFinal < minutosInicio) {
                    // Programa que cruza a meia-noite
                    if (minutosAtuais >= minutosInicio || minutosAtuais < minutosFinal) {
                        programaAtual = programa;
                        break;
                    }
                } else {
                    // Programa normal no mesmo dia
                    if (minutosAtuais >= minutosInicio && minutosAtuais < minutosFinal) {
                        programaAtual = programa;
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Erro ao parsear horário: " + hrInicio + " - " + hrFinal, e);
                continue;
            }
        }

        // Se não encontrou programa, tenta pegar o primeiro do dia
        if (programaAtual == null) {
            for (ProgramaAPI programa : programas) {
                if (programa.getNrDiaSemana() != null && programa.getNrDiaSemana().equals(diaSemanaAPI)) {
                    programaAtual = programa;
                    break;
                }
            }
        }

        if (programaAtual != null) {
            currentProgram = (programaAtual.getTitle() != null && !programaAtual.getTitle().isEmpty())
                    ? programaAtual.getTitle()
                    : "Radio Litoral FM";

            currentHost = (programaAtual.getNmLocutor() != null && !programaAtual.getNmLocutor().isEmpty())
                    ? programaAtual.getNmLocutor()
                    : "Na litoral eu to legal";

            Log.d(TAG, "✅ Programa atual: " + currentProgram +
                    " com " + currentHost +
                    " (" + programaAtual.getHrInicio() +
                    " até " + programaAtual.getHrFinal() + ")");
        } else {
            currentProgram = "Radio Litoral FM";
            currentHost = "Na litoral eu to legal";
            Log.d(TAG, "⚠️ Nenhum programa no ar, usando placeholders");
        }

        updateProgramInfo();
    }

    private void fetchCurrentSong() {
        if (controller != null && controller.getMetadata() != null) {
            String artist = controller.getMetadata().getString("artist");
            String title = controller.getMetadata().getString("title");

            if (artist != null && title != null &&
                    !artist.isEmpty() && !title.isEmpty()) {
                currentSong = artist + " - " + title;
            } else if (title != null && !title.isEmpty()) {
                currentSong = title;
            } else {
                currentSong = "Na Litoral eu tô legal";
            }

            updateSongInfo();
        }
    }

    private void updateMetadata(String program, String host, String song) {
        currentProgram = program;
        currentHost = host;
        currentSong = song;

        updateProgramInfo();
        updateSongInfo();
    }

    private void updateProgramInfo() {
        if (nameProgram != null) {
            nameProgram.setText(
                    currentProgram != null && !currentProgram.isEmpty()
                            ? currentProgram
                            : "Radio Litoral FM");
            nameProgram.setSelected(true);
        }

        if (nameLocutor != null) {
            nameLocutor.setText(
                    currentHost != null && !currentHost.isEmpty()
                            ? currentHost
                            : "Na litoral eu to legal");
            nameLocutor.setSelected(true);
        }

        Log.d(TAG, "📻 Programa atualizado: " + currentProgram + " - " + currentHost);
    }

    private void updateSongInfo() {
        if (musicName != null) {
            String textToShow = currentSong != null && !currentSong.isEmpty()
                    ? currentSong
                    : "Na Litoral eu tô legal";

            musicName.setText(textToShow);

            // Garante que o TextView tenha foco para o marquee funcionar
            if (textToShow.length() > 0) {
                musicName.setSelected(true);
                musicName.requestFocus();
            }
        }

        Log.d(TAG, "🎵 Música atualizada: " + currentSong);
    }

    // ===================== MEDIA CONTROLLER =====================

    private void attachMediaController() {
        try {
            controller = MediaControllerCompat.getMediaController(requireActivity());
            if (controller != null) {
                controller.registerCallback(controllerCallback);
                updateMetadataFromController();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao anexar MediaController", e);
        }
    }

    private void detachMediaController() {
        try {
            if (controller != null) {
                controller.unregisterCallback(controllerCallback);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desanexar MediaController", e);
        } finally {
            controller = null;
        }
    }

    private void updateMetadataFromController() {
        if (controller == null || controller.getMetadata() == null) {
            // Se não há metadados, atualiza com os valores atuais
            updateProgramInfo();
            updateSongInfo();
            return;
        }

        // Busca os metadados RDS do streaming
        String music = controller.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE);
        String artist = controller.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE);

        // Atualiza a música se houver metadados
        if (music != null && !music.isEmpty() && !music.trim().isEmpty()) {
            if (artist != null && !artist.isEmpty() && !artist.trim().isEmpty()) {
                currentSong = artist + " - " + music;
            } else {
                currentSong = music;
            }
            updateSongInfo();
        } else {
            // Se não há música, mantém o placeholder
            currentSong = "Na Litoral eu tô legal";
            updateSongInfo();
        }

        // Os dados de programa e locutor vêm da API de programação, não dos metadados
        // RDS
        // Eles são atualizados pelo fetchCurrentProgram() periodicamente
    }

    // ===================== CLIQUES =====================

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bt_back) {
            // Usa handleBackPress para navegação correta com back stack
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).handleBackPress();
            }
        } else if (id == R.id.bt_menu) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openMenu();
            }
        } else if (id == R.id.bt_home) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
            }
        } else if (id == R.id.bt_notif) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTF_PROGRAM);
            }
        } else if (id == R.id.btn_listen_now) {
            // Navegar para AudioFragment
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.AUDIO);
            }
        } else if (id == R.id.bt_promotion) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.PROMOTION);
            }
        } else if (id == R.id.bt_news) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NEWS);
            }
        } else if (id == R.id.bt_radio) {
            // Já está na tela de rádio (vídeo)
        } else if (id == R.id.bt_program) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.SCHEDULE);
            }
        } else if (id == R.id.bt_whatsapp) {
            // Abrir WhatsApp
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openWhatsApp();
            }
        } else if (id == R.id.btn_expand_video) {
            if (getActivity() != null) {
                startActivity(new Intent(requireContext(), FullscreenVideoActivity.class));
            }
        }
    }

    /**
     * Aplica o estado de animação nos Lotties baseado no modo estático
     */
    private void applyLottieAnimationState() {
        if (getView() != null) {
            LottieHelper.setAnimationStateForLotties(
                    getView(),
                    requireContext(),
                    R.id.lottie_radio1,
                    R.id.lottie_radio2);
        }
    }
}
