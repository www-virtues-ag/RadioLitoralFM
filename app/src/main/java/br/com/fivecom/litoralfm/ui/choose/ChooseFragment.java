package br.com.fivecom.litoralfm.ui.choose;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.support.v4.media.session.MediaControllerCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.databinding.FragmentChooseBinding;
import br.com.fivecom.litoralfm.databinding.ItemRadioSpinnerBinding;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.constants.Extras;

public class ChooseFragment extends Fragment {

    private FragmentChooseBinding binding;

    private static final String PREFS = "app_prefs";
    private static final String KEY_RADIO_ID = "selected_radio_id";

    private final List<RadioOption> options = new ArrayList<>();
    private int selectedRadioId = 10224; // SUL por padrão
    private int currentIndex = 0; // Índice atual da rádio selecionada na lista

    // Handler único do fragment para evitar callbacks rodando após destroy
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingNavigateRunnable;

    private RadioRecyclerAdapter adapter;

    // Modelo simples para as rádios
    private static class RadioOption {
        final String name;
        final int id;

        RadioOption(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }

    // ======================= CICLO DE VIDA ======================= //

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChooseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupOptionsFromRemoteConfig();
        setupRecyclerView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancela qualquer navegação pendente (evita crash "not attached")
        if (pendingNavigateRunnable != null) {
            uiHandler.removeCallbacks(pendingNavigateRunnable);
            pendingNavigateRunnable = null;
        }
        uiHandler.removeCallbacksAndMessages(null);

        binding = null;
    }

    // ======================= OPÇÕES (REMOTE CONFIG) ======================= //

    private void setupOptionsFromRemoteConfig() {
        options.clear();

        // Ordem específica: NOROESTE, GRANDE VITÓRIA, NORTE, SUL
        int[] orderedIds = {10225, 10223, 10226, 10224}; // NOROESTE, GRANDE VITÓRIA, NORTE, SUL

        // 1) Tenta usar os dados vindos do remote_config (Constants.data)
        if (data != null && data.radios != null && !data.radios.isEmpty()) {
            // Cria um mapa temporário para facilitar a busca
            Map<Integer, RadioOption> radioMap = new HashMap<>();
            
            for (int i = 0; i < data.radios.size(); i++) {
                try {
                    String idStr = data.radios.get(i).id;
                    if (idStr == null) continue;

                    int id = Integer.parseInt(idStr);
                    String regionalName = getRegionalNameById(id);
                    radioMap.put(id, new RadioOption(regionalName, id));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            
            // Adiciona na ordem especificada
            for (int id : orderedIds) {
                RadioOption option = radioMap.get(id);
                if (option != null) {
                    options.add(option);
                }
            }
        }

        // 2) Fallback se não tiver nada carregado - usa a ordem especificada
        if (options.isEmpty()) {
            options.add(new RadioOption("NOROESTE", 10225));
            options.add(new RadioOption("GRANDE VITÓRIA", 10223));
            options.add(new RadioOption("NORTE", 10226));
            options.add(new RadioOption("SUL", 10224));
        }

        // 3) Carrega última rádio salva nas preferências
        Context ctx = getContext();
        if (ctx != null) {
            selectedRadioId = getPrefs(ctx).getInt(KEY_RADIO_ID, selectedRadioId);
        }

        // Encontra o índice da rádio salva
        currentIndex = findIndexById(selectedRadioId);
        if (currentIndex < 0) {
            currentIndex = 0;
            if (!options.isEmpty()) {
                selectedRadioId = options.get(0).id;
            }
        }

        // Atualiza Constants.ID baseado no ID salvo
        updateConstantsIdFromRadioId(selectedRadioId);
    }

    /**
     * Mapeia o ID da rádio para o nome regional
     */
    private String getRegionalNameById(int radioId) {
        switch (radioId) {
            case 10223:
                return "GRANDE VITÓRIA";
            case 10224:
                return "SUL";
            case 10225:
                return "NOROESTE";
            case 10226:
                return "NORTE";
            default:
                return "RÁDIO LITORAL";
        }
    }

    // ======================= RECYCLER VIEW ======================= //

    private void setupRecyclerView() {
        if (binding == null || binding.rvRadios == null) return;

        Context ctx = getContext();
        if (ctx == null) return;

        adapter = new RadioRecyclerAdapter(options, selectedRadioId, this::onRadioSelected);
        
        // LayoutManager vertical com centralização horizontal
        LinearLayoutManager layoutManager = new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false);
        binding.rvRadios.setLayoutManager(layoutManager);
        binding.rvRadios.setAdapter(adapter);
    }

    private void onRadioSelected(RadioOption option, int position) {
        if (option == null) return;

        selectedRadioId = option.id;
        currentIndex = position;

        // Atualiza o adapter para refletir a nova seleção
        if (adapter != null) {
            adapter.setSelectedRadioId(selectedRadioId);
        }

        saveRadioSelectionSafe();
        updateConstantsIdFromRadioId(selectedRadioId);

        // Força o recarregamento do player de áudio quando a rádio muda
        reloadAudioPlayer();

        // Cancela navegação pendente anterior
        if (pendingNavigateRunnable != null) {
            uiHandler.removeCallbacks(pendingNavigateRunnable);
        }

        // Agenda navegação com segurança
        pendingNavigateRunnable = () -> navigateToMainFragmentSafe();
        uiHandler.postDelayed(pendingNavigateRunnable, 500);
    }

    /**
     * Força o recarregamento do player de áudio quando a rádio é trocada
     */
    private void reloadAudioPlayer() {
        try {
            if (getActivity() == null) return;

            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            if (controller != null && Constants.ID >= 0) {
                // Usa onPlayFromMediaId para forçar o recarregamento do player
                // Isso chama onRewind() no MediaService que para e recomeça o player
                String mediaId = String.valueOf(Constants.ID);
                Log.d("ChooseFragment", "🔄 Recarregando player para rádio ID: " + mediaId + " (índice: " + Constants.ID + ")");
                controller.getTransportControls().playFromMediaId(mediaId, null);
            }
        } catch (Exception e) {
            Log.e("ChooseFragment", "Erro ao recarregar player: " + e.getMessage());
        }
    }

    private void saveRadioSelectionSafe() {
        Context ctx = getContext();
        if (ctx == null) return;

        getPrefs(ctx)
                .edit()
                .putInt(KEY_RADIO_ID, selectedRadioId)
                .apply();
    }

    // ======================= ADAPTER ======================= //

    private static class RadioRecyclerAdapter extends RecyclerView.Adapter<RadioRecyclerAdapter.RadioViewHolder> {

        private final List<RadioOption> options;
        private int selectedRadioId;
        private final OnRadioSelectedListener listener;

        interface OnRadioSelectedListener {
            void onRadioSelected(RadioOption option, int position);
        }

        RadioRecyclerAdapter(List<RadioOption> options, int selectedRadioId, OnRadioSelectedListener listener) {
            this.options = options;
            this.selectedRadioId = selectedRadioId;
            this.listener = listener;
        }

        void setSelectedRadioId(int radioId) {
            int oldPosition = findPositionById(selectedRadioId);
            selectedRadioId = radioId;
            int newPosition = findPositionById(selectedRadioId);
            
            if (oldPosition >= 0) {
                notifyItemChanged(oldPosition);
            }
            if (newPosition >= 0 && newPosition != oldPosition) {
                notifyItemChanged(newPosition);
            }
        }

        private int findPositionById(int id) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).id == id) {
                    return i;
                }
            }
            return -1;
        }

        @NonNull
        @Override
        public RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemRadioSpinnerBinding binding = ItemRadioSpinnerBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new RadioViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull RadioViewHolder holder, int position) {
            RadioOption option = options.get(position);
            if (option == null) return;

            boolean isSelected = option.id == selectedRadioId;

            // Define o texto
            holder.binding.radioTitle.setText(option.name);

            // Configura visibilidade e opacidade
            if (isSelected) {
                // Rádio selecionada: mostra fl_location e vw_space, opacidade normal
                holder.binding.flLocation.setVisibility(View.VISIBLE);
                holder.binding.vwSpace.setVisibility(View.VISIBLE);
                holder.binding.container.setAlpha(1.0f);
            } else {
                // Rádio não selecionada: esconde fl_location e vw_space, opacidade reduzida
                holder.binding.flLocation.setVisibility(View.GONE);
                holder.binding.vwSpace.setVisibility(View.GONE);
                holder.binding.container.setAlpha(0.5f);
            }

            // Configura o clique
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRadioSelected(option, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        static class RadioViewHolder extends RecyclerView.ViewHolder {
            final ItemRadioSpinnerBinding binding;

            RadioViewHolder(@NonNull ItemRadioSpinnerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    /**
     * Versão segura: só navega se o fragment ainda estiver attached.
     */
    @OptIn(markerClass = UnstableApi.class)
    private void navigateToMainFragmentSafe() {
        // Se o fragment já não está mais ativo, não faz nada (evita crash)
        if (!isAdded() || getActivity() == null) return;

        // binding pode estar null após destroyView
        if (binding == null) return;

        // Salva a seleção final
        saveRadioSelectionSafe();
        updateConstantsIdFromRadioId(selectedRadioId);

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.navigateToFragment(MainActivity.FRAGMENT.MAIN);
        } else {
            Context ctx = getContext();
            if (ctx == null) return;

            Intent intent = new Intent(ctx, MainActivity.class);
            intent.putExtra(Extras.ID.name(), selectedRadioId);
            startActivity(intent);

            // Só chama finish se activity ainda existe
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    // ======================= HELPERS ======================= //

    private SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private int findIndexById(int id) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).id == id) return i;
        }
        return -1;
    }

    /**
     * Atualiza Constants.ID (índice dentro de data.radios) baseado no ID inteiro da rádio.
     */
    private void updateConstantsIdFromRadioId(int radioId) {
        if (data == null || data.radios == null || data.radios.isEmpty()) {
            Constants.ID = 0;
            return;
        }

        String radioIdStr = String.valueOf(radioId);
        int foundIndex = -1;
        for (int i = 0; i < data.radios.size(); i++) {
            if (data.radios.get(i).id != null &&
                    data.radios.get(i).id.equals(radioIdStr)) {
                foundIndex = i;
                break;
            }
        }

        Constants.ID = (foundIndex >= 0) ? foundIndex : 0;
    }
}
