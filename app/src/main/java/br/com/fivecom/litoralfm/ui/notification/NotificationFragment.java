package br.com.fivecom.litoralfm.ui.notification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import br.com.fivecom.litoralfm.databinding.FragmentNotificationBinding;
import br.com.fivecom.litoralfm.ui.main.MainActivity;

public class NotificationFragment extends Fragment {

    private static final String PREFS = "app_prefs";
    private static final String KEY_NOTIF_SCREEN_DONE = "notif_screen_done";
    private static final String KEY_NOTIF_ENABLED = "notif_enabled";

    private FragmentNotificationBinding binding;
    private ActivityResultLauncher<String> requestNotifPermission;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestNotifPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    getPrefs().edit()
                            .putBoolean(KEY_NOTIF_ENABLED, isGranted)
                            .putBoolean(KEY_NOTIF_SCREEN_DONE, true)
                            .apply();

                    if (!isGranted) {
                        openAppNotificationSettings();
                    }

                    closeThisScreen();
                }
        );

        binding.inside.setOnClickListener(v -> enableNotifications());

        // Botão "Agora não" / "Mais tarde"
        binding.later.setOnClickListener(v -> {
            getPrefs().edit()
                    .putBoolean(KEY_NOTIF_SCREEN_DONE, true)
                    .putBoolean(KEY_NOTIF_ENABLED, false)
                    .apply();
            closeThisScreen();
        });
    }

    // =====================================================================
    // LÓGICA DE PERMISSÃO
    // =====================================================================

    private void enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ precisa de POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {

                getPrefs().edit()
                        .putBoolean(KEY_NOTIF_ENABLED, true)
                        .putBoolean(KEY_NOTIF_SCREEN_DONE, true)
                        .apply();

                closeThisScreen();
            } else {
                // Pede a permissão
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Versões anteriores não precisam da permissão em runtime
            getPrefs().edit()
                    .putBoolean(KEY_NOTIF_ENABLED, true)
                    .putBoolean(KEY_NOTIF_SCREEN_DONE, true)
                    .apply();

            closeThisScreen();
        }
    }

    private void openAppNotificationSettings() {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
            }
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    // =====================================================================
    // NAVEGAÇÃO PÓS-TELA DE NOTIFICAÇÃO
    // =====================================================================

    @OptIn(markerClass = UnstableApi.class)
    private void closeThisScreen() {
        // Agora a NotificationFragment está dentro da MainActivity
        // (aberta a partir da Splash com o extra "go_to_notification").
        // Então aqui a gente navega para o ChooseFragment.
        if (isAdded() && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity())
                    .navigateToFragment(MainActivity.FRAGMENT.CHOOSE);
        } else if (isAdded()) {
            // Fallback: se por algum motivo não for MainActivity,
            // apenas fecha a Activity atual.
            requireActivity().finish();
        }
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
