package br.com.fivecom.litoralfm.utils.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Gerenciador de cache para WebViews de banners usando SharedPreferences.
 * Controla quando recarregar URLs baseado em tempo de expiração.
 */
public class WebViewCacheManager {
    
    private static final String TAG = "WebViewCacheManager";
    private static final String PREFS_NAME = "app_prefs";
    private static final String PREFIX_URL_CACHE = "webview_cache_url_";
    private static final String PREFIX_TIMESTAMP = "webview_cache_time_";
    
    // Tempo de cache: 5 minutos (300000 ms)
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000;
    
    /**
     * Gera uma chave segura para SharedPreferences baseada na URL.
     * Remove caracteres especiais que não são permitidos em chaves.
     */
    private static String getSafeKey(@NonNull String url) {
        // Remove caracteres especiais e substitui por underscore
        return url.replaceAll("[^a-zA-Z0-9]", "_");
    }
    
    /**
     * Obtém o SharedPreferences.
     */
    private static SharedPreferences getPrefs(@NonNull Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Verifica se uma URL deve ser recarregada.
     * 
     * @param context Contexto da aplicação
     * @param url URL a verificar
     * @return true se deve recarregar, false se pode usar cache
     */
    public static boolean shouldReload(@NonNull Context context, @NonNull String url) {
        if (context == null) {
            Log.w(TAG, "⚠️ Context é null, forçando recarregamento");
            return true;
        }
        
        String safeKey = getSafeKey(url);
        SharedPreferences prefs = getPrefs(context);
        
        // Busca o timestamp da última carga
        long lastLoadTime = prefs.getLong(PREFIX_TIMESTAMP + safeKey, 0);
        
        if (lastLoadTime == 0) {
            // Primeira vez carregando esta URL
            saveCache(context, url);
            Log.d(TAG, "🆕 Primeira carga da URL: " + url);
            return true;
        }
        
        long timeSinceLastLoad = System.currentTimeMillis() - lastLoadTime;
        
        if (timeSinceLastLoad > CACHE_DURATION_MS) {
            // Cache expirado, precisa recarregar
            saveCache(context, url);
            Log.d(TAG, "⏰ Cache expirado para URL (expirou há " + (timeSinceLastLoad / 1000) + "s): " + url);
            return true;
        }
        
        // Cache ainda válido
        long remainingTime = (CACHE_DURATION_MS - timeSinceLastLoad) / 1000;
        Log.d(TAG, "✅ Cache válido para URL (restam " + remainingTime + "s): " + url);
        return false;
    }
    
    /**
     * Salva o cache de uma URL com o timestamp atual.
     */
    private static void saveCache(@NonNull Context context, @NonNull String url) {
        String safeKey = getSafeKey(url);
        SharedPreferences prefs = getPrefs(context);
        prefs.edit()
                .putString(PREFIX_URL_CACHE + safeKey, url)
                .putLong(PREFIX_TIMESTAMP + safeKey, System.currentTimeMillis())
                .apply();
    }
    
    /**
     * Limpa o cache de uma URL específica.
     * Útil quando a URL mudou ou precisa forçar recarregamento.
     * 
     * @param context Contexto da aplicação
     * @param url URL a limpar do cache
     */
    public static void clearCache(@NonNull Context context, @NonNull String url) {
        if (context == null) return;
        
        String safeKey = getSafeKey(url);
        SharedPreferences prefs = getPrefs(context);
        prefs.edit()
                .remove(PREFIX_URL_CACHE + safeKey)
                .remove(PREFIX_TIMESTAMP + safeKey)
                .apply();
        Log.d(TAG, "🗑️ Cache limpo para URL: " + url);
    }
    
    /**
     * Limpa todo o cache de WebViews.
     */
    public static void clearAllCache(@NonNull Context context) {
        if (context == null) return;
        
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Remove todas as chaves relacionadas ao cache de WebView
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(PREFIX_URL_CACHE) || key.startsWith(PREFIX_TIMESTAMP)) {
                editor.remove(key);
            }
        }
        editor.apply();
        Log.d(TAG, "🗑️ Todo o cache de WebView foi limpo");
    }
    
    /**
     * Verifica se uma URL está em cache (independente de estar expirada ou não).
     * 
     * @param context Contexto da aplicação
     * @param url URL a verificar
     * @return true se está em cache
     */
    public static boolean isCached(@NonNull Context context, @NonNull String url) {
        if (context == null) return false;
        
        String safeKey = getSafeKey(url);
        SharedPreferences prefs = getPrefs(context);
        return prefs.contains(PREFIX_URL_CACHE + safeKey);
    }
}
