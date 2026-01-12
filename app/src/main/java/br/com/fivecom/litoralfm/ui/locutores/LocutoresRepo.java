package br.com.fivecom.litoralfm.ui.locutores;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.models.locutores.Locutor;

/**
 * Repositório para gerenciar locutores.
 * Mantém locutores padrão como fallback e permite atualização via API.
 */
public final class LocutoresRepo {
    private static final String TAG = "LocutoresRepo";

    private static Map<String, Locutor> locutoresMap = new HashMap<>();
    private static final List<Locutor> DEFAULT_LOCUTORES = new ArrayList<>();

    // Inicializar locutores padrão (fallback)
    static {
        // DEFAULT_LOCUTORES removido para usar apenas dados da API
        loadDefaultLocutores();
    }

    private LocutoresRepo() {
    }

    /**
     * Retorna um locutor pelo ID
     */
    public static Locutor get(String id) {
        return locutoresMap.get(id);
    }

    /**
     * Retorna todos os locutores
     */
    public static List<Locutor> getAll() {
        return new ArrayList<>(locutoresMap.values());
    }

    public static void updateLocutores(List<Locutor> locutores) {
        Log.d(TAG, "📝 Atualizando locutores com dados da API");
        locutoresMap.clear();

        for (Locutor locutor : locutores) {
            locutoresMap.put(locutor.getId(), locutor);
        }

        Log.d(TAG, "✅ Total de locutores atualizado: " + locutoresMap.size());
    }

    /**
     * Carrega locutores padrão (fallback)
     */
    public static void loadDefaultLocutores() {
        Log.d(TAG, "⚠️ Carregando locutores padrão (fallback)");
        locutoresMap.clear();

        for (Locutor locutor : DEFAULT_LOCUTORES) {
            locutoresMap.put(locutor.getId(), locutor);
        }
    }

}
