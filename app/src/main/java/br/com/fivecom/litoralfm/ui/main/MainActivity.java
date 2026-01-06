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
import android.view.View;
import android.view.ViewGroup;
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
import br.com.fivecom.litoralfm.ui.contact.ContactActivity;
import br.com.fivecom.litoralfm.ui.locutores.LocutoresActivity;
import br.com.fivecom.litoralfm.ui.main.fragment.AudioFragment;
import br.com.fivecom.litoralfm.ui.main.fragment.MainFragment;
import br.com.fivecom.litoralfm.ui.main.fragment.ScheduleFragment;
import br.com.fivecom.litoralfm.ui.notification.NotfProgramFragment;
import br.com.fivecom.litoralfm.ui.notification.NotificationFragment;
import br.com.fivecom.litoralfm.ui.promocao.PromocaoFragment;
import br.com.fivecom.litoralfm.news.NewsFragment;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.constants.Extras;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.core.Update;
import br.com.fivecom.litoralfm.utils.dialog.NextVersionDialog;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

@UnstableApi
public class MainActivity extends MediaActivity implements BluetoothDialogFragment.OnDeviceSelectedListener {

    private ActivityMainBinding binding;

    public enum FRAGMENT {
        MAIN,
        AUDIO,
        CHOOSE,
        NOTIFICATION,
        AGENDA,
        PROMOTION,
        SCHEDULE,
        NOTF_PROGRAM,
        NEWS
    }

    private FRAGMENT mFrag = FRAGMENT.MAIN;
    private FragmentManager fragmentManager;
    private Toast toast;
    private Chromecast mChromecast;

    public static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        android.util.Log.d("MainActivity", "=== ONCREATE ===");

        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
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
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::onMyBackPressed
            );
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
    }

    private Fragment getFragment(@NonNull FRAGMENT fragment) {
        switch (fragment) {
            case MAIN:
                return new MainFragment();
            case AUDIO:
                return new AudioFragment();
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
            default:
                return new MainFragment();
        }
    }

    public void navigateToFragment(FRAGMENT fragment) {
        Fragment targetFragment = getFragment(fragment);
        String tag = fragment.name();
        mFrag = fragment;

        try {
            fragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(binding.frameLayout.getId(), targetFragment, tag)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Erro ao navegar: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        try {
            if (mChromecast != null) mChromecast.onResume();
        } catch (Exception e) {
            // Ignora
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            if (mChromecast != null) mChromecast.onPause();
        } catch (Exception e) {
            // Ignora
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            if (mChromecast != null) mChromecast.onClose();
        } catch (Exception e) {
            // Ignora
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Ajustes de layout específicos podem ir aqui, se precisar.
    }

    private void onMyBackPressed() {
        // Fecha o menu se estiver aberto
        if (binding != null && binding.menuDrawerContainer != null 
                && binding.menuDrawerContainer.getVisibility() == View.VISIBLE) {
            closeMenu();
            return;
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return;
        }

        // Linha removida:
        // if (isBackground()) return;

        MainActivity.super.onBackPressed();
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
        String[] permissions = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                } :
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                };

        boolean hasConnect = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED;

        boolean hasScan = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED;

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
        if (toast != null) toast.cancel();
        toast = Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT);
        toast.show();
    }

    // ====================== MENU LATERAL =========================== //

    private void setupMenu() {
        if (binding == null) return;

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
        // menuContent é um include, então precisamos acessar o root view ou usar findViewById no container
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
                    openInstagram();
                });
            }

            // Botão YouTube
            View btnYoutube = menuContainer.findViewById(R.id.btn_youtube);
            if (btnYoutube != null) {
                btnYoutube.setOnClickListener(v -> {
                    closeMenu();
                    openYouTube();
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
                    showNextVersionDialog();
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
        if (data == null || data.radios == null || Constants.ID < 0 || Constants.ID >= data.radios.size()) {
            Toast.makeText(this, "Dados da rádio não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }

        Data.Radios radio = data.radios.get(Constants.ID);
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
        if (data == null || data.radios == null || Constants.ID < 0 || Constants.ID >= data.radios.size()) {
            Toast.makeText(this, "Dados da rádio não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }

        Data.Radios radio = data.radios.get(Constants.ID);
        if (radio.youtube != null && !radio.youtube.isEmpty()) {
            Intents.app(this, Intents.Social.YOUTUBE, radio.youtube);
        } else {
            Toast.makeText(this, "YouTube não configurado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abre o WhatsApp da rádio
     */
    private void openWhatsApp() {
        if (data == null || data.radios == null || Constants.ID < 0 || Constants.ID >= data.radios.size()) {
            Toast.makeText(this, "Dados da rádio não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }

        Data.Radios radio = data.radios.get(Constants.ID);
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
        if (data == null || data.radios == null || Constants.ID < 0 || Constants.ID >= data.radios.size()) {
            Toast.makeText(this, "Dados da rádio não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }

        Data.Radios radio = data.radios.get(Constants.ID);
        if (radio.url_site != null && !radio.url_site.isEmpty()) {
            Intents.website_internal(this, radio.url_site);
        } else {
            Toast.makeText(this, "Site não configurado", Toast.LENGTH_SHORT).show();
        }
    }

    public void openMenu() {
        if (binding == null) return;

        View menuContainer = binding.menuDrawerContainer;
        View overlay = binding.menuOverlay;

        if (menuContainer != null && overlay != null) {
            // Configura posição inicial (fora da tela à direita)
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            menuContainer.setTranslationX(screenWidth);
            menuContainer.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE);

            // Animações
            menuContainer.setAlpha(0f);
            overlay.setAlpha(0f);

            // Anima o menu vindo da direita para a esquerda
            menuContainer.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .start();

            // Anima o overlay escuro
            overlay.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    private void closeMenu() {
        if (binding == null) return;

        View menuContainer = binding.menuDrawerContainer;
        View overlay = binding.menuOverlay;

        if (menuContainer != null && overlay != null) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;

            // Anima o menu saindo para a direita
            menuContainer.animate()
                    .translationX(screenWidth)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        menuContainer.setVisibility(View.GONE);
                        menuContainer.setTranslationX(0f); // Reseta para próxima abertura
                    })
                    .start();

            // Anima o overlay escuro
            overlay.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> overlay.setVisibility(View.GONE))
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
}