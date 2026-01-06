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

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.agenda.AgendaItem;
import br.com.fivecom.litoralfm.models.news.Noticia;
import br.com.fivecom.litoralfm.services.AgendaService;
import br.com.fivecom.litoralfm.ui.agenda.AgendaAdapter;
import br.com.fivecom.litoralfm.ui.agenda.DetailAgendaActivity;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.ui.main.MediaActivity;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.ui.main.adapter.MainNewsAdapter;
import br.com.fivecom.litoralfm.ui.news.network.NoticiaService;
import br.com.fivecom.litoralfm.ui.news.network.NoticiasCallback;
import br.com.fivecom.litoralfm.utils.core.Intents;

@UnstableApi
public class MainFragment extends Fragment implements View.OnClickListener {

    // Topo
    private ImageView btMenu;
    private ImageView btNotif;

    // Player mini
    private ImageView btPlay;
    private TextView musicName;
    private TextView artistName;

    // Data / hora / dia
    private TextView numberHour;
    private TextView txtDate;
    private TextView nmDay;
    private TextView txtCity;
    private RelativeLayout rlCity;

    // WebView do banner (já no layout)
    private WebView webView;

    // Bottom nav
    private ImageView btnNav1, btnNav2, btnNav3, btnNav4, btnNav5;

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
    private AgendaService agendaService;
    private List<AgendaItem> agendaList = new ArrayList<>();

    // ProgressBar para o player
    private ProgressBar progressBar;

    // Relógio
    private Handler clockHandler;
    private Runnable clockRunnable;

    // Media controller
    private MediaControllerCompat controller;
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            syncPlaybackState(state);
        }

        @Override
        public void onMetadataChanged(android.support.v4.media.MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            updateMetadataFromController();
        }
    };

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
        setupNewsRecyclerView(view);
        setupAgendaRecyclerView(view);
        startClock();
        attachMediaController();
        loadNews();
        loadAgenda();
    }

    @Override
    public void onStart() {
        super.onStart();
        attachMediaController();
        // Verifica se a rádio foi alterada e atualiza Constants.ID
        checkAndUpdateRadioSelection();
        // Atualiza a cidade
        updateCityName();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualiza a cidade quando o fragment volta a ser visível
        updateCityName();
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
        }
    }

    /**
     * Retorna o nome da cidade baseado no ID da rádio
     */
    private String getCityNameByRadioId(int radioId) {
        switch (radioId) {
            case 10224:
                return "CACHOEIRO - ES";
            case 10225:
                return "COLATINA - ES";
            case 10226:
                return "LINHARES - ES";
            case 10223:
                return "VITÓRIA - ES";
            default:
                return "VITÓRIA - ES";
        }
    }

    /**
     * Atualiza o nome da cidade no TextView baseado na rádio selecionada
     */
    private void updateCityName() {
        if (txtCity == null) return;

        try {
            // Lê a última rádio selecionada
            android.content.SharedPreferences prefs = requireContext()
                    .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
            int savedRadioId = prefs.getInt("selected_radio_id", 10223); // Default: VITÓRIA

            String cityName = getCityNameByRadioId(savedRadioId);
            txtCity.setText(cityName);
            android.util.Log.d("MainFragment", "🏙️ Cidade atualizada: " + cityName + " (Rádio ID: " + savedRadioId + ")");
        } catch (Exception e) {
            android.util.Log.e("MainFragment", "Erro ao atualizar cidade: " + e.getMessage());
            // Fallback para VITÓRIA em caso de erro
            txtCity.setText("VITÓRIA - ES");
        }
    }

    @Override
    public void onStop() {
        detachMediaController();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
    }

    // ===================== INIT =====================

    private void initViews(@NonNull View view) {
        // Topo
        btMenu = view.findViewById(R.id.bt_menu);
        btNotif = view.findViewById(R.id.bt_notif);

        // Mini player
        btPlay = view.findViewById(R.id.bt_play);
        musicName = view.findViewById(R.id.music_name);
        artistName = view.findViewById(R.id.artist_name);
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

        // WebView banner
        webView = view.findViewById(R.id.webView);

        // Bottom nav
        btnNav1 = view.findViewById(R.id.bt_promotion);
        btnNav2 = view.findViewById(R.id.bt_news);
        btnNav3 = view.findViewById(R.id.bt_radio);
        btnNav4 = view.findViewById(R.id.bt_program);
        btnNav5 = view.findViewById(R.id.bt_wpp);

        // News button (btn_news)
        btnNews = view.findViewById(R.id.btn_news);

        // Agenda button
        btnAgenda = view.findViewById(R.id.btn_agenda);

        // Agenda RecyclerView
        rvAgenda = view.findViewById(R.id.rv_agenda);
        progressAgenda = view.findViewById(R.id.progress_agenda);
        txtEmptyAgenda = view.findViewById(R.id.txt_empty_agenda);

        // Inicializar AgendaService
        agendaService = new AgendaService();

        // Handler do relógio
        clockHandler = new Handler(Looper.getMainLooper());

        // Atualiza a cidade baseada na rádio selecionada
        updateCityName();
    }

    private void setupClickListeners() {
        if (btMenu != null)
            btMenu.setOnClickListener(this);
        if (btNotif != null)
            btNotif.setOnClickListener(this);
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
    }

    /**
     * Configura a WebView do banner conforme o exemplo fornecido
     */
    private void setupWebView() {
        if (webView == null) return;

        webView.setBackgroundColor(0x00000000);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
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
        if (data != null && data.radios != null && !data.radios.isEmpty() && Constants.ID >= 0 && Constants.ID < data.radios.size()) {
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
                            Build.MANUFACTURER + " - " + Build.MODEL
                    );
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
            
            // CORREÇÃO: Torna a WebView visível antes de carregar
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(pubUrl);
            Log.d("MainFragment", "✅ WebView carregando URL: " + pubUrl);
        } else {
            Log.w("MainFragment", "⚠️ Dados da rádio não disponíveis para carregar o banner (ID: " + Constants.ID + ", radios size: " + (data != null && data.radios != null ? data.radios.size() : 0) + ")");
            webView.setVisibility(View.GONE);
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
        showNewsLoading(true);
        NoticiaService.getInstance().fetchTodasNoticias(new NoticiasCallback() {
            @Override
            public void onSuccess(List<Noticia> noticias) {
                if (isAdded() && newsAdapter != null) {
                    newsAdapter.setNoticias(noticias);
                    showNewsLoading(false);
                    Log.d("MainFragment", "✅ Notícias carregadas: " + noticias.size());
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    showNewsLoading(false);
                    Log.e("MainFragment", "❌ Erro ao carregar notícias: " + errorMessage);
                    showToast("Erro ao carregar notícias");
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

    // ===================== MEDIA CONTROLLER =====================

    private void attachMediaController() {
        try {
            controller = MediaControllerCompat.getMediaController(requireActivity());
            if (controller != null) {
                controller.registerCallback(controllerCallback);
                syncPlaybackState(controller.getPlaybackState());
                updateMetadataFromController();
            }
        } catch (Exception ignored) {
        }
    }

    private void detachMediaController() {
        try {
            if (controller != null) {
                controller.unregisterCallback(controllerCallback);
            }
        } catch (Exception ignored) {
        } finally {
            controller = null;
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

    private void updateMetadataFromController() {
        if (controller == null || controller.getMetadata() == null)
            return;

        // Busca os metadados RDS do streaming
        // METADATA_KEY_DISPLAY_TITLE = nome da música
        // METADATA_KEY_DISPLAY_SUBTITLE = nome do artista
        String music = controller.getMetadata().getString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE);
        String artist = controller.getMetadata().getString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE);

        if (musicName != null) {
            String musicText = music != null && !music.isEmpty() && !music.trim().isEmpty()
                    ? music
                    : getString(R.string.app_name);
            musicName.setText(musicText);
            // Manter selecionado para marquee funcionar
            musicName.setSelected(true);
        }

        if (artistName != null) {
            String artistText = artist != null && !artist.isEmpty() && !artist.trim().isEmpty()
                    ? artist
                    : getString(R.string.string_live, "Ao vivo");
            artistName.setText(artistText);
            // Manter selecionado para marquee funcionar
            artistName.setSelected(true);
        }
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
            openWhatsApp();
        } else if (id == R.id.btn_agenda) {
            navigateToAgenda();
        } else if (id == R.id.btn_news) {
            navigateToNews();
        } else if (id == R.id.rl_city) {
            navigateToChooseFragment();
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

    private void openWhatsApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://wa.me/5527999999999"));
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            // Se o WhatsApp não estiver instalado, tenta abrir no navegador
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("https://wa.me/5527999999999"));
                startActivity(intent);
            } catch (Exception ex) {
                showToast("Erro ao abrir WhatsApp");
            }
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