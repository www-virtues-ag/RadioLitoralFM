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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import android.support.v4.media.session.MediaControllerCompat;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.databinding.FragmentChooseBinding;
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

    // Evita disparar navegação no spinner durante setup inicial
    private boolean ignoreNextSpinnerEvent = false;

    // Modelo simples para o spinner
    private static class RadioOption {
        final String name;
        final int id;

        RadioOption(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
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
        setupSpinner(binding.spinnerRegiao);
        setupActions();
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

        // 1) Tenta usar os dados vindos do remote_config (Constants.data)
        if (data != null && data.radios != null && !data.radios.isEmpty()) {
            for (int i = 0; i < data.radios.size(); i++) {
                try {
                    String idStr = data.radios.get(i).id;
                    if (idStr == null) continue;

                    int id = Integer.parseInt(idStr);

                    // Nome regional amigável
                    String regionalName = getRegionalNameById(id);

                    options.add(new RadioOption(regionalName, id));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        // 2) Fallback se não tiver nada carregado
        if (options.isEmpty()) {
            options.add(new RadioOption("GRANDE VITÓRIA", 10223));
            options.add(new RadioOption("SUL", 10224));
            options.add(new RadioOption("NOROESTE", 10225));
            options.add(new RadioOption("NORTE", 10226));
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

    // ======================= SPINNER ======================= //

    private void setupSpinner(Spinner spinner) {
        Context ctx = getContext();
        if (ctx == null) return;

        RadioSpinnerAdapter adapter = new RadioSpinnerAdapter(ctx, options);
        spinner.setAdapter(adapter);

        // Seleciona no spinner a rádio atual (sem disparar listener)
        if (currentIndex >= 0 && currentIndex < options.size()) {
            ignoreNextSpinnerEvent = true;
            spinner.setSelection(currentIndex, false);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view,
                                       int position,
                                       long id) {

                // Ignora o primeiro evento do spinner ao setar seleção inicial
                if (ignoreNextSpinnerEvent) {
                    ignoreNextSpinnerEvent = false;
                    return;
                }

                if (position >= 0 && position < options.size()) {
                    RadioOption option = options.get(position);
                    selectedRadioId = option.id;
                    currentIndex = position;

                    saveRadioSelectionSafe();
                    updateConstantsIdFromRadioId(selectedRadioId);

                    // Navega automaticamente
                    navigateToMainFragmentSafe();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nada
            }
        });
    }

    private void navigateToPreviousRadio() {
        if (options.isEmpty()) return;

        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = options.size() - 1;
        }

        updateSelectedRadio();
    }

    private void navigateToNextRadio() {
        if (options.isEmpty()) return;

        currentIndex++;
        if (currentIndex >= options.size()) {
            currentIndex = 0;
        }

        updateSelectedRadio();
    }

    private void updateSelectedRadio() {
        if (currentIndex >= 0 && currentIndex < options.size()) {
            RadioOption option = options.get(currentIndex);
            selectedRadioId = option.id;

            // Atualiza spinner visualmente (sem disparar listener)
            if (binding != null && binding.spinnerRegiao != null) {
                ignoreNextSpinnerEvent = true;
                binding.spinnerRegiao.setSelection(currentIndex, false);
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

    private static class RadioSpinnerAdapter extends ArrayAdapter<RadioOption> {

        private final LayoutInflater inflater;

        RadioSpinnerAdapter(@NonNull Context context,
                            @NonNull List<RadioOption> items) {
            super(context, 0, items);
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position,
                            @Nullable View convertView,
                            @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.spinnner_item_radio, parent, false);
            }

            TextView tv = null;
            if (view != null) {
                View textView = view.findViewById(R.id.txtRadioNm);
                if (textView instanceof TextView) {
                    tv = (TextView) textView;
                }
            }

            RadioOption item = getItem(position);
            if (item != null && tv != null) {
                tv.setText(item.name);
            }

            return view;
        }

        @NonNull
        @Override
        public View getDropDownView(int position,
                                    @Nullable View convertView,
                                    @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.spinner_dropdown_radio, parent, false);
            }

            TextView tv = null;
            if (view != null) {
                View textView = view.findViewById(R.id.txtRadioNameDpdown);
                if (textView instanceof TextView) {
                    tv = (TextView) textView;
                }
            }

            RadioOption item = getItem(position);
            if (item != null && tv != null) {
                tv.setText(item.name);
            }

            return view;
        }
    }

    // ======================= AÇÕES ======================= //

    private void setupActions() {
        if (binding != null && binding.btUp != null) {
            binding.btUp.setOnClickListener(v -> navigateToPreviousRadio());
        }

        if (binding != null && binding.btDown != null) {
            binding.btDown.setOnClickListener(v -> navigateToNextRadio());
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
