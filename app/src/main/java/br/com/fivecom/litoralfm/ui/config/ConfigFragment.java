package br.com.fivecom.litoralfm.ui.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.onesignal.OneSignal;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.utils.dialog.NotificationEnabledDialog;
import br.com.fivecom.litoralfm.utils.dialog.AnimationPausedDialog;
import br.com.fivecom.litoralfm.utils.constants.Preferences;

/**
 * Fragment para configurações do app
 */
@OptIn(markerClass = UnstableApi.class)
public class ConfigFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ConfigFragment";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    // Views
    private ImageView btnBack;
    private ImageView switchNotifications;
    private ImageView switchStatic;
    private ImageView messageNotifications;
    private ImageView messageAnimations;
    private ImageView messageMode;
    private Preferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
        loadNotificationState();
        loadAnimationState();
    }

    private void initViews(@NonNull View view) {
        btnBack = view.findViewById(R.id.bt_back);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchStatic = view.findViewById(R.id.switch_static);
        messageNotifications = view.findViewById(R.id.message_notifications);
        messageAnimations = view.findViewById(R.id.message_animations);
        messageMode = view.findViewById(R.id.message_mode);
        preferences = new Preferences(requireContext());
    }

    private void setupClickListeners() {
        if (btnBack != null)
            btnBack.setOnClickListener(this);
        if (switchNotifications != null)
            switchNotifications.setOnClickListener(this);
        if (switchStatic != null)
            switchStatic.setOnClickListener(this);
        if (messageNotifications != null)
            messageNotifications.setOnClickListener(this);
        if (messageAnimations != null)
            messageAnimations.setOnClickListener(this);
        if (messageMode != null)
            messageMode.setOnClickListener(this);
    }

    /**
     * Carrega o estado salvo das notificações e atualiza a UI
     */
    private void loadNotificationState() {
        if (switchNotifications == null)
            return;

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true); // Default: true

        updateSwitchImage(notificationsEnabled);
        Log.d(TAG, "📱 Estado das notificações carregado: " + (notificationsEnabled ? "HABILITADO" : "DESABILITADO"));
    }

    /**
     * Atualiza a imagem do switch baseado no estado
     */
    private void updateSwitchImage(boolean enabled) {
        if (switchNotifications == null)
            return;

        int drawableRes = enabled ? R.drawable.switch_on : R.drawable.switch_off;
        switchNotifications.setImageResource(drawableRes);
    }

    /**
     * Alterna o estado das notificações
     */
    private void toggleNotifications() {
        if (switchNotifications == null)
            return;

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean currentState = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        boolean newState = !currentState;

        // Salva no SharedPreferences
        prefs.edit()
                .putBoolean(KEY_NOTIFICATIONS_ENABLED, newState)
                .apply();

        // Atualiza a UI
        updateSwitchImage(newState);

        // Atualiza o OneSignal
        updateOneSignalNotifications(newState);

        // Mostra dialog quando as notificações forem habilitadas
        if (newState && isAdded() && getContext() != null) {
            NotificationEnabledDialog.show(requireContext());
        }

        Log.d(TAG, "🔄 Notificações " + (newState ? "HABILITADAS" : "DESABILITADAS"));
    }

    /**
     * Carrega o estado salvo das animações e atualiza a UI
     * Lógica invertida: switch_on = animações pausadas, switch_off = animações
     * rodando
     */
    private void loadAnimationState() {
        if (switchStatic == null)
            return;

        // animationEnabled = true significa animações rodando (switch_off)
        // animationEnabled = false significa animações pausadas (switch_on)
        boolean animationEnabled = preferences.isAnimationEnabled();

        updateStaticSwitchImage(animationEnabled);
        Log.d(TAG, "🎬 Estado das animações carregado: "
                + (animationEnabled ? "RODANDO (switch_off)" : "PAUSADAS (switch_on)"));
    }

    /**
     * Atualiza a imagem do switch estático baseado no estado
     * Lógica invertida:
     * - Quando animationEnabled = true: switch_off (animações rodando normalmente)
     * - Quando animationEnabled = false: switch_on (animações pausadas - modo
     * estático)
     */
    private void updateStaticSwitchImage(boolean animationEnabled) {
        if (switchStatic == null)
            return;

        // Invertido: animationEnabled = true -> switch_off, animationEnabled = false ->
        // switch_on
        int drawableRes = animationEnabled ? R.drawable.switch_off : R.drawable.switch_on;
        switchStatic.setImageResource(drawableRes);
    }

    /**
     * Alterna o estado das animações (modo estático)
     * Lógica invertida: switch_on pausa, switch_off retoma
     */
    private void toggleAnimation() {
        if (switchStatic == null)
            return;

        boolean currentState = preferences.isAnimationEnabled();
        boolean newState = !currentState;

        // Salva no Preferences
        preferences.setAnimationEnabled(newState);

        // Atualiza a UI
        updateStaticSwitchImage(newState);

        // Mostra dialog quando as animações forem pausadas (switch_on)
        if (!newState && isAdded() && getContext() != null) {
            AnimationPausedDialog.show(requireContext());
        }

        Log.d(TAG, "🔄 Animações " + (newState ? "RODANDO (switch_off)" : "PAUSADAS (switch_on)"));

        // Notifica os fragments para atualizar os Lotties
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).notifyAnimationStateChanged(newState);
        }
    }

    /**
     * Atualiza o estado das notificações no OneSignal
     */
    private void updateOneSignalNotifications(boolean enabled) {
        try {
            if (enabled) {
                // Habilita notificações no OneSignal
                OneSignal.getUser().getPushSubscription().optIn();
                Log.d(TAG, "✅ OneSignal: Notificações habilitadas");
            } else {
                // Desabilita notificações no OneSignal
                OneSignal.getUser().getPushSubscription().optOut();
                Log.d(TAG, "❌ OneSignal: Notificações desabilitadas");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar OneSignal: " + e.getMessage(), e);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bt_back) {
            navigateBack();
        } else if (id == R.id.switch_notifications) {
            toggleNotifications();
        } else if (id == R.id.switch_static) {
            toggleAnimation();
        } else if (id == R.id.message_notifications) {
            showInfoDialog("AVISO",
                    "Receba alertas sobre notícias, programação e novidades da rádio diretamente no seu dispositivo");
        } else if (id == R.id.message_animations) {
            showInfoDialog("AVISO", "Controle as animações do app para economizar bateria ou melhorar a performance");
        } else if (id == R.id.message_mode) {
            showInfoDialog("AVISO", "Escolha entre modo claro ou escuro para melhor conforto");
        }
    }

    /**
     * Exibe um diálogo informativo customizado
     */
    private void showInfoDialog(String title, String message) {
        if (getContext() == null)
            return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_locutores_aviso, null);

        android.widget.TextView txtTitle = view.findViewById(R.id.txt_titulo);
        android.widget.TextView txtMessage = view.findViewById(R.id.txt_mensagem);
        android.widget.Button btnOk = view.findViewById(R.id.btn_ok);

        if (txtTitle != null)
            txtTitle.setText(title);
        if (txtMessage != null)
            txtMessage.setText(message);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (btnOk != null) {
            btnOk.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.setView(view);
        dialog.show();
    }

    /**
     * Navega de volta para o fragment anterior
     */
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
}
