package br.com.fivecom.litoralfm.ui.main;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.services.MediaService;
import br.com.fivecom.litoralfm.ui.removal.RemovalActivity;
import br.com.fivecom.litoralfm.ui.views.alerts.DefaultDialog;
import br.com.fivecom.litoralfm.utils.core.Config;

public abstract class MediaActivity extends AppCompatActivity implements View.OnClickListener {

    // Controller / Browser
    public MediaControllerCompat mController;
    private MediaBrowserCompat mMediaBrowser;

    // Rede
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    // Diálogo de erro genérico (se quiser usar depois)
    private AlertDialog.Builder erroDialog;

    // Áudio
    public AudioManager audioManager = null;

    // Lista de listeners (Fragments) que implementam MediaStateListener
    private final List<MediaStateListener> mediaStateListeners = new ArrayList<>();

    // Flag para evitar chamar connect() enquanto está CONNECTING
    private boolean isMediaBrowserConnecting = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        erroDialog = new AlertDialog.Builder(this).setCancelable(false);
        connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        isMediaBrowserConnecting = false;

        // Conexão com o MediaService via MediaBrowser
        mMediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MediaService.class),
                new MediaBrowserCompat.ConnectionCallback() {

                    @OptIn(markerClass = UnstableApi.class)
                    @Override
                    public void onConnected() {
                        super.onConnected();

                        isMediaBrowserConnecting = false;

                        // Garante que temos um MediaController
                        mController = MediaControllerCompat.getMediaController(MediaActivity.this);
                        if (mController == null) {
                            mController = new MediaControllerCompat(
                                    MediaActivity.this,
                                    mMediaBrowser.getSessionToken()
                            );
                            MediaControllerCompat.setMediaController(MediaActivity.this, mController);
                        }

                        // Registra o callback para repassar estado aos listeners
                        mController.registerCallback(mCb);

                        // Inicia playback (se essa for a regra padrão)
                        mController.getTransportControls().play();

                        // Rede + licença
                        networkCallbackConnect();
                        remoteConfig();

                        // Hook para activities filhas (MainActivity, etc.)
                        onConnectedMedia();
                    }

                    @Override
                    public void onConnectionSuspended() {
                        super.onConnectionSuspended();
                        isMediaBrowserConnecting = false;

                        if (mController != null) {
                            mController.getTransportControls().stop();
                        }
                    }

                    @Override
                    public void onConnectionFailed() {
                        super.onConnectionFailed();
                        isMediaBrowserConnecting = false;

                        if (mController != null) {
                            mController.getTransportControls().stop();
                        }
                        new DefaultDialog(MediaActivity.this)
                                .type(DefaultDialog.FINISH)
                                .setName(R.string.alert_ops)
                                .setDesc(R.string.alert_app_erro)
                                .start(R.string.string_retry, R.string.string_close);
                    }
                },
                null
        );
    }

    private void remoteConfig() {
        new Config().fetch(this, () -> {
            if (!data.license) {
                startActivity(new Intent(MediaActivity.this, RemovalActivity.class));
                if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
                    if (mController != null) {
                        mController.getTransportControls().stop();
                    }
                    mMediaBrowser.disconnect();
                }
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Conecta apenas se não estiver conectado nem conectando
        connectMediaService();

        // Propaga estado atual para os listeners já registrados
        if (mController != null) {
            if (mController.getPlaybackState() != null) {
                mCb.onPlaybackStateChanged(mController.getPlaybackState());
            }
            if (mController.getMetadata() != null) {
                mCb.onMetadataChanged(mController.getMetadata());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }

        if (mMediaBrowser != null) {
            try {
                if (mMediaBrowser.isConnected()) {
                    mMediaBrowser.disconnect();
                }
            } catch (Exception ignored) {}
        }

        if (mController != null) {
            try {
                mController.unregisterCallback(mCb);
            } catch (Exception ignored) {}
        }

        isMediaBrowserConnecting = false;

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        // Aqui você controla apenas PLAY/PAUSE.
        if (view.getId() == R.id.bt_play) {
            if (mController != null
                    && mController.getPlaybackState() != null
                    && mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                mController.getTransportControls().pause();
            } else if (mController != null) {
                mController.getTransportControls().play();
            }
        }
    }

    /**
     * Hook para Activities filhas (MainActivity, etc.) executarem lógica
     * extra assim que o MediaService estiver conectado.
     */
    public void onConnectedMedia() {
        // sobrescrever em MainActivity se precisar
    }

    public void connectMediaService() {
        if (mMediaBrowser == null) return;

        // evita crash: só chama connect se estiver realmente desconectado
        if (mMediaBrowser.isConnected() || isMediaBrowserConnecting) {
            return;
        }

        isMediaBrowserConnecting = true;
        mMediaBrowser.connect();
    }

    // === CALLBACK DO MEDIA CONTROLLER: repassa para os listeners ===
    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state != null) {
                notifyPlaybackStateChanged(state);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata != null) {
                notifyMetadataChanged(metadata);
            }
        }
    };

    // ========================================================================
    // MEDIA STATE LISTENERS (Fragments)
    // ========================================================================

    public void addMediaStateListener(MediaStateListener listener) {
        if (listener != null && !mediaStateListeners.contains(listener)) {
            mediaStateListeners.add(listener);

            // Já dispara o estado atual se existir
            if (mController != null) {
                if (mController.getPlaybackState() != null) {
                    listener.onPlaybackStateChanged(mController.getPlaybackState());
                }
                if (mController.getMetadata() != null) {
                    listener.onMetadataChanged(mController.getMetadata());
                }
            }
        }
    }

    public void removeMediaStateListener(MediaStateListener listener) {
        mediaStateListeners.remove(listener);
    }

    private void notifyPlaybackStateChanged(PlaybackStateCompat state) {
        for (MediaStateListener listener : new ArrayList<>(mediaStateListeners)) {
            try {
                listener.onPlaybackStateChanged(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyMetadataChanged(MediaMetadataCompat metadata) {
        for (MediaStateListener listener : new ArrayList<>(mediaStateListeners)) {
            try {
                listener.onMetadataChanged(metadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ========================================================================
    // REDE
    // ========================================================================

    private void networkCallbackConnect() {
        if (connectivityManager == null) {
            connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (connectivityManager == null) return;

        if (networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                NetworkCapabilities caps =
                        connectivityManager.getNetworkCapabilities(network);
                boolean hasInternet = caps != null &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

                if (hasInternet) {
                    if (mController != null
                            && mController.getPlaybackState() != null
                            && mController.getPlaybackState().getState()
                            == PlaybackStateCompat.STATE_ERROR) {
                        mController.getTransportControls().play();
                    }
                } else {
                    if (mController != null) {
                        mController.getTransportControls().pause();
                    }
                    showInternetDialog();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (mController != null) {
                    mController.getTransportControls().pause();
                }
                showInternetDialog();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                if (mController != null) {
                    mController.getTransportControls().pause();
                }
                showInternetDialog();
            }
        };

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    private void showInternetDialog() {
        new DefaultDialog(MediaActivity.this)
                .type(DefaultDialog.DEFAULT)
                .setName(R.string.alert_ops)
                .setDesc(R.string.alert_app_erro)
                .start(R.string.string_retry, R.string.string_close);
    }
}