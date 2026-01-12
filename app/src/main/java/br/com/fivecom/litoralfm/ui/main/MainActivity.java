package br.com.fivecom.litoralfm.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.chromecast.Chromecast;
import br.com.fivecom.litoralfm.databinding.ActivityMainBinding;
import br.com.fivecom.litoralfm.models.Data;
import br.com.fivecom.litoralfm.ui.bluetooth.BluetoothConnectionManager;
import br.com.fivecom.litoralfm.ui.bluetooth.BluetoothDialogFragment;
import br.com.fivecom.litoralfm.ui.agenda.AgendaFragment;
import br.com.fivecom.litoralfm.ui.choose.ChooseFragment;
import br.com.fivecom.litoralfm.ui.config.ConfigFragment;
import br.com.fivecom.litoralfm.ui.contact.ContactActivity;
import br.com.fivecom.litoralfm.ui.locutores.LocutoresActivity;
import br.com.fivecom.litoralfm.ui.main.fragment.AudioFragment;
import br.com.fivecom.litoralfm.ui.main.fragment.MainFragment;
import br.com.fivecom.litoralfm.ui.main.fragment.RadioFragment;
import br.com.fivecom.litoralfm.ui.main.fragment.ScheduleFragment;
import br.com.fivecom.litoralfm.ui.notification.NotfProgramFragment;
import br.com.fivecom.litoralfm.ui.notification.NotificationFragment;
import br.com.fivecom.litoralfm.ui.promocao.PromocaoFragment;
import br.com.fivecom.litoralfm.news.NewsFragment;
import br.com.fivecom.litoralfm.ui.social.SocialFeedFragment;
import br.com.fivecom.litoralfm.models.social.Platform;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.constants.Extras;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.core.Update;
import br.com.fivecom.litoralfm.utils.dialog.NextVersionDialog;
import br.com.fivecom.litoralfm.utils.RadioInfoCache;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

@UnstableApi
public class MainActivity extends MediaActivity implements BluetoothDialogFragment.OnDeviceSelectedListener {

    private ActivityMainBinding binding;

    public enum FRAGMENT {
        MAIN,
        AUDIO,
        RADIO,
        CHOOSE,
        NOTIFICATION,
        AGENDA,
        PROMOTION,
        SCHEDULE,
        NOTF_PROGRAM,
        NEWS,
        CONFIG,
        SOCIAL_FEED
    }

    private FRAGMENT mFrag = FRAGMENT.MAIN;
    private FragmentManager fragmentManager;
    private Toast toast;
    private Chromecast mChromecast;
    private boolean isMenuOpen = false;

    // SharedVideoManager para gerenciar WebView única
    private br.com.fivecom.litoralfm.video.SharedVideoManager videoManager;
    private android.widget.FrameLayout backgroundVideoContainer;

    public static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        android.util.Log.d("MainActivity", "=== ONCREATE ===");

        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Update(this);

        try {
            if (binding.mediaRouteButton != null) {
                mChromecast = new Chromecast(this, "default");
                mChromecast.setMediaButton(binding.mediaRouteButton);
            }
        } catch (Exception e) {
            android.util.Log.w("MainActivity", "Chromecast não disponível: " + e.getMessage());
        }

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            boolean goToChoose = getIntent().getBooleanExtra("go_to_choose", false);
            boolean goToNotification = getIntent().getBooleanExtra("go_to_notification", false);
            boolean goToSchedule = getIntent().getBooleanExtra("go_to_schedule", false);
            boolean goToNotfProgram = getIntent().getBooleanExtra("go_to_notf_program", false);
            int radioId = getIntent().getIntExtra(Extras.ID.name(), 10224);

            if (goToNotfProgram) {
                mFrag = FRAGMENT.NOTF_PROGRAM;
                fragmentManager.beginTransaction()
                        .replace(binding.frameLayout.getId(),
                                new NotfProgramFragment(),
                                FRAGMENT.NOTF_PROGRAM.name())
                        .commit();

            } else if (goToNotification) {
                mFrag = FRAGMENT.NOTIFICATION;
                fragmentManager.beginTransaction()
                        .replace(binding.frameLayout.getId(),
                                new NotificationFragment(),
                                FRAGMENT.NOTIFICATION.name())
                        .commit();

            } else if (goToSchedule) {
                mFrag = FRAGMENT.SCHEDULE;
                fragmentManager.beginTransaction()
                        .replace(binding.frameLayout.getId(),
                                new ScheduleFragment(),
                                FRAGMENT.SCHEDULE.name())
                        .commit();

            } else if (goToChoose) {
                mFrag = FRAGMENT.CHOOSE;
                fragmentManager.beginTransaction()
                        .replace(binding.frameLayout.getId(),
                                new ChooseFragment(),
                                FRAGMENT.CHOOSE.name())
                        .commit();

            } else {
                mFrag = FRAGMENT.AUDIO;
                fragmentManager.beginTransaction()
                        .replace(binding.frameLayout.getId(),
                                new AudioFragment(),
                                FRAGMENT.AUDIO.name())
                        .commit();
            }
        }

        // Back customizado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::onMyBackPressed);
        } else {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    onMyBackPressed();
                }
            });
        }

        // Conecta no MediaService (vem da MediaActivity)
        connectMediaService();

        // Configura o menu lateral
        setupMenu();

        // Inicializa SharedVideoManager
        initializeSharedVideoManager();

        // Atualiza o estado inicial da NavBar após um pequeno delay para garantir que
        // as views estão infladas
        binding.getRoot().post(() -> updateNavBarState(mFrag));

    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    private Fragment getFragment(@NonNull FRAGMENT fragment) {
        switch (fragment) {
            case MAIN:
                return new MainFragment();
            case AUDIO:
                return new AudioFragment();
            case RADIO:
                return new RadioFragment();
            case CHOOSE:
                return new ChooseFragment();
            case NOTIFICATION:
                return new NotificationFragment();
            case AGENDA:
                return new AgendaFragment();
            case PROMOTION:
                return new PromocaoFragment();
            case SCHEDULE:
                return new ScheduleFragment();
            case NOTF_PROGRAM:
                return new NotfProgramFragment();
            case NEWS:
                return new NewsFragment();
            case CONFIG:
                return new ConfigFragment();
            default:
                return new MainFragment();
        }
    }

    /**
     * Notifica os fragments sobre mudança no estado de animação
     */
    public void notifyAnimationStateChanged(boolean enabled) {
        // Notifica todos os fragments ativos para atualizar seus Lotties
        Fragment currentFragment = fragmentManager.findFragmentById(binding.frameLayout.getId());
        if (currentFragment != null && currentFragment.getView() != null) {
            br.com.fivecom.litoralfm.utils.LottieHelper.setAnimationStateForLotties(
                    currentFragment.getView(),
                    this,
                    R.id.lottie_main,
                    R.id.lottie_audio1,
                    R.id.lottie_audio2,
                    R.id.lottie_radio1,
                    R.id.lottie_radio2);
        }
    }

    public void navigateToFragment(FRAGMENT fragment) {
        Fragment targetFragment = getFragment(fragment);

        // Se o fragment for null (caso especial como SOCIAL_FEED), não navega aqui
        if (targetFragment == null) {
            return;
        }

        String tag = fragment.name();

        // Controla o vídeo
        // handleVideoOnNavigation(mFrag, fragment); // REMOVIDO: SharedVideoManager
        // cuida disso

        mFrag = fragment;

        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(binding.frameLayout.getId(), targetFragment, tag);

            // Adiciona ao back stack apenas se não for MAIN (tela inicial)
            // Isso permite que o botão voltar funcione corretamente
            if (fragment != FRAGMENT.MAIN) {
                transaction.addToBackStack(tag);
            }

            transaction.commitAllowingStateLoss();

            // Atualiza o estado da NavBar após navegação (com delay para garantir que a
            // view foi inflada)
            binding.getRoot().post(() -> {
                // Adiciona um pequeno delay adicional para garantir que o fragment foi
                // totalmente criado
                binding.getRoot().postDelayed(() -> updateNavBarState(fragment), 100);
            });
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Erro ao navegar: " + e.getMessage());
        }
    }

    /**
     * Navega para o SocialFeedFragment com a plataforma especificada
     */
    public void navigateToSocialFeed(Platform platform) {
        // URL fixa para o feed de redes sociais
        String apiUrl = "https://redessociais.ticketss.app/litoral-fm-vitoria.json";

        // Cria o fragment com os parâmetros
        SocialFeedFragment fragment = SocialFeedFragment.newInstance(apiUrl, platform);
        mFrag = FRAGMENT.SOCIAL_FEED;

        try {
            fragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(binding.frameLayout.getId(), fragment, FRAGMENT.SOCIAL_FEED.name())
                    .addToBackStack(FRAGMENT.SOCIAL_FEED.name()) // Adiciona ao back stack
                    .commitAllowingStateLoss();

            // Atualiza NavBar
            binding.getRoot().post(() -> {
                binding.getRoot().postDelayed(() -> updateNavBarState(FRAGMENT.SOCIAL_FEED), 100);
            });
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Erro ao navegar para SocialFeed: " + e.getMessage());
            Toast.makeText(this, "Erro ao abrir feed social", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Atualiza o estado visual da NavBar baseado no fragment atual
     * Troca a imagem do ImageView para versão selecionada e o texto para branco
     */
    public void updateNavBarState(FRAGMENT currentFragment) {
        // Busca a view do include da NavBar
        // Como o include está em cada fragment, precisamos buscar no fragment atual
        View navBarView = null;

        // Tenta buscar no fragment atual
        Fragment currentFragmentInstance = fragmentManager.findFragmentById(binding.frameLayout.getId());
        if (currentFragmentInstance != null && currentFragmentInstance.getView() != null) {
            navBarView = currentFragmentInstance.getView().findViewById(R.id.rlNavBar);
        }

        if (navBarView == null) {
            android.util.Log.w("MainActivity", "NavBar não encontrada para atualização");
            return;
        }

        // Busca os ImageViews e TextViews da NavBar
        ImageView imgPromotion = navBarView.findViewById(R.id.img_promotion);
        ImageView imgNews = navBarView.findViewById(R.id.img_news);
        ImageView imgRadio = navBarView.findViewById(R.id.img_radio);
        ImageView imgProgram = navBarView.findViewById(R.id.img_program);

        TextView txtPromotion = navBarView.findViewById(R.id.txt_promotion);
        TextView txtNews = navBarView.findViewById(R.id.txt_news);
        TextView txtRadio = navBarView.findViewById(R.id.txt_radio);
        TextView txtProgram = navBarView.findViewById(R.id.txt_program);

        // Reseta todos para estado não selecionado
        if (imgPromotion != null) {
            imgPromotion.setImageResource(R.drawable.btn_promo_barra_off);
        }
        if (imgNews != null) {
            imgNews.setImageResource(R.drawable.btn_noticia_barra_off);
        }
        if (imgRadio != null) {
            imgRadio.setImageResource(R.drawable.btn_ao_vivo_barra_off);
        }
        if (imgProgram != null) {
            imgProgram.setImageResource(R.drawable.btn_prog_barra_off);
        }

        if (txtPromotion != null) {
            txtPromotion.setTextColor(getResources().getColor(R.color.vermelho_letter));
        }
        if (txtNews != null) {
            txtNews.setTextColor(getResources().getColor(R.color.vermelho_letter));
        }
        if (txtRadio != null) {
            txtRadio.setTextColor(getResources().getColor(R.color.vermelho_letter));
        }
        if (txtProgram != null) {
            txtProgram.setTextColor(getResources().getColor(R.color.vermelho_letter));
        }

        // Define o estado selecionado baseado no fragment atual
        switch (currentFragment) {
            case PROMOTION:
                if (imgPromotion != null) {
                    imgPromotion.setImageResource(R.drawable.btn_promo_barra_on);
                }
                if (txtPromotion != null) {
                    txtPromotion.setTextColor(getResources().getColor(android.R.color.white));
                }
                break;
            case NEWS:
                if (imgNews != null) {
                    imgNews.setImageResource(R.drawable.btn_noticia_barra_on);
                }
                if (txtNews != null) {
                    txtNews.setTextColor(getResources().getColor(android.R.color.white));
                }
                break;
            case RADIO:
            case AUDIO:
                // RADIO e AUDIO usam o mesmo botão (ao vivo)
                if (imgRadio != null) {
                    imgRadio.setImageResource(R.drawable.btn_ao_vivo_barra_on);
                }
                if (txtRadio != null) {
                    txtRadio.setTextColor(getResources().getColor(android.R.color.white));
                }
                break;
            case SCHEDULE:
                // SCHEDULE usa o botão de programas
                if (imgProgram != null) {
                    imgProgram.setImageResource(R.drawable.btn_prog_barra_on);
                }
                if (txtProgram != null) {
                    txtProgram.setTextColor(getResources().getColor(android.R.color.white));
                }
                break;
            case MAIN:
            default:
                // MAIN não tem botão correspondente na NavBar, mantém todos desmarcados
                break;
        }
    }

    @Override
    protected void onResume() {
        try {
            if (mChromecast != null)
                mChromecast.onResume();
        } catch (Exception e) {
            // Ignora
        }

        if (videoManager != null) {
            videoManager.onResume();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            if (mChromecast != null)
                mChromecast.onPause();
        } catch (Exception e) {
            // Ignora
        }

        // NÃO pausa o vídeo aqui - deixa o SharedVideoManager gerenciar
        // O vídeo deve continuar tocando em background quando navega entre fragments
        // Apenas pausa se a Activity realmente sair de foco (ex: app vai para background)
        // O SharedVideoManager já gerencia o estado do vídeo através dos fragments
        // if (videoManager != null && !isChangingConfigurations()) {
        //     videoManager.onPause();
        // }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            if (mChromecast != null)
                mChromecast.onClose();
        } catch (Exception e) {
            // Ignora
        }

        // Destrói o SharedVideoManager se necessário (geralmente não, é singleton
        // application scoped)
        // Mas se quisermos limpar referências da activity:
        // if (videoManager != null) {
        // videoManager.release();
        // }

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Ajustes de layout específicos podem ir aqui, se precisar.
    }

    private void onMyBackPressed() {
        // Fecha o menu se estiver aberto
        if (isMenuOpen) {
            closeMenu();
            return;
        }

        // Se há back stack, volta para o fragment anterior
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();

            // Atualiza o fragment atual após popBackStack
            // Usa post para garantir que o fragment foi restaurado
            binding.getRoot().post(() -> {
                Fragment currentFragment = fragmentManager.findFragmentById(binding.frameLayout.getId());
                if (currentFragment != null && currentFragment.getTag() != null) {
                    try {
                        String tag = currentFragment.getTag();
                        mFrag = FRAGMENT.valueOf(tag);
                        updateNavBarState(mFrag);
                    } catch (IllegalArgumentException e) {
                        // Se não conseguir encontrar o enum, assume MAIN
                        mFrag = FRAGMENT.MAIN;
                        updateNavBarState(mFrag);
                    }
                }
            });
            return;
        }

        // Se não há back stack, verifica se já está no MAIN
        // Se não estiver, navega para MAIN ao invés de fechar o app
        if (mFrag != FRAGMENT.MAIN) {
            navigateToFragment(FRAGMENT.MAIN);
            return;
        }

        // Se já está no MAIN e não há back stack, não faz nada
        // O app permanece aberto na tela principal
        // Não fecha o app para manter o usuário na aplicação
    }

    /**
     * Método público para ser chamado pelos fragments quando o bt_back é clicado
     * Centraliza a lógica de navegação para trás
     */
    public void handleBackPress() {
        onMyBackPressed();
    }

    // ====================== BLUETOOTH =========================== //

    @SuppressLint("MissingPermission")
    private void openBluetoothDialog() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            showToast(getString(R.string.string_support2));
        } else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            BluetoothDialogFragment dialog = new BluetoothDialogFragment();
            dialog.show(getSupportFragmentManager(), "BluetoothDialog");
        }
    }

    public void requestBluetoothPermissions() {
        String[] permissions = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? new String[] {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
        }
                : new String[] {
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                };

        boolean hasConnect = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;

        boolean hasScan = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

        if (hasConnect && hasScan) {
            openBluetoothDialog();
        } else {
            requestPermissions(permissions, REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openBluetoothDialog();
            } else {
                showToast(getString(R.string.toast_permissions));
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onDeviceSelected(BluetoothDevice device) {
        BluetoothConnectionManager.getInstance().connectA2dp(this, device);
        showToast(getString(R.string.string_try2) + " " + device.getName());
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        showToast(getString(R.string.string_disconnect));
    }

    public void showToast(@NonNull String string) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT);
        toast.show();
    }

    // ====================== MENU LATERAL =========================== //

    private void setupMenu() {
        if (binding == null)
            return;

        // Configura a largura do menu para 70% da tela
        View menuContainer = binding.menuDrawerContainer;
        if (menuContainer != null) {
            ViewGroup.LayoutParams params = menuContainer.getLayoutParams();
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            params.width = (int) (screenWidth * 0.7f);
            menuContainer.setLayoutParams(params);
        }

        // Configura o overlay para fechar o menu
        View overlay = binding.menuOverlay;
        if (overlay != null) {
            overlay.setOnClickListener(v -> closeMenu());
        }

        // Configura os botões do menu
        // menuContent é um include, então precisamos acessar o root view ou usar
        // findViewById no container
        if (menuContainer != null) {
            // Botão voltar do menu
            View btnVoltar = menuContainer.findViewById(R.id.btn_voltar);
            if (btnVoltar != null) {
                btnVoltar.setOnClickListener(v -> closeMenu());
            }

            // Botão notícias
            View btnNoticias = menuContainer.findViewById(R.id.btn_noticias);
            if (btnNoticias != null) {
                btnNoticias.setOnClickListener(v -> {
                    closeMenu();
                    navigateToNewsActivity();
                });
            }

            // Botão agenda
            View btnAgenda = menuContainer.findViewById(R.id.btn_agenda);
            if (btnAgenda != null) {
                btnAgenda.setOnClickListener(v -> {
                    closeMenu();
                    navigateToFragment(FRAGMENT.AGENDA);
                });
            }

            // Botão promoções
            View btnPromocoes = menuContainer.findViewById(R.id.btn_promocoes);
            if (btnPromocoes != null) {
                btnPromocoes.setOnClickListener(v -> {
                    closeMenu();
                    navigateToFragment(FRAGMENT.PROMOTION);
                });
            }

            // Botão programação
            View btnProgramacao = menuContainer.findViewById(R.id.btn_programacao);
            if (btnProgramacao != null) {
                btnProgramacao.setOnClickListener(v -> {
                    closeMenu();
                    navigateToFragment(FRAGMENT.SCHEDULE);
                });
            }

            // Botão contato
            View btnContato = menuContainer.findViewById(R.id.btn_contato);
            if (btnContato != null) {
                btnContato.setOnClickListener(v -> {
                    closeMenu();
                    navigateToContactActivity("Contato Comercial");
                });
            }

            // Botão pedido musical
            View btnPedidoMusical = menuContainer.findViewById(R.id.btn_pedido_musical);
            if (btnPedidoMusical != null) {
                btnPedidoMusical.setOnClickListener(v -> {
                    closeMenu();
                    navigateToContactActivity("Pedido Musical");
                });
            }

            // Botão quem somos
            View btnQuemSomos = menuContainer.findViewById(R.id.btn_quem_somos);
            if (btnQuemSomos != null) {
                btnQuemSomos.setOnClickListener(v -> {
                    closeMenu();
                    navigateToLocutoresActivity();
                });
            }

            // Botão Instagram
            View btnInstagram = menuContainer.findViewById(R.id.btn_instagram);
            if (btnInstagram != null) {
                btnInstagram.setOnClickListener(v -> {
                    closeMenu();
                    navigateToSocialFeed(Platform.instagram);
                });
            }

            // Botão YouTube
            View btnYoutube = menuContainer.findViewById(R.id.btn_youtube);
            if (btnYoutube != null) {
                btnYoutube.setOnClickListener(v -> {
                    closeMenu();
                    navigateToSocialFeed(Platform.youtube);
                });
            }

            // Botão WhatsApp
            View btnWhatsapp = menuContainer.findViewById(R.id.btn_whatsapp);
            if (btnWhatsapp != null) {
                btnWhatsapp.setOnClickListener(v -> {
                    closeMenu();
                    openWhatsApp();
                });
            }

            // URL da rádio (site)
            View urlRadioLitoral = menuContainer.findViewById(R.id.url_radio_litoral);
            if (urlRadioLitoral != null) {
                urlRadioLitoral.setOnClickListener(v -> {
                    closeMenu();
                    openWebsite();
                });
            }

            // Botão configurações
            View btnConfiguracoes = menuContainer.findViewById(R.id.btn_configuracoes);
            if (btnConfiguracoes != null) {
                btnConfiguracoes.setOnClickListener(v -> {
                    closeMenu();
                    navigateToFragment(FRAGMENT.CONFIG);
                });
            }
        }
    }

    /**
     * Mostra o dialog de próxima versão
     */
    private void showNextVersionDialog() {
        NextVersionDialog.show(this);
    }

    /**
     * Abre o Instagram da rádio
     */
    private void openInstagram() {
        // OTIMIZADO: Usa RadioInfoCache para validação
        Data.Radios radio = RadioInfoCache.getRadioSafely(data, Constants.ID, this);
        if (radio == null)
            return;

        if (radio.instagram != null && !radio.instagram.isEmpty()) {
            Intents.app(this, Intents.Social.INSTAGRAM, radio.instagram);
        } else {
            Toast.makeText(this, "Instagram não configurado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abre o YouTube da rádio
     */
    private void openYouTube() {
        // OTIMIZADO: Usa RadioInfoCache para validação
        Data.Radios radio = RadioInfoCache.getRadioSafely(data, Constants.ID, this);
        if (radio == null)
            return;

        if (radio.youtube != null && !radio.youtube.isEmpty()) {
            Intents.app(this, Intents.Social.YOUTUBE, radio.youtube);
        } else {
            Toast.makeText(this, "YouTube não configurado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abre o WhatsApp da rádio
     */
    public void openWhatsApp() {
        // OTIMIZADO: Usa RadioInfoCache para validação
        Data.Radios radio = RadioInfoCache.getRadioSafely(data, Constants.ID, this);
        if (radio == null)
            return;

        if (radio.whatsapp != null && !radio.whatsapp.isEmpty()) {
            Intents.app(this, Intents.Social.WHATSAPP, radio.whatsapp);
        } else {
            Toast.makeText(this, "WhatsApp não configurado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abre o site da rádio
     */
    private void openWebsite() {
        // OTIMIZADO: Usa RadioInfoCache para validação
        Data.Radios radio = RadioInfoCache.getRadioSafely(data, Constants.ID, this);
        if (radio == null)
            return;

        if (radio.url_site != null && !radio.url_site.isEmpty()) {
            Intents.website_internal(this, radio.url_site);
        } else {
            Toast.makeText(this, "Site não configurado", Toast.LENGTH_SHORT).show();
        }
    }

    public void openMenu() {
        if (binding == null)
            return;

        // Verifica se o menu já está aberto
        if (isMenuOpen) {
            return; // Menu já está aberto, não faz nada
        }

        View menuContainer = binding.menuDrawerContainer;
        View overlay = binding.menuOverlay;

        if (menuContainer != null && overlay != null) {
            // Cancela animações anteriores se houver
            menuContainer.clearAnimation();
            overlay.clearAnimation();
            menuContainer.animate().cancel();
            overlay.animate().cancel();

            // Marca o menu como aberto ANTES de animar
            isMenuOpen = true;

            // Configura posição inicial (fora da tela à direita)
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            menuContainer.setTranslationX(screenWidth);
            menuContainer.setAlpha(1f); // Garante alpha em 1 (não anima alpha para evitar piscar)
            menuContainer.setVisibility(View.VISIBLE);

            // Garante que o overlay está no estado correto antes de animar
            overlay.setAlpha(0f);
            overlay.setVisibility(View.VISIBLE);

            // Usa hardware layer para melhor performance e evitar piscar
            overlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            // Anima o overlay primeiro (mais rápido)
            overlay.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        overlay.setLayerType(View.LAYER_TYPE_NONE, null);
                    })
                    .start();

            // Anima o menu vindo da direita para a esquerda (SEM alterar alpha)
            menuContainer.animate()
                    .translationX(0f)
                    .setDuration(300)
                    .start();
        }
    }

    private void closeMenu() {
        if (binding == null)
            return;

        // Verifica se o menu já está fechado
        if (!isMenuOpen) {
            return; // Menu já está fechado, não faz nada
        }

        View menuContainer = binding.menuDrawerContainer;
        View overlay = binding.menuOverlay;

        if (menuContainer != null && overlay != null) {
            // Cancela animações anteriores se houver
            menuContainer.clearAnimation();
            overlay.clearAnimation();
            menuContainer.animate().cancel();
            overlay.animate().cancel();

            // Marca como fechado ANTES de animar para evitar chamadas duplicadas
            isMenuOpen = false;

            int screenWidth = getResources().getDisplayMetrics().widthPixels;

            // Garante que o overlay está no estado correto antes de animar
            // Isso evita o piscar causado por estados intermediários
            float currentAlpha = overlay.getAlpha();
            if (currentAlpha < 0.1f) {
                // Se já está quase invisível, esconde imediatamente
                overlay.setVisibility(View.GONE);
                overlay.setAlpha(0f);
            } else {
                // Usa hardware layer para melhor performance e evitar piscar
                overlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);

                // Anima o overlay primeiro (mais rápido)
                overlay.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction(() -> {
                            overlay.setLayerType(View.LAYER_TYPE_NONE, null);
                            overlay.setAlpha(0f); // Garante alpha em 0
                            overlay.setVisibility(View.GONE);
                        })
                        .start();
            }

            // Anima o menu saindo para a direita (SEM alterar alpha para evitar piscar)
            menuContainer.animate()
                    .translationX(screenWidth)
                    .setDuration(300)
                    .withEndAction(() -> {
                        // Reseta tudo ANTES de esconder para evitar flash
                        menuContainer.setTranslationX(screenWidth); // Mantém fora da tela
                        menuContainer.setAlpha(1f); // Garante alpha em 1
                        menuContainer.setVisibility(View.GONE);
                    })
                    .start();
        }
    }

    private void navigateToContactActivity() {
        navigateToContactActivity(null);
    }

    private void navigateToContactActivity(String contactType) {
        Intent intent = new Intent(this, ContactActivity.class);
        if (contactType != null) {
            intent.putExtra("contact_type", contactType);
        }
        startActivity(intent);
    }

    private void navigateToLocutoresActivity() {
        Intent intent = new Intent(this, LocutoresActivity.class);
        startActivity(intent);
    }

    private void navigateToNewsActivity() {
        navigateToFragment(FRAGMENT.NEWS);
    }

    /**
     * Inicializa o SharedVideoManager e obtém referência ao container de
     * background.
     */
    private void initializeSharedVideoManager() {
        videoManager = br.com.fivecom.litoralfm.video.SharedVideoManager.getInstance();
        videoManager.initialize(this);

        backgroundVideoContainer = binding.backgroundVideoContainer;

        android.util.Log.d("MainActivity", "✅ SharedVideoManager inicializado");
    }

    /**
     * Retorna o container de vídeo em background para os fragments.
     */
    public android.widget.FrameLayout getBackgroundVideoContainer() {
        return backgroundVideoContainer;
    }

    /**
     * Retorna o SharedVideoManager.
     */
    public br.com.fivecom.litoralfm.video.SharedVideoManager getVideoManager() {
        return videoManager;
    }
}
