package br.com.fivecom.litoralfm.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.ui.schedule.DetailProgramaActivity;
import br.com.fivecom.litoralfm.utils.SavedProgramsManager;

/**
 * Fragment para exibir os programas salvos pelo usuário
 */
public class NotfProgramFragment extends Fragment {

    private static final String TAG = "NotfProgramFragment";

    private RecyclerView rvNotfProgram;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtMarkAllRead;
    private ImageView imgMark;
    private View rlMark;
    private ImageView btBack;
    private ImageView btHome;
    private NotfProgramAdapter adapter;
    private SavedProgramsManager savedProgramsManager;
    private TextView txtEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedProgramsManager = new SavedProgramsManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notf_program, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadSavedPrograms();
    }

    private void initViews(@NonNull View view) {
        rvNotfProgram = view.findViewById(R.id.rv_notf_program);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        txtMarkAllRead = view.findViewById(R.id.txt_mark);
        imgMark = view.findViewById(R.id.img_mark);
        rlMark = view.findViewById(R.id.rl_mark);
        btBack = view.findViewById(R.id.bt_back);
        btHome = view.findViewById(R.id.bt_home);
        
        // TextView para mensagem de lista vazia (se não existir no layout, criar programaticamente)
        txtEmpty = view.findViewById(R.id.txt_empty_notf);
    }

    private void setupRecyclerView() {
        if (rvNotfProgram != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false);
            rvNotfProgram.setLayoutManager(layoutManager);

            adapter = new NotfProgramAdapter(requireContext());
            rvNotfProgram.setAdapter(adapter);

            // Listener para cliques nos itens
            adapter.setOnItemClickListener((programa, position) -> {
                // Marca o programa como lido quando clicado
                savedProgramsManager.markAsRead(programa);
                // Atualiza a UI
                loadSavedPrograms();
                // Abre os detalhes
                openProgramaDetail(programa);
            });
        }
    }

    private void setupListeners() {
        // SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadSavedPrograms();
            });
        }

        // Botão voltar
        if (btBack != null) {
            btBack.setOnClickListener(v -> navigateBack());
        }

        // Botão home
        if (btHome != null) {
            btHome.setOnClickListener(v -> navigateToMain());
        }

        // Marcar tudo como lido
        if (rlMark != null) {
            rlMark.setOnClickListener(v -> {
                markAllAsRead();
            });
        }
    }

    private void loadSavedPrograms() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        List<ProgramaAPI> savedPrograms = savedProgramsManager.getSavedPrograms();
        
        Log.d(TAG, "Programas salvos carregados: " + savedPrograms.size());

        if (adapter != null) {
            adapter.updateProgramas(savedPrograms);
        }

        // Atualizar UI
        updateEmptyState(savedPrograms.isEmpty());
        updateMarkAllReadButton();

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            if (rvNotfProgram != null) {
                rvNotfProgram.setVisibility(View.GONE);
            }
            // Se não tiver TextView de empty, pode criar uma mensagem ou deixar vazio
            if (txtEmpty != null) {
                txtEmpty.setVisibility(View.VISIBLE);
            }
        } else {
            if (rvNotfProgram != null) {
                rvNotfProgram.setVisibility(View.VISIBLE);
            }
            if (txtEmpty != null) {
                txtEmpty.setVisibility(View.GONE);
            }
        }
    }

    private void openProgramaDetail(ProgramaAPI programa) {
        Intent intent = new Intent(requireContext(), DetailProgramaActivity.class);
        intent.putExtra("programa_item", programa);
        // Pode passar o nome da rádio se necessário
        startActivity(intent);
    }

    private void markAllAsRead() {
        savedProgramsManager.markAllAsRead();
        loadSavedPrograms();
        if (isAdded()) {
            Toast.makeText(requireContext(), "Todos os programas foram marcados como lidos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkAllReadButton() {
        if (imgMark == null) return;

        List<ProgramaAPI> savedPrograms = savedProgramsManager.getSavedPrograms();
        boolean allRead = true;

        // Verifica se todos os programas foram lidos
        for (ProgramaAPI programa : savedPrograms) {
            if (!savedProgramsManager.isProgramRead(programa)) {
                allRead = false;
                break;
            }
        }

        // Atualiza a imagem do botão
        if (allRead && savedPrograms.size() > 0) {
            imgMark.setImageResource(R.drawable.rectangle_notf_actived);
        } else {
            imgMark.setImageResource(R.drawable.rectangle_notf);
        }
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

    private void navigateToMain() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarrega os programas quando o fragment volta a ficar visível
        // (caso algum programa tenha sido removido em outra tela ou marcado como lido)
        loadSavedPrograms();
    }
}

