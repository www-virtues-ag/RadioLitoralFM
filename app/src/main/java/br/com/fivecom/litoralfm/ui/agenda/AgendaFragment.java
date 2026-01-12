package br.com.fivecom.litoralfm.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.agenda.AgendaItem;
import br.com.fivecom.litoralfm.services.AgendaService;
import br.com.fivecom.litoralfm.utils.ServiceManager;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.models.Data;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

/**
 * Fragment for displaying agenda/events
 * Based on Swift AgendaView implementation
 */
public class AgendaFragment extends Fragment implements AgendaService.AgendaCallback {

    // Views
    private RecyclerView recyclerAgenda;
    private TextView txtEmptyAgenda;
    private TextView txtEmptyAgenda2;

    // Buttons
    private ImageView btnVoltar;
    private ImageView btnHome;
    private ImageView btnNotificacoes;
    private ImageView btnMenu;
    
    // Navbar buttons (agora são LinearLayout no include)
    private View btPromotion;
    private View btnNews;
    private View btnRadio;
    private View btProgram;
    private View btWhatsapp;

    // Service and Data
    private AgendaService agendaService;
    private AgendaAdapter adapter;
    private List<AgendaItem> agendaList = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usa ServiceManager para compartilhar instância
        agendaService = ServiceManager.getAgendaService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Setup listeners
        setupListeners();

        // Fetch data if needed
        if (agendaList.isEmpty()) {
            fetchData();
        }
    }

    private void initViews(View view) {
        // Buttons (IDs corretos do layout)
        btnVoltar = view.findViewById(R.id.bt_back);
        btnHome = view.findViewById(R.id.bt_home);
        btnNotificacoes = view.findViewById(R.id.bt_notif);
        btnMenu = view.findViewById(R.id.bt_menu);
        
        // Navbar buttons
        btPromotion = view.findViewById(R.id.bt_promotion);
        btnNews = view.findViewById(R.id.bt_news);
        btnRadio = view.findViewById(R.id.bt_radio);
        btProgram = view.findViewById(R.id.bt_program);
        btWhatsapp = view.findViewById(R.id.bt_whatsapp);

        // Setup RecyclerView with GridLayoutManager for 2 columns
        recyclerAgenda = view.findViewById(R.id.rv_agenda);
        recyclerAgenda.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new AgendaAdapter(requireContext(), agendaList);
        recyclerAgenda.setAdapter(adapter);

        // Set click listener
        adapter.setOnAgendaItemClickListener(this::openAgendaDetail);

        // TextViews para mensagem de lista vazia
        txtEmptyAgenda = view.findViewById(R.id.txt_empty_agenda);
        txtEmptyAgenda2 = view.findViewById(R.id.txt_empty_agenda2);
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
        if (btPromotion != null) {
            btPromotion.setOnClickListener(v -> navigateToPromotion());
        }
        if (btnNews != null) {
            btnNews.setOnClickListener(v -> navigateToNews());
        }
        if (btnRadio != null) {
            btnRadio.setOnClickListener(v -> navigateToAudio());
        }
        if (btProgram != null) {
            btProgram.setOnClickListener(v -> navigateToSchedule());
        }
        if (btWhatsapp != null) {
            btWhatsapp.setOnClickListener(v -> openWhatsApp());
        }
    }

    private void fetchData() {
        showLoading(true);
        agendaService.fetchAgenda(this);
    }

    private void showLoading(boolean show) {
        if (recyclerAgenda != null) {
            recyclerAgenda.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        // Mostrar/esconder ProgressBar
        View progressBar = getView() != null ? getView().findViewById(R.id.progress_agenda) : null;
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateUI() {
        boolean isEmpty = agendaList.isEmpty();
        
        if (recyclerAgenda != null) {
            recyclerAgenda.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        
        // Mostrar/esconder mensagens de lista vazia
        if (txtEmptyAgenda != null) {
            txtEmptyAgenda.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (txtEmptyAgenda2 != null) {
            txtEmptyAgenda2.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        
        // Esconder ProgressBar
        View progressBar = getView() != null ? getView().findViewById(R.id.progress_agenda) : null;
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        
        if (adapter != null && !agendaList.isEmpty()) {
            adapter.updateAgenda(agendaList);
        }
    }


    private void openAgendaDetail(AgendaItem item) {
        Intent intent = new Intent(requireContext(), DetailAgendaActivity.class);
        intent.putExtra("agenda_item", item);
        startActivity(intent);
    }

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
    }

    private void goHome() {
        // Navigate to home (MainFragment)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
        }
    }

    private void openNotifications() {
        // Navigate to notifications (programs saved)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTF_PROGRAM);
        }
    }

    private void openMenu() {
        // Navigate to menu (ChooseFragment)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.CHOOSE);
        }
    }

    private void navigateToPromotion() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.PROMOTION);
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

    private void navigateToNews() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NEWS);
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


    // AgendaCallback implementation
    @Override
    public void onSuccess(List<AgendaItem> agenda) {
        agendaList.clear();
        agendaList.addAll(agenda);
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
