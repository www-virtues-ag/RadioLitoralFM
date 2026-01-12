package br.com.fivecom.litoralfm.ui.promocao;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.promocao.Promocao;
import br.com.fivecom.litoralfm.services.PromocaoService;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.models.Data;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

/**
 * Fragment para exibir promoções
 * Equivalente ao PromoView do Swift
 */
public class PromocaoFragment extends Fragment implements PromocaoService.PromocaoCallback {

    // Views
    private RecyclerView recyclerPromocoes;
    private ProgressBar progressPromocoes;
    private TextView txtEmptyPromocoes;
    private TextView txtEmptyPromocoes2;

    // Buttons
    private ImageView btnVoltar;
    private ImageView btnHome;
    private ImageView btnNotificacoes;
    private ImageView btnMenu;
    
    // Navbar buttons (agora são LinearLayout no include)
    private View btnNav1, btnNav2, btnNav3, btnNav4, btnNav5;

    // Service and Data
    private PromocaoService promocaoService;
    private PromocaoAdapter adapter;
    private List<Promocao> promocoesList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        promocaoService = new PromocaoService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promotion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Setup listeners
        setupListeners();

        // Setup RecyclerView
        setupRecyclerView(view);

        // Fetch data if needed
        if (promocoesList.isEmpty()) {
            fetchData();
        }
    }

    private void initViews(View view) {
        // Buttons
        btnVoltar = view.findViewById(R.id.bt_back);
        btnHome = view.findViewById(R.id.bt_home);
        btnNotificacoes = view.findViewById(R.id.bt_notif);
        btnMenu = view.findViewById(R.id.bt_menu);
        
        // Navbar buttons
        btnNav1 = view.findViewById(R.id.bt_promotion);
        btnNav2 = view.findViewById(R.id.bt_news);
        btnNav3 = view.findViewById(R.id.bt_radio);
        btnNav4 = view.findViewById(R.id.bt_program);
        btnNav5 = view.findViewById(R.id.bt_whatsapp);

        // RecyclerView
        recyclerPromocoes = view.findViewById(R.id.rv_promo);
        progressPromocoes = view.findViewById(R.id.progress_promo);
        txtEmptyPromocoes = view.findViewById(R.id.txt_empty_promo);
        txtEmptyPromocoes2 = view.findViewById(R.id.txt_empty_promo2);
    }

    private void setupRecyclerView(@NonNull View view) {
        if (recyclerPromocoes != null) {
            recyclerPromocoes.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new PromocaoAdapter(requireContext(), promocoesList);
            recyclerPromocoes.setAdapter(adapter);

            // Set click listener para abrir link
            adapter.setOnPromocaoClickListener(this::openPromocaoLink);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupListeners() {
        // Navigation buttons
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> navigateBack());
        }
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> goHome());
        }
        if (btnNotificacoes != null) {
            btnNotificacoes.setOnClickListener(v -> openNotifications());
        }
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openMenu();
                }
            });
        }
        
        // Navbar buttons
        if (btnNav1 != null) {
            btnNav1.setOnClickListener(v -> {
                // Já está na tela de promoções
            });
        }
        if (btnNav2 != null) {
            btnNav2.setOnClickListener(v -> navigateToNews());
        }
        if (btnNav3 != null) {
            btnNav3.setOnClickListener(v -> navigateToAudio());
        }
        if (btnNav4 != null) {
            btnNav4.setOnClickListener(v -> navigateToSchedule());
        }
        if (btnNav5 != null) {
            btnNav5.setOnClickListener(v -> openWhatsApp());
        }
    }

    private void fetchData() {
        showLoading(true);
        promocaoService.fetchPromocoes(this);
    }

    private void showLoading(boolean show) {
        if (progressPromocoes != null) {
            progressPromocoes.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerPromocoes != null) {
            recyclerPromocoes.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        // Esconder mensagens de lista vazia durante o carregamento
        if (txtEmptyPromocoes != null) {
            txtEmptyPromocoes.setVisibility(View.GONE);
        }
        if (txtEmptyPromocoes2 != null) {
            txtEmptyPromocoes2.setVisibility(View.GONE);
        }
    }

    private void updateUI() {
        boolean isEmpty = promocoesList.isEmpty();

        if (recyclerPromocoes != null) {
            recyclerPromocoes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // Mostrar/esconder mensagens de lista vazia
        if (txtEmptyPromocoes != null) {
            txtEmptyPromocoes.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (txtEmptyPromocoes2 != null) {
            txtEmptyPromocoes2.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        // Esconder ProgressBar
        if (progressPromocoes != null) {
            progressPromocoes.setVisibility(View.GONE);
        }

        if (adapter != null && !promocoesList.isEmpty()) {
            adapter.updatePromocoes(promocoesList);
        }
    }

    private void openPromocaoLink(Promocao promocao) {
        // Abre a tela de detalhes da promoção
        Intent intent = new Intent(requireContext(), DetailPromocaoActivity.class);
        intent.putExtra("promocao_item", promocao);
        startActivity(intent);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void navigateBack() {
        // Usa handleBackPress para navegação correta com back stack
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).handleBackPress();
            return;
        }
        // Fallback caso não seja MainActivity
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
        // Navega para o MainFragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void goHome() {
        // Navigate to home (MainFragment)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openNotifications() {
        // Navigate to notifications (programs saved)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTF_PROGRAM);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void navigateToNews() {
        // Navega para o NewsFragment (fragment_new)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NEWS);
        }
    }

    private void navigateToAudio() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.AUDIO);
        }
    }

    private void navigateToSchedule() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.SCHEDULE);
        }
    }

    private void openWhatsApp() {
        if (data == null || data.radios == null || Constants.ID < 0 || Constants.ID >= data.radios.size()) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Dados da rádio não disponíveis", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Data.Radios radio = data.radios.get(Constants.ID);
        if (radio.whatsapp != null && !radio.whatsapp.isEmpty()) {
            Intents.app(requireContext(), Intents.Social.WHATSAPP, radio.whatsapp);
        } else {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "WhatsApp não configurado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // PromocaoCallback implementation
    @Override
    public void onSuccess(List<Promocao> promocoes) {
        promocoesList.clear();
        promocoesList.addAll(promocoes);
        showLoading(false);
        updateUI();
    }

    @Override
    public void onError(String error) {
        showLoading(false);
        // Show empty state on error (sem toast)
        updateUI();
    }
}

