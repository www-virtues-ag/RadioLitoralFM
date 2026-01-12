package br.com.fivecom.litoralfm.ui.main.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;
import br.com.fivecom.litoralfm.ui.video.FullscreenVideoActivity;
import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.Data;
import br.com.fivecom.litoralfm.models.agenda.AgendaItem;
import br.com.fivecom.litoralfm.models.news.Noticia;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.services.AgendaService;
import br.com.fivecom.litoralfm.services.ProgramacaoAPIService;
import br.com.fivecom.litoralfm.ui.agenda.AgendaAdapter;
import br.com.fivecom.litoralfm.ui.agenda.DetailAgendaActivity;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.ui.main.MediaActivity;
import br.com.fivecom.litoralfm.utils.TimeParser;
import br.com.fivecom.litoralfm.utils.RadioInfoCache;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.ui.main.adapter.MainNewsAdapter;
import br.com.fivecom.litoralfm.ui.news.network.NoticiaService;
import br.com.fivecom.litoralfm.ui.news.network.NoticiasCallback;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.core.WebViewCacheManager;
import br.com.fivecom.litoralfm.utils.LottieHelper;
import br.com.fivecom.litoralfm.utils.ServiceManager;
import br.com.fivecom.litoralfm.utils.PrefsCache;
import br.com.fivecom.litoralfm.utils.RadioInfoCache;

@UnstableApi
public class MainFragment extends Fragment
        implements View.OnClickListener, br.com.fivecom.litoralfm.ui.main.MediaStateListener {

    // Topo
    private ImageView btMenu;
    private ImageView btNotif;
    private ImageView btBackChoose;

    // Player mini
    private ImageView btPlay;
    private TextView musicName;
    private TextView artistName;
    private TextView programNameMain;
    private TextView locutorNameMain;

    // Data / hora / dia
    private TextView numberHour;
    private TextView txtDate;
    private TextView nmDay;
    private TextView txtCity;
    private RelativeLayout rlCity;

    // WebView do banner (já no layout)
    private WebView webView;

    // Video
    private FrameLayout videoContainer;
    private br.com.fivecom.litoralfm.video.SharedVideoManager videoManager;
    private ImageView imgCoverMain;
    private androidx.cardview.widget.CardView cardViewVideo;
    private ImageView btnExpandVideo;

    // Bottom nav (agora são LinearLayout no include)
    private View btnNav1, btnNav2, btnNav3, btnNav4, btnNav5;

    // News button (btn_news)
    private ImageView btnNews;

    // Agenda button
    private ImageView btnAgenda;

    // News RecyclerView
    private RecyclerView rvNews;
    private MainNewsAdapter newsAdapter;
    private ProgressBar progressNews;

    // Agenda RecyclerView
    private RecyclerView rvAgenda;
    private AgendaAdapter agendaAdapter;
    private ProgressBar progressAgenda;
    private TextView txtEmptyAgenda;
    private FrameLayout flAgenda;
    private AgendaService agendaService;
    private List<AgendaItem> agendaList = new ArrayList<>();

    // Programação
    private ProgramacaoAPIService programacaoAPIService;
    private String currentProgramName = "";
    private String currentLocutorName = "";
    private String currentMusicName = "";
    private String currentArtistName = "";

    // ProgressBar para o player
    private ProgressBar progressBar;

    // Relógio
    private Handler clockHandler;
    private Runnable clockRunnable;

    // Atualização automática de notícias
    private Handler newsUpdateHandler;
    private Runnable newsUpdateRunnable;
    private static final long NEWS_UPDATE_INTERVAL_MS = 10 * 60 * 1000; // 10 minutos

    // Media controller - obtido via MediaActivity
    private MediaControllerCompat controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
        setupWebView();
        if (videoContainer != null) {
            setupVideoManager();
        }
        updateVideoVisibility();
        setupNewsRecyclerView(view);
        setupAgendaRecyclerView(view);
        setupProgramacaoService();
        startClock();
        startNewsAutoUpdate();

        // Carrega dados apenas se não foram carregados anteriormente
        // (evita recarregar em rotações de tela)
        if (savedInstanceState == null) {
            loadNews();
            loadAgenda();
            fetchCurrentProgram();
        } else {
            // Restaura dados do cache se disponível
            List<Noticia> cachedNews = NoticiaService.getInstance().getCachedNoticias(null);
            if (cachedNews != null && !cachedNews.isEmpty() && newsAdapter != null) {
                newsAdapter.setNoticias(cachedNews);
            }
        }

        // Aplica o estado de animação nos Lotties
        applyLottieAnimationState();
    }

    /**
     * Aplica o estado de animação nos Lotties baseado no modo estático
     */
    private void applyLottieAnimationState() {
        if (getView() != null) {
            LottieHelper.setAnimationStateForLotties(
                    getView(),
                    requireContext(),
                    R.id.lottie_main);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Registra como listener do MediaActivity para receber atualizações de playback
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            // PRIMEIRO obtém o controller para evitar race condition nos callbacks
            controller = activity.mController;
            // DEPOIS registra o listener
            activity.addMediaStateListener(this);
        }
        // Verifica se a rádio foi alterada e atualiza Constants.ID
        checkAndUpdateRadioSelection();
        // Atualiza a cidade
        updateCityName();
        // Busca o programa atual apenas se necessário (cache já foi verificado)
        // fetchCurrentProgram() agora verifica cache internamente
        fetchCurrentProgram();
    }

    /**
     * Verifica se a rádio foi alterada e atualiza Constants.ID
     */
    private void checkAndUpdateRadioSelection() {
        if (data == null || data.radios == null || data.radios.isEmpty()) {
            return;
        }

        // Lê a última rádio selecionada
        android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        int savedRadioId = prefs.getInt("selected_radio_id", -1);

        if (savedRadioId != -1) {
            // Encontra o índice da rádio no array baseado no ID
            String radioIdStr = String.valueOf(savedRadioId);
            for (int i = 0; i < data.radios.size(); i++) {
                if (data.radios.get(i).id != null && data.radios.get(i).id.equals(radioIdStr)) {
                    Constants.ID = i;
                    android.util.Log.d("MainFragment", "✅ Rádio atualizada - ID: " + savedRadioId + ", Índice: " + i);
                    break;
                }
            }
            // Atualiza a cidade quando a rádio muda
            updateCityName();
            // Atualiza a visibilidade do vídeo/capa quando a rádio muda
            updateVideoVisibility();
            // Busca o programa atual da nova rádio
            fetchCurrentProgram();
        }
    }

    /**
     * Retorna o nome da cidade baseado no ID da rádio.
     * OTIMIZADO: Usa RadioInfoCache com lookup O(1)
     */
    private String getCityNameByRadioId(int radioId) {
        return RadioInfoCache.getCityName(radioId);
    }

    /**
     * Atualiza o nome da cidade no TextView baseado na rádio selecionada
     */
    private void updateCityName() {
        if (txtCity == null)
            return;

        try {
            // Usa cache para evitar leituras repetidas de SharedPreferences
            int savedRadioId = PrefsCache.getRadioId(requireContext());

            String cityName = getCityNameByRadioId(savedRadioId);
            txtCity.setText(cityName);
            android.util.Log.d("MainFragment",
                    "🏙️ Cidade atualizada: " + cityName + " (Rádio ID: " + savedRadioId + ")");
        } catch (Exception e) {
            android.util.Log.e("MainFragment", "Erro ao atualizar cidade: " + e.getMessage());
            // Fallback para VITÓRIA em caso de erro
            txtCity.setText("VITÓRIA - ES");
        }
    }

    @Override
    public void onStop() {
        videoContainer.removeAllViews();
        // Desregistra como listener do MediaActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).removeMediaStateListener(this);
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Cancela handlers quando fragment é pausado
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
        // Pausa atualização automática de notícias quando fragment é pausado
        stopNewsAutoUpdate();

        // Controla o vídeo quando o fragment é pausado
        // Move vídeo para background se a activity ainda existe
        if (videoManager != null && getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.getBackgroundVideoContainer() != null) {
                try {
                    videoManager.moveToBackground(activity.getBackgroundVideoContainer());
                } catch (Exception e) {
                    Log.e("MainFragment", "Erro ao mover vídeo para background", e);
                }
            }
        }

        // Pausa o WebView do banner quando o fragment é pausado
        if (webView != null && webView.getVisibility() == View.VISIBLE) {
            webView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Retoma atualização automática de notícias quando fragment volta a ficar
        // visível
        startNewsAutoUpdate();
        // Atualiza o estado dos Lotties quando o fragment volta a ficar visível
        applyLottieAnimationState();
        // Atualiza a cidade quando o fragment volta a ser visível
        updateCityName();
        // Atualiza a visibilidade do vídeo/capa quando o fragment volta a ser visível
        updateVideoVisibility();
        // Retoma o player de mídia de áudio (rádio)
        // Sincroniza o estado da UI quando o fragment volta a ficar visível
        if (getActivity() instanceof MainActivity) {
            controller = ((MainActivity) getActivity()).mController;
            if (controller != null && controller.getPlaybackState() != null) {
                syncPlaybackState(controller.getPlaybackState());
            }
        }

        // Anexa vídeo ao container deste fragment
        if (videoManager != null && videoContainer != null) {
            try {
                videoManager.moveToForeground(videoContainer,
                        br.com.fivecom.litoralfm.video.SharedVideoManager.VideoLocation.MAIN_FRAGMENT);

                // Se não foi pausado manualmente, garante que está tocando
                if (!videoManager.isVideoManuallyPaused()) {
                    videoManager.play();
                }
            } catch (Exception e) {
                Log.e("MainFragment", "Erro ao mover vídeo para foreground", e);
            }
        }

        // Atualiza a barra de progresso do áudio
        updateProgress();

        // Retoma o WebView do banner e verifica se precisa recarregar
        if (webView != null) {
            webView.onResume();
            // Se o WebView está visível mas perdeu o conteúdo, recarrega
            if (webView.getVisibility() == View.VISIBLE &&
                    (webView.getUrl() == null || webView.getUrl().isEmpty())) {
                Log.d("MainFragment", "🔄 WebView do banner perdeu conteúdo, recarregando...");
                setupWebView();
            }
        }
    }

    private void updateProgress() {
        if (progressBar == null || controller == null || controller.getPlaybackState() == null)
            return;

        int state = controller.getPlaybackState().getState();
        if (state == PlaybackStateCompat.STATE_BUFFERING) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancela todos os handlers
        if (clockHandler != null) {
            clockHandler.removeCallbacksAndMessages(null);
            clockHandler = null;
        }
        clockRunnable = null;

        // Cancela atualização automática de notícias
        stopNewsAutoUpdate();
        if (newsUpdateHandler != null) {
            newsUpdateHandler.removeCallbacksAndMessages(null);
            newsUpdateHandler = null;
        }
        newsUpdateRunnable = null;

        // Pausa o vídeo do fragment ao destruir a view
        // O vídeo continua tocando na WebView de background da Activity
        if (videoManager != null && getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.getBackgroundVideoContainer() != null) {
                videoManager.moveToBackground(activity.getBackgroundVideoContainer());
            }
        }

    }

    // ===================== INIT =====================

    private void initViews(@NonNull View view) {
        // Topo
        btMenu = view.findViewById(R.id.bt_menu);
        btNotif = view.findViewById(R.id.bt_notif);
        btBackChoose = view.findViewById(R.id.bt_back_choose);

        // Mini player
        btPlay = view.findViewById(R.id.bt_play);
        musicName = view.findViewById(R.id.music_name);
        programNameMain = view.findViewById(R.id.program_name_main);
        locutorNameMain = view.findViewById(R.id.locutor_name_main);
        progressBar = view.findViewById(R.id.progress_bar);

        // Configurar TextViews para marquee (TextRun style)
        if (musicName != null) {
            musicName.setSelected(true);
            musicName.setFocusable(true);
            musicName.setFocusableInTouchMode(true);
        }
        if (artistName != null) {
            artistName.setSelected(true);
            artistName.setFocusable(true);
            artistName.setFocusableInTouchMode(true);
        }

        // Data / hora / dia
        numberHour = view.findViewById(R.id.number_hour);
        txtDate = view.findViewById(R.id.txt_date);
        nmDay = view.findViewById(R.id.nm_day);
        txtCity = view.findViewById(R.id.txt_city);
        rlCity = view.findViewById(R.id.rl_city);

        // Container para vídeo compartilhado
        videoContainer = view.findViewById(R.id.video_container);

        // WebView banner
        webView = view.findViewById(R.id.webView);

        // WebView player de vídeo e ImageView da capa
        if (videoContainer != null) {
            setupVideoManager();
        }
        imgCoverMain = view.findViewById(R.id.img_cover_main);
        cardViewVideo = view.findViewById(R.id.cv_video);
        btnExpandVideo = view.findViewById(R.id.btn_expand_video);
        btnExpandVideo = view.findViewById(R.id.btn_expand_video);

        // Bottom nav
        btnNav1 = view.findViewById(R.id.bt_promotion);
        btnNav2 = view.findViewById(R.id.bt_news);
        btnNav3 = view.findViewById(R.id.bt_radio);
        btnNav4 = view.findViewById(R.id.bt_program);
        btnNav5 = view.findViewById(R.id.bt_whatsapp);

        // News button (btn_news)
        btnNews = view.findViewById(R.id.btn_news);

        // Agenda button
        btnAgenda = view.findViewById(R.id.btn_agenda);

        // Agenda RecyclerView
        rvAgenda = view.findViewById(R.id.rv_agenda);
        progressAgenda = view.findViewById(R.id.progress_agenda);
        txtEmptyAgenda = view.findViewById(R.id.txt_empty_agenda);
        flAgenda = view.findViewById(R.id.fl_agenda);

        // Inicializar AgendaService (usando ServiceManager para compartilhar instância)
        agendaService = ServiceManager.getAgendaService();

        // Handler do relógio
        clockHandler = new Handler(Looper.getMainLooper());

        // Handler para atualização automática de notícias
        newsUpdateHandler = new Handler(Looper.getMainLooper());

        // Atualiza a cidade baseada na rádio selecionada
        updateCityName();
    }

    private void setupClickListeners() {
        if (btMenu != null)
            btMenu.setOnClickListener(this);
        if (btNotif != null)
            btNotif.setOnClickListener(this);
        if (btBackChoose != null)
            btBackChoose.setOnClickListener(this);
        if (btPlay != null)
            btPlay.setOnClickListener(this);

        if (btnNav1 != null)
            btnNav1.setOnClickListener(this);
        if (btnNav2 != null)
            btnNav2.setOnClickListener(this);
        if (btnNav3 != null)
            btnNav3.setOnClickListener(this);
        if (btnNav4 != null)
            btnNav4.setOnClickListener(this);
        if (btnNav5 != null)
            btnNav5.setOnClickListener(this);
        if (btnNews != null)
            btnNews.setOnClickListener(this);
        if (btnAgenda != null)
            btnAgenda.setOnClickListener(this);
        if (rlCity != null)
            rlCity.setOnClickListener(this);
        if (btnExpandVideo != null)
            btnExpandVideo.setOnClickListener(this);

    }

    /**
     * Configura a WebView do banner conforme o exemplo fornecido
     */
    private void setupWebView() {
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
                    Log.d("MainFragment", "🔍 Template decodificado: " + Intents.decode(pubTemplate));
                    Log.d("MainFragment", "🔍 URL formatada: " + pubUrl);
                } else {
                    Log.w("MainFragment", "⚠️ String resource 'pub' não encontrado");
                }
            } catch (Exception e) {
                Log.w("MainFragment", "❌ Erro ao obter string resource 'pub'", e);
            }

            // Se não encontrou o string resource, oculta a WebView
            if (pubUrl == null || pubUrl.isEmpty()) {
                Log.d("MainFragment", "⚠️ URL do banner não configurada. Ocultando WebView.");
                webView.setVisibility(View.GONE);
                return;
            }

            // Verifica se a URL já está carregada na WebView
            String currentUrl = webView.getUrl();
            // Se já tem conteúdo e a URL é a mesma, não precisa recarregar
            if (currentUrl != null && !currentUrl.isEmpty() && currentUrl.equals(pubUrl)) {
                Log.d("MainFragment",
                        "✅ WebView do banner já tem o conteúdo correto, mantendo carregado: " + currentUrl);
                return; // Já tem o conteúdo correto, não precisa recarregar
            }
            boolean urlChanged = !pubUrl.equals(currentUrl);

            // Verifica se precisa recarregar baseado no cache manager
            boolean shouldReload = WebViewCacheManager.shouldReload(requireContext(), pubUrl);

            // Só recarrega se a URL mudou ou se o cache expirou
            if (urlChanged || shouldReload) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(pubUrl);
                Log.d("MainFragment",
                        "✅ WebView carregando URL: " + pubUrl + (urlChanged ? " (URL mudou)" : " (cache expirado)"));
            } else {
                // URL já está carregada e cache ainda válido, apenas torna visível
                webView.setVisibility(View.VISIBLE);
                Log.d("MainFragment", "⏭️ WebView usando cache para URL: " + pubUrl);
            }
        } else {
            Log.w("MainFragment", "⚠️ Dados da rádio não disponíveis para carregar o banner (ID: " + Constants.ID
                    + ", radios size: " + (data != null && data.radios != null ? data.radios.size() : 0) + ")");
            webView.setVisibility(View.GONE);
        }
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
        // ID do vídeo Litoral FM
        String videoId = "k5imdGeVuZBajbE7oN8";

        if (videoManager.getCurrentVideoId().isEmpty() || !videoManager.getCurrentVideoId().equals(videoId)) {
            videoManager.loadVideo(videoId);
        }
    }

    /**
     * Carrega o player de vídeo Dailymotion
     */

    /**
     * Atualiza a visibilidade do vídeo e da capa baseado no ID da rádio
     * Mostra o vídeo apenas para Grande Vitória (ID 10223)
     * Mostra a capa para as outras rádios
     */
    private void updateVideoVisibility() {
        if (videoContainer == null || imgCoverMain == null || cardViewVideo == null)
            return;

        try {
            // Usa cache para evitar leituras repetidas de SharedPreferences
            int savedRadioId = PrefsCache.getRadioId(requireContext());

            // Mostra o vídeo apenas para Grande Vitória (ID 10223)
            boolean isGrandeVitoria = (savedRadioId == 10223);

            if (isGrandeVitoria) {
                // Mostra o CardView com vídeo e oculta a capa
                cardViewVideo.setVisibility(View.VISIBLE);
                videoContainer.setVisibility(View.VISIBLE);
                imgCoverMain.setVisibility(View.GONE);

                // Move para foreground se o vídeo manager estiver disponível
                if (videoManager != null) {
                    videoManager.moveToForeground(videoContainer,
                            br.com.fivecom.litoralfm.video.SharedVideoManager.VideoLocation.MAIN_FRAGMENT);

                    // Dá play se não estiver pausado manualmente
                    if (!videoManager.isVideoManuallyPaused()) {
                        videoManager.play();
                    }
                }

                Log.d("MainFragment", "📺 Vídeo VISÍVEL, capa OCULTA (Rádio ID: " + savedRadioId + ")");
            } else {
                // Mostra a capa e oculta o CardView com vídeo
                cardViewVideo.setVisibility(View.GONE);
                videoContainer.setVisibility(View.GONE);
                imgCoverMain.setVisibility(View.VISIBLE);

                // Move vídeo para background
                if (videoManager != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity.getBackgroundVideoContainer() != null) {
                        videoManager.moveToBackground(activity.getBackgroundVideoContainer());
                    }
                }

                Log.d("MainFragment", "🖼️ Capa VISÍVEL, CardView vídeo OCULTO (Rádio ID: " + savedRadioId + ")");
            }
        } catch (Exception e) {
            Log.e("MainFragment", "Erro ao atualizar visibilidade do vídeo/capa: " + e.getMessage());
            // Em caso de erro, mostra a capa por padrão
            if (cardViewVideo != null)
                cardViewVideo.setVisibility(View.GONE);
            if (videoContainer != null)
                videoContainer.setVisibility(View.GONE);
            if (imgCoverMain != null)
                imgCoverMain.setVisibility(View.VISIBLE);
        }
    }

    // ===================== AGENDA RECYCLERVIEW =====================

    private void setupAgendaRecyclerView(@NonNull View view) {
        if (rvAgenda != null) {
            // Layout horizontal (mesmo estilo da RecyclerView de notícias)
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false);
            rvAgenda.setLayoutManager(layoutManager);

            // Adapter
            agendaAdapter = new AgendaAdapter(requireContext(), agendaList);
            rvAgenda.setAdapter(agendaAdapter);

            // Set click listener para abrir detalhes
            agendaAdapter.setOnAgendaItemClickListener(this::openAgendaDetail);
        }
    }

    private void loadAgenda() {
        showAgendaLoading(true);
        agendaService.fetchAgenda(new AgendaService.AgendaCallback() {
            @Override
            public void onSuccess(List<AgendaItem> agenda) {
                if (isAdded() && agendaAdapter != null) {
                    agendaList.clear();
                    agendaList.addAll(agenda);
                    agendaAdapter.updateAgenda(agendaList);
                    showAgendaLoading(false);
                    updateAgendaEmptyState();
                    Log.d("MainFragment", "✅ Agendas carregadas: " + agenda.size());
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    showAgendaLoading(false);
                    updateAgendaEmptyState();
                    Log.e("MainFragment", "❌ Erro ao carregar agendas: " + errorMessage);
                }
            }
        });
    }

    private void showAgendaLoading(boolean show) {
        if (progressAgenda != null) {
            progressAgenda.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvAgenda != null) {
            rvAgenda.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        // Esconder mensagem de lista vazia durante o carregamento
        if (txtEmptyAgenda != null) {
            txtEmptyAgenda.setVisibility(View.GONE);
        }
    }

    private void updateAgendaEmptyState() {
        boolean isEmpty = agendaList.isEmpty();

        if (txtEmptyAgenda != null) {
            txtEmptyAgenda.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (rvAgenda != null) {
            rvAgenda.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // Ajusta a altura do FrameLayout quando a lista estiver vazia
        if (flAgenda != null) {
            ViewGroup.LayoutParams params = flAgenda.getLayoutParams();
            if (params != null) {
                if (isEmpty) {
                    // Altura reduzida para 40sdp quando vazio
                    params.height = (int) getResources().getDimension(com.appdimens.sdps.R.dimen._40sdp);
                } else {
                    // Altura normal de 120sdp quando tem conteúdo
                    params.height = (int) getResources().getDimension(com.appdimens.sdps.R.dimen._120sdp);
                }
                flAgenda.setLayoutParams(params);
            }
        }
    }

    private void openAgendaDetail(AgendaItem item) {
        Intent intent = new Intent(requireContext(), DetailAgendaActivity.class);
        intent.putExtra("agenda_item", item);
        startActivity(intent);
    }

    // ===================== NEWS RECYCLERVIEW =====================

    private void setupNewsRecyclerView(@NonNull View view) {
        rvNews = view.findViewById(R.id.rv_news);
        progressNews = view.findViewById(R.id.progress_news);
        if (rvNews != null) {
            // Layout horizontal
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false);
            rvNews.setLayoutManager(layoutManager);

            // Adapter
            newsAdapter = new MainNewsAdapter(requireContext());
            rvNews.setAdapter(newsAdapter);
        }
    }

    private void loadNews() {
        // Verifica cache antes de fazer request
        List<Noticia> cachedNews = NoticiaService.getInstance().getCachedNoticias(null);
        if (cachedNews != null && !cachedNews.isEmpty()) {
            Log.d("MainFragment", "💾 Usando notícias do cache (" + cachedNews.size() + " notícias)");
            if (newsAdapter != null) {
                newsAdapter.setNoticias(cachedNews);
                showNewsLoading(false);
            }
            return;
        }

        loadNewsForceUpdate();
    }

    /**
     * Carrega notícias forçando atualização (ignora cache)
     * Usado para atualização automática a cada 10 minutos
     */
    private void loadNewsForceUpdate() {
        showNewsLoading(true);
        // Limpa o cache antes de buscar para forçar atualização
        // Nota: NoticiaService não tem método público para limpar cache,
        // então vamos buscar diretamente, que vai atualizar o cache automaticamente
        NoticiaService.getInstance().fetchTodasNoticias(new NoticiasCallback() {
            @Override
            public void onSuccess(List<Noticia> noticias) {
                if (isAdded() && newsAdapter != null) {
                    newsAdapter.setNoticias(noticias);
                    showNewsLoading(false);
                    Log.d("MainFragment", "✅ Notícias atualizadas: " + noticias.size());
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    showNewsLoading(false);
                    Log.e("MainFragment", "❌ Erro ao atualizar notícias: " + errorMessage);
                    // Não mostra toast em atualizações automáticas para não incomodar o usuário
                }
            }
        });
    }

    private void showNewsLoading(boolean show) {
        if (progressNews != null) {
            progressNews.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvNews != null) {
            rvNews.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // ===================== CLOCK =====================

    private void startClock() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                updateClock();
                if (clockHandler != null) {
                    clockHandler.postDelayed(this, 1000);
                }
            }
        };
        if (clockHandler != null) {
            clockHandler.post(clockRunnable);
        }
    }

    // ===================== NEWS AUTO UPDATE =====================

    /**
     * Inicia a atualização automática de notícias a cada 10 minutos
     */
    private void startNewsAutoUpdate() {
        if (newsUpdateHandler == null) {
            newsUpdateHandler = new Handler(Looper.getMainLooper());
        }

        // Cancela qualquer atualização pendente antes de iniciar nova
        if (newsUpdateRunnable != null) {
            newsUpdateHandler.removeCallbacks(newsUpdateRunnable);
        }

        newsUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded() && isResumed()) {
                    Log.d("MainFragment", "🔄 Atualização automática de notícias (a cada 10 minutos)");
                    // Força atualização ignorando o cache
                    loadNewsForceUpdate();
                }

                // Agenda próxima atualização
                if (newsUpdateHandler != null) {
                    newsUpdateHandler.postDelayed(this, NEWS_UPDATE_INTERVAL_MS);
                }
            }
        };

        // Inicia a primeira atualização após 10 minutos
        if (newsUpdateHandler != null) {
            newsUpdateHandler.postDelayed(newsUpdateRunnable, NEWS_UPDATE_INTERVAL_MS);
            Log.d("MainFragment", "✅ Atualização automática de notícias iniciada (próxima atualização em 10 minutos)");
        }
    }

    /**
     * Para a atualização automática de notícias
     */
    private void stopNewsAutoUpdate() {
        if (newsUpdateHandler != null && newsUpdateRunnable != null) {
            newsUpdateHandler.removeCallbacks(newsUpdateRunnable);
            Log.d("MainFragment", "⏸️ Atualização automática de notícias pausada");
        }
    }

    private void updateClock() {
        if (!isAdded())
            return;

        Calendar calendar = Calendar.getInstance();

        // Hora (HH:mm)
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String hour = hourFormat.format(calendar.getTime());

        // Data (dd/MM/yyyy)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());

        // Dia da semana (abreviado)
        String[] dias = { "DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB" };
        int dow = calendar.get(Calendar.DAY_OF_WEEK); // 1 = DOM
        String dia = dias[dow - 1];

        if (numberHour != null)
            numberHour.setText(hour);
        if (txtDate != null)
            txtDate.setText(date);
        if (nmDay != null)
            nmDay.setText(dia);
    }

    // ===================== MEDIA CONTROLLER (via MediaStateListener)
    // =====================

    /**
     * Implementação de MediaStateListener.onPlaybackStateChanged
     * Chamado automaticamente pela MediaActivity quando o estado de playback muda
     */
    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> syncPlaybackState(state));
        }
    }

    /**
     * Implementação de MediaStateListener.onMetadataChanged
     * Chamado automaticamente pela MediaActivity quando os metadados mudam
     */
    @Override
    public void onMetadataChanged(android.support.v4.media.MediaMetadataCompat metadata) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> updateMetadataFromController(metadata));
        }
    }

    private void syncPlaybackState(@Nullable PlaybackStateCompat state) {
        if (state == null)
            return;

        int s = state.getState();
        boolean nowPlaying = (s == PlaybackStateCompat.STATE_PLAYING ||
                s == PlaybackStateCompat.STATE_BUFFERING);

        // Ícone play/pause
        if (btPlay != null) {
            btPlay.setImageResource(nowPlaying
                    ? R.drawable.btn_pause_radio
                    : R.drawable.btn_play_radio);
        }

        // ProgressBar de buffering
        if (progressBar != null) {
            if (s == PlaybackStateCompat.STATE_BUFFERING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    // ===================== PROGRAMAÇÃO =====================

    private void setupProgramacaoService() {
        // Usa ServiceManager para compartilhar instância
        programacaoAPIService = ServiceManager.getProgramacaoService();
        programacaoAPIService.setCallback(new ProgramacaoAPIService.ProgramacaoAPICallback() {
            @Override
            public void onProgramasLoaded(List<ProgramaAPI> programas) {
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        encontrarProgramaAtual(programas);
                    });
                }
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                // Pode ser usado para mostrar indicador de carregamento se necessário
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("MainFragment", "Erro ao buscar programação: " + errorMessage);
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        currentProgramName = "";
                        currentLocutorName = "";
                        updateProgramAndLocutorTexts();
                    });
                }
            }
        });
    }

    private void fetchCurrentProgram() {
        if (programacaoAPIService == null || data == null || data.radios == null ||
                data.radios.isEmpty() || Constants.ID < 0 || Constants.ID >= data.radios.size()) {
            return;
        }

        String radioId = data.radios.get(Constants.ID).id;
        if (radioId != null && !radioId.isEmpty()) {
            programacaoAPIService.fetchProgramacao(radioId, null);
        }
    }

    /**
     * Encontra o programa atual baseado no horário e dia da semana
     */
    private void encontrarProgramaAtual(List<ProgramaAPI> programas) {
        if (programas == null || programas.isEmpty()) {
            currentProgramName = "";
            currentLocutorName = "";
            updateProgramAndLocutorTexts();
            Log.d("MainFragment", "⚠️ Nenhum programa encontrado");
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
                int minutosInicio = parseHorario(hrInicio);
                int minutosFinal = parseHorario(hrFinal);

                // Verifica se o horário atual está dentro do intervalo do programa
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
                continue;
            }
        }

        if (programaAtual != null) {
            currentProgramName = programaAtual.getTitle() != null ? programaAtual.getTitle() : "";
            currentLocutorName = programaAtual.getNmLocutor() != null ? programaAtual.getNmLocutor() : "";
            Log.d("MainFragment", "✅ Programa encontrado: " + currentProgramName + " - " + currentLocutorName);
        } else {
            currentProgramName = "";
            currentLocutorName = "";
            Log.d("MainFragment", "⚠️ Nenhum programa no horário atual");
        }

        updateProgramAndLocutorTexts();
    }

    /**
     * Converte horário no formato "HH:mm" para minutos desde meia-noite.
     * OTIMIZADO: Usa TimeParser com cache automático
     */
    private int parseHorario(String horario) {
        return TimeParser.getMinutesFromTime(horario);
    }

    /**
     * Atualiza os TextViews com prioridade: programa/locutor primeiro, depois
     * música/artista
     */
    private void updateProgramAndLocutorTexts() {
        // Prioridade 1: Programa e Locutor
        boolean hasProgram = currentProgramName != null && !currentProgramName.isEmpty() &&
                !currentProgramName.trim().isEmpty() && !currentProgramName.equals("null");
        boolean hasLocutor = currentLocutorName != null && !currentLocutorName.isEmpty() &&
                !currentLocutorName.trim().isEmpty() && !currentLocutorName.equals("null");

        if (programNameMain != null) {
            if (hasProgram) {
                programNameMain.setText(currentProgramName);
                programNameMain.setSelected(true);
            } else {
                // Fallback: usa nome da música
                String musicText = (currentMusicName != null && !currentMusicName.isEmpty() &&
                        !currentMusicName.trim().isEmpty())
                                ? currentMusicName
                                : getString(R.string.app_name);
                programNameMain.setText(musicText);
                programNameMain.setSelected(true);
            }
        }

        if (locutorNameMain != null) {
            if (hasLocutor) {
                locutorNameMain.setText(currentLocutorName);
                locutorNameMain.setSelected(true);
            } else {
                // Fallback: usa nome do artista
                String artistText = (currentArtistName != null && !currentArtistName.isEmpty() &&
                        !currentArtistName.trim().isEmpty())
                                ? currentArtistName
                                : getString(R.string.string_live, "Ao vivo");
                locutorNameMain.setText(artistText);
                locutorNameMain.setSelected(true);
            }
        }
    }

    private void updateMetadataFromController() {
        if (controller == null)
            return;
        updateMetadataFromController(controller.getMetadata());
    }

    private void updateMetadataFromController(android.support.v4.media.MediaMetadataCompat metadata) {
        if (metadata == null)
            return;

        // Busca os metadados RDS do streaming
        // METADATA_KEY_DISPLAY_TITLE = nome da música
        // METADATA_KEY_DISPLAY_SUBTITLE = nome do artista
        String music = metadata.getString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE);
        String artist = metadata.getString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE);

        // Atualiza os valores de música e artista
        currentMusicName = (music != null && !music.isEmpty() && !music.trim().isEmpty())
                ? music
                : "";
        currentArtistName = (artist != null && !artist.isEmpty() && !artist.trim().isEmpty())
                ? artist
                : "";

        // Atualiza os TextViews com prioridade (programa/locutor primeiro, depois
        // música/artista)
        updateProgramAndLocutorTexts();
    }

    // ===================== CLIQUES =====================

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bt_menu) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openMenu();
            }
        } else if (id == R.id.bt_notif) {
            navigateToNotfProgram();
        } else if (id == R.id.bt_play) {
            togglePlayPause();
        } else if (id == R.id.bt_promotion) {
            navigateToPromotion();
        } else if (id == R.id.bt_news) {
            navigateToNews();
        } else if (id == R.id.bt_radio) {
            // Central (Ao vivo) – navega para AudioFragment
            navigateToAudioFragment();
        } else if (id == R.id.bt_program) {
            navigateToSchedule();
        } else if (id == R.id.bt_whatsapp) {
            openWhatsAppFromFirebase();
        } else if (id == R.id.btn_agenda) {
            navigateToAgenda();
        } else if (id == R.id.btn_news) {
            navigateToNews();
        } else if (id == R.id.rl_city) {
            navigateToChooseFragment();
        } else if (id == R.id.bt_back_choose) {
            navigateToChooseFragment();
        } else if (id == R.id.btn_expand_video) {
            startActivity(new Intent(requireContext(), FullscreenVideoActivity.class));
        }
    }

    private void navigateToAgenda() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.AGENDA);
        }
    }

    private void navigateToAudioFragment() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.AUDIO);
        }
    }

    private void navigateToSchedule() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.SCHEDULE);
        }
    }

    private void navigateToNotfProgram() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTF_PROGRAM);
        }
    }

    private void navigateToChooseFragment() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.CHOOSE);
        }
    }

    private void navigateToPromotion() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.PROMOTION);
        }
    }

    private void navigateToNews() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NEWS);
        }
    }

    private void openWhatsAppFromFirebase() {
        // OTIMIZADO: Usa RadioInfoCache para validação
        Data.Radios radio = RadioInfoCache.getRadioSafely(data, Constants.ID, requireContext());
        if (radio == null)
            return;

        if (radio.whatsapp != null && !radio.whatsapp.isEmpty()) {
            Intents.app(requireContext(), Intents.Social.WHATSAPP, radio.whatsapp);
        } else {
            showToast("WhatsApp não configurado");
        }
    }

    private void togglePlayPause() {
        // Se a Activity for uma MediaActivity, reaproveita a lógica dela
        if (getActivity() instanceof MediaActivity && btPlay != null) {
            ((MediaActivity) requireActivity()).onClick(btPlay);
            return;
        }

        // Fallback direto pelo MediaController, se estiver disponível
        if (controller != null && controller.getPlaybackState() != null
                && controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            controller.getTransportControls().pause();
        } else if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void showToast(String msg) {
        if (!isAdded())
            return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}