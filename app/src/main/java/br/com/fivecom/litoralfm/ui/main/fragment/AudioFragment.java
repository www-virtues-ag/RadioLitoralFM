package br.com.fivecom.litoralfm.ui.main.fragment;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.Calendar;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.Data;
import br.com.fivecom.litoralfm.ui.choose.ChooseFragment;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.ui.main.MediaActivity;
import br.com.fivecom.litoralfm.ui.main.MediaStateListener;
import br.com.fivecom.litoralfm.ui.views.CircularVolumeSeekBar;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.services.ProgramacaoAPIService;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.constants.Extras;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.core.WebViewCacheManager;
import br.com.fivecom.litoralfm.utils.LottieHelper;
import br.com.fivecom.litoralfm.utils.ServiceManager;
import br.com.fivecom.litoralfm.utils.PrefsCache;
import br.com.fivecom.litoralfm.utils.requests.RequestListener;
import br.com.fivecom.litoralfm.utils.requests.RequestManager;

@UnstableApi
public class AudioFragment extends Fragment implements View.OnClickListener, MediaStateListener {

    private static final String TAG = "AudioFragment";
    private static final long METADATA_UPDATE_INTERVAL = 10000;
    private static final String ARG_RADIO_ID = "arg_radio_id";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_RADIO_ID = "selected_radio_id";

    // ID da rádio usada neste fragmento (vem da ChooseFragment ou default)
    private int selectedRadioId = 10224; // SUL por padrão
    private int lastSelectedRadioId = -1; // Para detectar mudanças
    private ImageView btPlay;
    private ImageView btBack;
    private ImageView btMenu;
    private ImageView btHome;
    private ImageView btNotif;
    private ImageView coverAlbum;
    private TextView nameProgram;
    private TextView nameLocutor;
    private TextView musicName;
    private CircularVolumeSeekBar volumeSeekBar;
    private CardView coverPlaceholder;
    // Bottom nav (agora são LinearLayout no include)
    private View btnNav1, btnNav2, btnNav3, btnNav4, btnNav5;
    private ImageView btnWatchNow;
    private ProgressBar progressBar;
    private WebView webView;

    private AudioManager audioManager;
    private boolean isPlaying = false;
    private RequestManager requestManager;
    private ProgramacaoAPIService programacaoAPIService;
    private Handler metadataHandler;
    private Runnable metadataRunnable;
    private String currentSong = "";
    private String currentProgram = "Radio Litoral FM";
    private String currentHost = "Na litoral eu to legal";
    private String currentAlbumArtUrl = "";
    private MediaControllerCompat controller;

    // ======================= VOLUME SYNC (ROBUSTO) ======================= //
    private final Handler volumeHandler = new Handler(Looper.getMainLooper());
    private Runnable volumeRunnable;
    private boolean isUpdatingFromSystem = false;
    private int lastSystemVolume = -1;
    private int lastSystemMaxVolume = -1;

    // ======================= FACTORY ======================= //

    public static AudioFragment newInstance(int radioId) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RADIO_ID, radioId);
        fragment.setArguments(args);
        return fragment;
    }

    // ======================= CICLO DE VIDA ======================= //

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            selectedRadioId = getArguments().getInt(ARG_RADIO_ID, selectedRadioId);
        }

        if (getActivity() != null && getActivity().getIntent() != null) {
            int fromIntent = getActivity().getIntent().getIntExtra(Extras.ID.name(), selectedRadioId);
            selectedRadioId = fromIntent;
        }

        // Lê do SharedPreferences para pegar a última seleção do usuário
        selectedRadioId = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_RADIO_ID, selectedRadioId);

        // Inicializa lastSelectedRadioId
        lastSelectedRadioId = selectedRadioId;

        // Atualiza o índice global Constants.ID baseado no selectedRadioId
        updateConstantsIdFromRadioId();

        Log.d(TAG, "🎯 selectedRadioId = " + selectedRadioId + ", Constants.ID = " + Constants.ID);
    }

    /**
     * Atualiza Constants.ID (índice do array) baseado no selectedRadioId
     */
    private void updateConstantsIdFromRadioId() {
        if (data == null || data.radios == null || data.radios.isEmpty()) {
            Log.w(TAG, "⚠️ data.radios não disponível para atualizar Constants.ID");
            Constants.ID = 0; // Default para primeira rádio
            return;
        }

        // Procura o índice da rádio no array baseado no ID
        int index = findRadioIndexById(selectedRadioId);
        if (index >= 0) {
            Constants.ID = index;
            Log.d(TAG, "✅ Constants.ID atualizado para índice: " + index);
        } else {
            Log.w(TAG, "⚠️ Rádio com ID " + selectedRadioId + " não encontrada, usando índice 0");
            Constants.ID = 0;
        }
    }

    /**
     * Encontra o índice da rádio no array data.radios baseado no ID
     * @param radioId O ID da rádio (ex: 10224)
     * @return O índice no array, ou -1 se não encontrado
     */
    private int findRadioIndexById(int radioId) {
        if (data == null || data.radios == null) return -1;

        String radioIdStr = String.valueOf(radioId);
        for (int i = 0; i < data.radios.size(); i++) {
            if (data.radios.get(i).id != null && data.radios.get(i).id.equals(radioIdStr)) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupAudioManager();
        setupVolumeSeekBar();
        setupClickListeners();
        setupWebView();
        updateWatchNowButtonVisibility();
        startMetadataUpdates();
        attachMediaController();
        
        // Aplica o estado de animação nos Lotties
        applyLottieAnimationState();

        // Registra como listener da MediaActivity para receber callbacks de metadados/estado
        if (getActivity() instanceof MediaActivity) {
            ((MediaActivity) getActivity()).addMediaStateListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        attachMediaController();
        updateWatchNowButtonVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachMediaController();
        
        // Cancela handlers quando fragment é pausado (garantia extra)
        if (metadataHandler != null && metadataRunnable != null) {
            metadataHandler.removeCallbacks(metadataRunnable);
        }

        // Atualizar volume inicial + iniciar sync contínuo
        updateVolumeFromSystem();
        startVolumeSync();
        
        // Atualiza o estado dos Lotties quando o fragment volta a ficar visível
        applyLottieAnimationState();

        // Verifica se o usuário trocou de rádio no ChooseFragment (usa cache)
        int savedRadioId = PrefsCache.getRadioId(requireContext());

        if (savedRadioId != lastSelectedRadioId && lastSelectedRadioId != -1) {
            Log.d(TAG, "🔄 Rádio mudou de " + lastSelectedRadioId + " para " + savedRadioId);

            selectedRadioId = savedRadioId;
            updateConstantsIdFromRadioId();

            // Força o MediaService a trocar de rádio
            switchRadioStream();

            // Atualiza a visibilidade do botão watch now
            updateWatchNowButtonVisibility();

            lastSelectedRadioId = savedRadioId;
        } else if (lastSelectedRadioId == -1) {
            lastSelectedRadioId = savedRadioId;
            // Atualiza a visibilidade do botão watch now na primeira vez
            updateWatchNowButtonVisibility();
        }
        
        // Retoma o WebView do banner e verifica se precisa recarregar
        if (webView != null) {
            webView.onResume();
            // Se o WebView está visível mas perdeu o conteúdo, recarrega
            if (webView.getVisibility() == View.VISIBLE && 
                (webView.getUrl() == null || webView.getUrl().isEmpty())) {
                Log.d(TAG, "🔄 WebView do banner perdeu conteúdo, recarregando...");
                setupWebView();
            }
        }
    }

    @Override
    public void onStop() {
        stopVolumeSync();
        
        // Cancela handlers quando fragment é pausado
        if (metadataHandler != null && metadataRunnable != null) {
            metadataHandler.removeCallbacks(metadataRunnable);
        }
        
        detachMediaController();
        super.onStop();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Cancela handlers quando fragment é pausado
        if (metadataHandler != null && metadataRunnable != null) {
            metadataHandler.removeCallbacks(metadataRunnable);
        }
        stopVolumeSync();
        // Pausa o WebView do banner quando o fragment é pausado
        if (webView != null && webView.getVisibility() == View.VISIBLE) {
            webView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        // Remove o listener da MediaActivity
        if (getActivity() instanceof MediaActivity) {
            ((MediaActivity) getActivity()).removeMediaStateListener(this);
        }

        // Cancela todos os handlers
        if (metadataHandler != null) {
            metadataHandler.removeCallbacksAndMessages(null);
            metadataHandler = null;
        }
        metadataRunnable = null;
        
        if (volumeHandler != null) {
            volumeHandler.removeCallbacksAndMessages(null);
        }
        volumeRunnable = null;

        stopVolumeSync();

        btPlay = null;
        btBack = null;
        btMenu = null;
        btHome = null;
        btNotif = null;
        coverAlbum = null;
        nameProgram = null;
        nameLocutor = null;
        musicName = null;
        volumeSeekBar = null;
        progressBar = null;
        
        // Pausa o WebView do banner mas NÃO destrói para manter o carregamento entre navegações
        if (webView != null) {
            webView.onPause();
            // Não seta como null para manter o banner carregado entre navegações
            // webView = null;
        }
        
        isMediaControllerAttached = false;

        super.onDestroyView();
    }

    // ======================= MEDIA CALLBACKS ======================= //

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (state == null) return;
        requireActivity().runOnUiThread(() -> syncPlaybackState(state));
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null) return;

        requireActivity().runOnUiThread(() -> {
            String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE);
            String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

            // Pega a URL da capa do álbum
            String albumArtUrl = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);

            // Atualiza a música
            if (title != null && !title.isEmpty()) {
                currentSong = title;
                if (artist != null && !artist.isEmpty()) {
                    currentSong = artist + " - " + title;
                }
                updateSongInfo();
            }

            // Atualiza a capa do álbum
            if (albumArtUrl != null && !albumArtUrl.isEmpty()) {
                currentAlbumArtUrl = albumArtUrl;
                updateAlbumArt();
            }

            Log.d(TAG, "🎵 Metadados atualizados: " + currentSong + " | Capa: " + albumArtUrl);
        });
    }

    private boolean isMediaControllerAttached = false;
    
    private void attachMediaController() {
        // Evita múltiplas chamadas desnecessárias
        if (isMediaControllerAttached && controller != null) {
            return;
        }
        
        try {
            controller = MediaControllerCompat.getMediaController(requireActivity());
            if (controller != null) {
                syncPlaybackState(controller.getPlaybackState());

                MediaMetadataCompat metadata = controller.getMetadata();
                if (metadata != null) {
                    onMetadataChanged(metadata);
                }
                isMediaControllerAttached = true;
            }
        } catch (Exception ignored) {
        }
    }

    private void detachMediaController() {
        controller = null;
    }

    private void syncPlaybackState(@Nullable PlaybackStateCompat state) {
        if (state == null) return;

        int s = state.getState();
        boolean nowPlaying =
                (s == PlaybackStateCompat.STATE_PLAYING ||
                        s == PlaybackStateCompat.STATE_BUFFERING);

        isPlaying = nowPlaying;

        if (btPlay != null) {
            btPlay.setImageResource(
                    nowPlaying ? R.drawable.btn_pause_radio : R.drawable.btn_play_radio
            );
        }

        if (progressBar != null) {
            if (s == PlaybackStateCompat.STATE_BUFFERING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    // ======================= INIT ======================= //

    private void initializeViews(View view) {
        btPlay = view.findViewById(R.id.bt_play);

        btBack = view.findViewById(R.id.bt_back);
        btMenu = view.findViewById(R.id.bt_menu);
        btHome = view.findViewById(R.id.bt_home);
        btNotif = view.findViewById(R.id.bt_notif);

        coverAlbum = view.findViewById(R.id.cover_album);
        coverPlaceholder = view.findViewById(R.id.cover_placeholder);

        nameProgram = view.findViewById(R.id.name_program);
        nameLocutor = view.findViewById(R.id.name_locutor);
        musicName = view.findViewById(R.id.music_name);

        volumeSeekBar = view.findViewById(R.id.volumeSeekBar);

        progressBar = view.findViewById(R.id.progress_bar);

        btnNav1 = view.findViewById(R.id.bt_promotion);
        btnNav2 = view.findViewById(R.id.bt_news);
        btnNav3 = view.findViewById(R.id.bt_radio);
        btnNav4 = view.findViewById(R.id.bt_program);
        btnNav5 = view.findViewById(R.id.bt_whatsapp);

        btnWatchNow = view.findViewById(R.id.btn_watch_now);

        webView = view.findViewById(R.id.webView);

        requestManager = new RequestManager();
        // Usa ServiceManager para compartilhar instância
        programacaoAPIService = ServiceManager.getProgramacaoService();
        programacaoAPIService.setCallback(new ProgramacaoAPIService.ProgramacaoAPICallback() {
            @Override
            public void onProgramasLoaded(List<ProgramaAPI> programas) {
                if (getActivity() == null) return;
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
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    currentProgram = "Radio Litoral FM";
                    currentHost = "Na litoral eu to legal";
                    updateProgramInfo();
                });
            }
        });
        metadataHandler = new Handler(Looper.getMainLooper());
        
        // Configura o TextView music_name para o marquee funcionar
        if (musicName != null) {
            musicName.setSelected(true);
            musicName.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
            musicName.setMarqueeRepeatLimit(-1); // marquee_forever
            musicName.setSingleLine(true);
            musicName.setHorizontallyScrolling(true);
        }
    }

    private void setupAudioManager() {
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null || volumeSeekBar == null) return;

        // Inicializa o volume do widget com o volume real do sistema
        updateVolumeFromSystem();
    }

    private void setupVolumeSeekBar() {
        if (volumeSeekBar == null) return;

        volumeSeekBar.setOnVolumeChangedListener((volume, fromUser) -> {
            if (!fromUser) return;
            if (audioManager == null) return;
            if (isUpdatingFromSystem) return;

            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int targetVolume = Math.round((volume / 100f) * maxVolume);

            targetVolume = Math.max(0, Math.min(targetVolume, maxVolume));

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);

            // Atualiza cache pra evitar "piscada" no polling
            lastSystemVolume = targetVolume;
            lastSystemMaxVolume = maxVolume;

            Log.d(TAG, "🔊 Volume alterado pelo usuário: " + volume + "% (" + targetVolume + "/" + maxVolume + ")");
        });
    }

    private void updateVolumeFromSystem() {
        if (audioManager == null || volumeSeekBar == null) return;

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Só atualiza se houve mudança real (evita trabalho desnecessário e loops)
        if (currentVolume == lastSystemVolume && maxVolume == lastSystemMaxVolume) return;

        lastSystemVolume = currentVolume;
        lastSystemMaxVolume = maxVolume;

        int volumePercent = Math.round((currentVolume / (float) maxVolume) * 100f);

        isUpdatingFromSystem = true;
        // Importante: este método deve NÃO disparar callback como fromUser
        volumeSeekBar.setVolumeSilent(volumePercent);
        isUpdatingFromSystem = false;

        Log.d(TAG, "🔊 Volume do sistema: " + volumePercent + "% (" + currentVolume + "/" + maxVolume + ")");
    }

    private void startVolumeSync() {
        stopVolumeSync(); // evita duplicar

        volumeRunnable = new Runnable() {
            @Override
            public void run() {
                updateVolumeFromSystem();
                volumeHandler.postDelayed(this, 250); // responsivo e leve
            }
        };

        volumeHandler.post(volumeRunnable);
        Log.d(TAG, "✅ VolumeSync iniciado");
    }

    private void stopVolumeSync() {
        if (volumeRunnable != null) {
            volumeHandler.removeCallbacks(volumeRunnable);
            volumeRunnable = null;
            Log.d(TAG, "✅ VolumeSync parado");
        }
    }

    private void setupClickListeners() {
        if (btPlay != null) btPlay.setOnClickListener(this);
        if (btBack != null) btBack.setOnClickListener(this);
        if (btMenu != null) btMenu.setOnClickListener(this);
        if (btHome != null) btHome.setOnClickListener(this);
        if (btNotif != null) btNotif.setOnClickListener(this);

        if (btnNav1 != null) btnNav1.setOnClickListener(this);
        if (btnNav2 != null) btnNav2.setOnClickListener(this);
        if (btnNav3 != null) btnNav3.setOnClickListener(this);
        if (btnNav4 != null) btnNav4.setOnClickListener(this);
        if (btnNav5 != null) btnNav5.setOnClickListener(this);
        if (btnWatchNow != null) btnWatchNow.setOnClickListener(this);
    }

    /**
     * Atualiza a visibilidade do botão "watch now" baseado no ID da rádio
     * O botão só aparece para a rádio "Grande Vitória" (ID 10223)
     */
    private void updateWatchNowButtonVisibility() {
        if (btnWatchNow == null) return;

        // Usa cache para evitar leituras repetidas de SharedPreferences
        int currentRadioId = PrefsCache.getRadioId(requireContext());

        // Mostra o botão apenas para Grande Vitória (ID 10223)
        boolean isGrandeVitoria = (currentRadioId == 10223);
        btnWatchNow.setVisibility(isGrandeVitoria ? View.VISIBLE : View.GONE);

        Log.d(TAG, "📺 Botão watch now: " + (isGrandeVitoria ? "VISÍVEL" : "OCULTO") + " (Rádio ID: " + currentRadioId + ")");
    }

    /**
     * Configura a WebView do banner
     */
    private void setupWebView() {
        if (webView == null) return;

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
        if (data != null && data.radios != null && !data.radios.isEmpty() && Constants.ID >= 0 && Constants.ID < data.radios.size()) {
            String pubUrl = String.format(
                    Intents.decode(getString(R.string.pub)),
                    data.radios.get(Constants.ID).id,
                    "Android " + Build.VERSION.RELEASE,
                    Build.MANUFACTURER + " - " + Build.MODEL
            );
            
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
                Log.d(TAG, "✅ WebView carregando URL: " + pubUrl + (urlChanged ? " (URL mudou)" : " (cache expirado)"));
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

    // ======================= CLICK ======================= //

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bt_play) {
            togglePlayPause();
        } else if (id == R.id.bt_back) {
            // Usa handleBackPress para navegação correta com back stack
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).handleBackPress();
            }
        } else if (id == R.id.bt_menu) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openMenu();
            }
        } else if (id == R.id.bt_home) {
            // Navega para o MainFragment
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
            }
        } else if (id == R.id.bt_notif) {
            navigateToNotfProgram();
        } else if (id == R.id.bt_promotion) {
            navigateToPromotion();
        } else if (id == R.id.bt_news) {
            navigateToNews();
        } else if (id == R.id.bt_radio) {
            // Já está na tela de rádio
        } else if (id == R.id.bt_program) {
            navigateToSchedule();
        } else if (id == R.id.bt_whatsapp) {
            openWhatsAppFromFirebase();
        } else if (id == R.id.btn_watch_now) {
            // Navega para o RadioFragment (vídeo)
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.RADIO);
            }
        }
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToNotfProgram() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTF_PROGRAM);
        }
    }

    private void navigateToPromotion() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.PROMOTION);
        }
    }

    private void navigateToSchedule() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.SCHEDULE);
        }
    }

    private void navigateToNews() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NEWS);
        }
    }

    private void openWhatsAppFromFirebase() {
        if (data == null || data.radios == null || Constants.ID < 0 || Constants.ID >= data.radios.size()) {
            return;
        }

        Data.Radios radio = data.radios.get(Constants.ID);
        if (radio.whatsapp != null && !radio.whatsapp.isEmpty()) {
            Intents.app(requireContext(), Intents.Social.WHATSAPP, radio.whatsapp);
        }
    }

    private void togglePlayPause() {
        if (getActivity() instanceof MediaActivity) {
            ((MediaActivity) requireActivity()).onClick(btPlay);
            return;
        }

        if (controller != null &&
                controller.getPlaybackState() != null &&
                controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            controller.getTransportControls().pause();
        } else if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void switchRadioStream() {
        Log.d(TAG, "🔄 Iniciando troca de stream para rádio ID: " + selectedRadioId + " (índice: " + Constants.ID + ")");

        if (controller != null) {
            controller.getTransportControls().stop();
        }

        currentSong = "";
        currentProgram = "Radio Litoral FM";
        currentHost = "Na litoral eu to legal";
        currentAlbumArtUrl = "";

        updateProgramInfo();
        updateSongInfo();
        updateAlbumArt();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Inicia o novo stream
            if (controller != null) {
                Log.d(TAG, "▶️ Iniciando novo stream...");
                controller.getTransportControls().play();
            }

            // Aguarda mais 1s para buscar os metadados da nova rádio
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                fetchCurrentProgram();
                Log.d(TAG, "✅ Troca de stream concluída para rádio ID: " + selectedRadioId);
            }, 1000);
        }, 800);
    }

    // ======================= METADADOS ======================= //

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

    private void fetchCurrentProgram() {
        if (programacaoAPIService == null || data == null || data.radios == null) return;

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

        // Converte Calendar.DAY_OF_WEEK para o formato da API (1=Domingo, 2=Segunda, etc.)
        // A API usa: 1=Domingo, 2=Segunda, 3=Terça, 4=Quarta, 5=Quinta, 6=Sexta, 7=Sábado
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

        // Se não encontrou programa, usa o primeiro do dia ou placeholders
        if (programaAtual == null) {
            // Tenta pegar o primeiro programa do dia
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
                currentSong = "";
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
                            : "Radio Litoral FM"
            );
        }

        if (nameLocutor != null) {
            nameLocutor.setText(
                    currentHost != null && !currentHost.isEmpty()
                            ? currentHost
                            : "Na litoral eu to legal"
            );
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

    private void updateAlbumArt() {
        if (coverAlbum == null || getContext() == null) return;

        if (currentAlbumArtUrl != null && !currentAlbumArtUrl.isEmpty()) {
            Log.d(TAG, "🖼️ Carregando capa do álbum: " + currentAlbumArtUrl);

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(requireContext())
                    .load(currentAlbumArtUrl)
                    .apply(options)
                    .into(coverAlbum);
        } else {
            Log.d(TAG, "🖼️ Sem capa disponível, usando placeholder ic_launcher");
            Glide.with(requireContext())
                    .load(R.drawable.ic_launcher)
                    .into(coverAlbum);
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
                R.id.lottie_audio1,
                R.id.lottie_audio2
            );
        }
    }
}
