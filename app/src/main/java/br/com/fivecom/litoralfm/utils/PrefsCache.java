package br.com.fivecom.litoralfm.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Cache para valores de SharedPreferences para evitar leituras repetidas
 */
public class PrefsCache {
    
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_RADIO_ID = "selected_radio_id";
    
    // Cache de valores
    private static int cachedRadioId = -1;
    private static long radioIdCacheTimestamp = 0;
    private static final long CACHE_DURATION_MS = 1000; // 1 segundo
    
    /**
     * Obtém o ID da rádio selecionada com cache
     */
    public static int getRadioId(Context context) {
        long now = System.currentTimeMillis();
        if (cachedRadioId == -1 || (now - radioIdCacheTimestamp) > CACHE_DURATION_MS) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            cachedRadioId = prefs.getInt(KEY_RADIO_ID, 10223); // Default: VITÓRIA
            radioIdCacheTimestamp = now;
        }
        return cachedRadioId;
    }
    
    /**
     * Invalida o cache (chamar quando o valor for alterado)
     */
    public static void invalidateRadioIdCache() {
        cachedRadioId = -1;
        radioIdCacheTimestamp = 0;
    }
    
    /**
     * Força atualização do cache
     */
    public static void updateRadioIdCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        cachedRadioId = prefs.getInt(KEY_RADIO_ID, 10223);
        radioIdCacheTimestamp = System.currentTimeMillis();
    }
}
