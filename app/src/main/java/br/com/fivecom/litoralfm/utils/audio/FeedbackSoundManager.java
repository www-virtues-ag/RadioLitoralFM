package br.com.fivecom.litoralfm.utils.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.util.Log;

public class FeedbackSoundManager {
    private static final String TAG = "FeedbackSoundManager";
    private static FeedbackSoundManager instance;

    private Context context;
    private SoundPool soundPool;
    private boolean soundsLoaded = false;

    public FeedbackSoundManager(Context context) {
        this.context = context;
        initializeSoundPool();
    }

    public static FeedbackSoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new FeedbackSoundManager(context.getApplicationContext());
        }
        return instance;
    }

    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    soundsLoaded = true;
                    Log.d(TAG, "Sons de feedback carregados com sucesso");
                } else {
                    Log.e(TAG, "Erro ao carregar sons de feedback");
                }
            }
        });

        soundsLoaded = true; // Para usar ToneGenerator como padrão
    }

    public void playListeningStartSound() {
        playTone(ToneGenerator.TONE_PROP_BEEP, 100);
    }

    public void playSuccessSound() {
        playTone(ToneGenerator.TONE_PROP_ACK, 150);
    }

    public void playErrorSound() {
        playTone(ToneGenerator.TONE_PROP_NACK, 200);
    }

    public void playSimpleBeep() {
        playTone(ToneGenerator.TONE_PROP_BEEP, 50);
    }

    private void playTone(int toneType, int duration) {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50);
            toneGenerator.startTone(toneType, duration);

            // Liberar o ToneGenerator após um pequeno delay
            new android.os.Handler().postDelayed(() -> {
                try {
                    toneGenerator.release();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao liberar ToneGenerator: " + e.getMessage());
                }
            }, duration + 50);

            Log.d(TAG, "Tom de feedback reproduzido");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao reproduzir tom de feedback: " + e.getMessage());
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundsLoaded = false;
        instance = null;
    }
}
