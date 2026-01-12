package br.com.fivecom.litoralfm.ui.main.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.Data;
import br.com.fivecom.litoralfm.models.scheduler.DiaSemanaOption;
import br.com.fivecom.litoralfm.models.scheduler.Programa;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.models.scheduler.RadioOption;
import br.com.fivecom.litoralfm.services.ProgramacaoAPIService;
import br.com.fivecom.litoralfm.utils.ServiceManager;
import br.com.fivecom.litoralfm.services.ProgramacaoService;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.ui.main.adapter.ScheduleAdapter;
import br.com.fivecom.litoralfm.ui.spinners.SearchableSpinner;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.core.WebViewCacheManager;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

/**
 * Fragment para exibir a programação da rádio
 * Adaptado de ScheduleActivity para Fragment
 */
public class ScheduleFragment extends Fragment implements 
        ProgramacaoService.ProgramacaoCallback, 
        ProgramacaoAPIService.ProgramacaoAPICallback,
        View.OnClickListener {

    private static final String TAG = "ScheduleFragment";

    // Serviços
    private ProgramacaoService programacaoService;
    private ProgramacaoAPIService programacaoAPIService;

    // Seleções atuais
    private RadioOption selectedRadio;
    private DiaSemanaOption selectedDia;

    // Views
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView hintTextView;
    private SearchableSpinner spinnerWeekday;
    private SearchableSpinner spinnerRegion;
    private ImageView btBack;
    private ImageView btHome;
    private ImageView btMenu;
    private ImageView btNotif;
    
    // Navbar buttons (agora são LinearLayout no include)
    private View btPromotion;
    private View btNews;
    private View btRadio;
    private View btProgram;
    private View btWhatsapp;
    
    // WebView do banner
    private WebView webView;
    
    // Adapter
    private ScheduleAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializa seleções padrão
        List<RadioOption> radios = RadioOption.getTodas();
        List<DiaSemanaOption> dias = DiaSemanaOption.getTodos();
        
        selectedRadio = radios.get(0); // Vitória por padrão
        selectedDia = dias.get(0); // Todos os dias

        // Inicializa os serviços
        initServices();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa views
        initViews(view);

        // Configura spinners
        setupSpinners();

        // Configura RecyclerView
        setupRecyclerView();

        // Configura listeners
        setupListeners();

        // Configura WebView do banner
        setupWebView();
    }

    private void initServices() {
        // Serviço da API legada
        programacaoService = new ProgramacaoService();
        programacaoService.setCallback(this);

        // Serviço da nova API (usa ServiceManager para compartilhar instância)
        programacaoAPIService = ServiceManager.getProgramacaoService();
        programacaoAPIService.setCallback(this);
    }

    private void initViews(@NonNull View view) {
        // RecyclerView e SwipeRefreshLayout
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        hintTextView = view.findViewById(R.id.hint);

        // Spinners
        spinnerWeekday = view.findViewById(R.id.spinner_weekday);
        spinnerRegion = view.findViewById(R.id.spinner_region);

        // Botões de navegação do header
        btBack = view.findViewById(R.id.bt_back);
        btHome = view.findViewById(R.id.bt_home);
        btMenu = view.findViewById(R.id.bt_menu);
        btNotif = view.findViewById(R.id.bt_notif_program);

        // Navbar buttons
        btPromotion = view.findViewById(R.id.bt_promotion);
        btNews = view.findViewById(R.id.bt_news);
        btRadio = view.findViewById(R.id.bt_radio);
        btProgram = view.findViewById(R.id.bt_program);
        btWhatsapp = view.findViewById(R.id.bt_whatsapp);

        // WebView do banner
        webView = view.findViewById(R.id.webView);
    }

    private void setupSpinners() {
        // Spinner de dias da semana
        final List<DiaSemanaOption> dias = DiaSemanaOption.getTodos();
        ArrayAdapter<DiaSemanaOption> diaAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_weekday,
                android.R.id.text1,
                dias
        );
        // Define layout personalizado para o dropdown
        diaAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerWeekday.setAdapter(diaAdapter);
        
        // Seleciona "TODOS" após configurar o adapter e ajusta posição do dropdown
        spinnerWeekday.post(() -> {
            spinnerWeekday.selectItemByIndex(0);
            // Posiciona o dropdown logo abaixo do spinner
            int spinnerHeight = spinnerWeekday.getHeight();
            spinnerWeekday.setDropDownVerticalOffset(spinnerHeight);
        });

        // Spinner de regiões
        final List<RadioOption> radios = RadioOption.getTodas();
        ArrayAdapter<RadioOption> radioAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_weekday,
                android.R.id.text1,
                radios
        );
        // Define layout personalizado para o dropdown
        radioAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerRegion.setAdapter(radioAdapter);
        
        // Seleciona primeira rádio após configurar o adapter e ajusta posição do dropdown
        spinnerRegion.post(() -> {
            spinnerRegion.selectItemByIndex(0);
            // Posiciona o dropdown logo abaixo do spinner
            int spinnerHeight = spinnerRegion.getHeight();
            spinnerRegion.setDropDownVerticalOffset(spinnerHeight);
        });

        // Listeners dos spinners
        spinnerWeekday.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // Ignora se ainda não foi marcado como "dirty" (primeira seleção)
                if (spinnerWeekday.getSelectedItemPosition() == SearchableSpinner.NO_ITEM_SELECTED) {
                    return;
                }
                
                if (position >= 0 && position < dias.size()) {
                    DiaSemanaOption diaSelecionado = dias.get(position);
                    if (diaSelecionado != null && !diaSelecionado.equals(selectedDia)) {
                        selectedDia = diaSelecionado;
                        loadProgramacao();
                    }
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Nada a fazer
            }
        });

        spinnerRegion.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // Ignora se ainda não foi marcado como "dirty" (primeira seleção)
                if (spinnerRegion.getSelectedItemPosition() == SearchableSpinner.NO_ITEM_SELECTED) {
                    return;
                }
                
                if (position >= 0 && position < radios.size()) {
                    RadioOption radioSelecionada = radios.get(position);
                    if (radioSelecionada != null && !radioSelecionada.equals(selectedRadio)) {
                        selectedRadio = radioSelecionada;
                        loadProgramacao();
                    }
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Nada a fazer
            }
        });
    }

    private void setupRecyclerView() {
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new ScheduleAdapter(requireContext());
            recyclerView.setAdapter(adapter);

            // Listener para cliques nos itens
            adapter.setOnItemClickListener((programa, position) -> {
                openProgramaDetail(programa);
            });
        }
    }

    private void setupListeners() {
        // SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadProgramacao();
            });
        }

        // Botões do header
        if (btBack != null) {
            btBack.setOnClickListener(this);
        }
        if (btHome != null) {
            btHome.setOnClickListener(this);
        }
        if (btMenu != null) {
            btMenu.setOnClickListener(this);
        }
        if (btNotif != null) {
            btNotif.setOnClickListener(this);
        }

        // Navbar buttons
        if (btPromotion != null) {
            btPromotion.setOnClickListener(this);
        }
        if (btNews != null) {
            btNews.setOnClickListener(this);
        }
        if (btRadio != null) {
            btRadio.setOnClickListener(this);
        }
        if (btProgram != null) {
            btProgram.setOnClickListener(this);
        }
        if (btWhatsapp != null) {
            btWhatsapp.setOnClickListener(this);
        }
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

    @Override
    public void onStart() {
        super.onStart();
        // Inicia o serviço legado (atualiza programa atual automaticamente)
        programacaoService.start();

        // Carrega programação da nova API
        loadProgramacao();
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onPause() {
        super.onPause();
        // Pausa o WebView do banner quando o fragment é pausado
        if (webView != null && webView.getVisibility() == View.VISIBLE) {
            webView.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Para o serviço legado
        programacaoService.stop();
    }

    /**
     * Carrega a programação da nova API com base nas seleções atuais
     */
    public void loadProgramacao() {
        if (selectedRadio != null) {
            String dia = selectedDia != null ? selectedDia.getId() : "";
            programacaoAPIService.fetchProgramacao(selectedRadio.getId(), dia);
        }
    }

    /**
     * Atualiza a rádio selecionada e recarrega a programação
     */
    public void setSelectedRadio(RadioOption radio) {
        this.selectedRadio = radio;
        loadProgramacao();
    }

    /**
     * Atualiza o dia selecionado e recarrega a programação
     */
    public void setSelectedDia(DiaSemanaOption dia) {
        this.selectedDia = dia;
        loadProgramacao();
    }

    // ==================== View.OnClickListener ====================

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bt_back) {
            navigateBack();
        } else if (btHome != null && v == btHome) {
            // Home - navega para MainFragment
            navigateToMain();
        } else if (btMenu != null && v == btMenu) {
            openMenu();
        } else if (btNotif != null && v == btNotif) {
            navigateToNotfProgram();
        } else if (id == R.id.bt_promotion) {
            navigateToPromotions();
        } else if (id == R.id.bt_news) {
            navigateToNews();
        } else if (id == R.id.bt_radio) {
            navigateToAudio();
        } else if (id == R.id.bt_program) {
            // Já está na tela de programação
            // Não faz nada ou pode mostrar feedback visual
        } else if (id == R.id.bt_whatsapp) {
            openWhatsAppFromFirebase();
        }
    }

    private void navigateBack() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).handleBackPress();
        }
    }

    private void navigateToMain() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
        }
    }

    private void navigateToPromotions() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.PROMOTION);
        }
    }

    private void navigateToAudio() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.AUDIO);
        }
    }

    private void navigateToNews() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NEWS);
        }
    }

    private void navigateToNotifications() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTIFICATION);
        }
    }

    private void navigateToNotfProgram() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTF_PROGRAM);
        }
    }

    private void openMenu() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openMenu();
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

    private void openProgramaDetail(ProgramaAPI programa) {
        Intent intent = new Intent(requireContext(), br.com.fivecom.litoralfm.ui.schedule.DetailProgramaActivity.class);
        intent.putExtra("programa_item", programa);
        // Passa o nome da rádio selecionada
        if (selectedRadio != null) {
            intent.putExtra("nome_radio", selectedRadio.getNome());
        }
        startActivity(intent);
    }

    // ==================== ProgramacaoService.ProgramacaoCallback ====================

    @Override
    public void onProgramaAtualChanged(Programa programa) {
        if (programa != null) {
            Log.d(TAG, "Programa atual: " + programa.getNome() +
                    " - " + programa.getApresentadores() +
                    " (" + programa.getHorario() + ")");

            // TODO: Atualizar UI com programa atual (live_item_layout)
            // Pode atualizar o layout "live_item_layout" se necessário
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.d(TAG, "Loading: " + isLoading);

        // Atualizar indicador de carregamento
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(isLoading);
        }
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Erro: " + errorMessage);

        // Mostrar mensagem de erro
        if (hintTextView != null) {
            hintTextView.setText(errorMessage);
            hintTextView.setVisibility(View.VISIBLE);
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (isAdded()) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== ProgramacaoAPIService.ProgramacaoAPICallback ====================

    @Override
    public void onProgramasLoaded(List<ProgramaAPI> programas) {
        Log.d(TAG, "Programas carregados: " + programas.size());

        for (ProgramaAPI programa : programas) {
            Log.d(TAG, "  - " + programa.getTitle() +
                    " (" + programa.getHrInicio() + " - " + programa.getHrFinal() + ")");
        }

        // Atualizar RecyclerView com a lista de programas
        if (adapter != null) {
            adapter.updateProgramas(programas);
        }

        // Atualizar UI
        if (programas.isEmpty()) {
            if (hintTextView != null) {
                hintTextView.setText("Nenhuma programação disponível");
                hintTextView.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        } else {
            if (hintTextView != null) {
                hintTextView.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}

