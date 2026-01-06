package br.com.fivecom.litoralfm.ui.promocao;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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

/**
 * Fragment para exibir promoções
 * Equivalente ao PromoView do Swift
 */
public class PromocaoFragment extends Fragment implements PromocaoService.PromocaoCallback {

    // Views
    private RecyclerView recyclerPromocoes;
    private ProgressBar progressPromocoes;
    private TextView txtEmptyPromocoes;

    // Buttons
    private ImageView btnVoltar;
    private ImageView btnHome;
    private ImageView btnNotificacoes;
    private ImageView btnMenu;
    
    // Navbar buttons
    private ImageView btnNav1, btnNav2, btnNav3, btnNav4, btnNav5;

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
        btnNav1 = view.findViewById(R.id.btnNav1);
        btnNav2 = view.findViewById(R.id.btnNav2);
        btnNav3 = view.findViewById(R.id.btnNav3);
        btnNav4 = view.findViewById(R.id.btnNav4);
        btnNav5 = view.findViewById(R.id.btnNav5);

        // RecyclerView
        recyclerPromocoes = view.findViewById(R.id.rv_promo);
        progressPromocoes = view.findViewById(R.id.progress_promo);
        txtEmptyPromocoes = view.findViewById(R.id.txt_empty_promo);
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
            btnNav2.setOnClickListener(v -> {
                // Notícias - em breve
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Notícias - em breve", Toast.LENGTH_SHORT).show();
                }
            });
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
        // Esconder mensagem de lista vazia durante o carregamento
        if (txtEmptyPromocoes != null) {
            txtEmptyPromocoes.setVisibility(View.GONE);
        }
    }

    private void updateUI() {
        boolean isEmpty = promocoesList.isEmpty();

        if (recyclerPromocoes != null) {
            recyclerPromocoes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // Mostrar/esconder mensagem de lista vazia
        if (txtEmptyPromocoes != null) {
            txtEmptyPromocoes.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/5527999999999"));
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Se o WhatsApp não estiver instalado, tenta abrir no navegador
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/5527999999999"));
                startActivity(intent);
            } catch (Exception ex) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Erro ao abrir WhatsApp", Toast.LENGTH_SHORT).show();
                }
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
        // Show error message
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
        // Show empty state on error
        updateUI();
    }
}

