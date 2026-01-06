package br.com.fivecom.litoralfm.ui.main;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Interface para notificar Fragments sobre mudanças no estado da mídia
 */
public interface MediaStateListener {
    /**
     * Chamado quando o estado de reprodução muda
     * @param state O novo estado de reprodução
     */
    void onPlaybackStateChanged(PlaybackStateCompat state);

    /**
     * Chamado quando os metadados da mídia mudam
     * @param metadata Os novos metadados
     */
    void onMetadataChanged(MediaMetadataCompat metadata);
}