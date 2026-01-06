package br.com.fivecom.litoralfm.ui.locutores;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.services.LocutorService;
import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.locutores.Locutor;

public class LocutoresActivity extends AppCompatActivity {
    private static final String TAG = "LocutoresActivity";

    public static final String EXTRA_LOCUTOR_ID = "extra_locutor_id";

    private LocutorService locutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_locutores);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.locutores), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar botão voltar
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());

        // Configurar botões dos locutores
        configurarBotoes();

        // Buscar locutores da API
        buscarLocutoresDaAPI();
    }

    /**
     * Configura os botões dos locutores
     */
    private void configurarBotoes() {
        // Map: id do botão -> id do locutor
        Map<Integer, String> map = new HashMap<>();
        map.put(R.id.btn_locutor_sol, "sol");
        map.put(R.id.btn_locutor_cleide, "cleide");
        map.put(R.id.btn_locutor_sergio, "sergio");
        map.put(R.id.btn_locutor_bruninho, "bruninho");
        map.put(R.id.btn_locutor_nat, "nat");
        map.put(R.id.btn_locutor_alex, "alex");
        map.put(R.id.btn_locutor_jonas, "jonas");
        map.put(R.id.btn_locutor_roliber, "roliber");

        for (Map.Entry<Integer, String> e : map.entrySet()) {
            int viewId = e.getKey();
            String locutorId = e.getValue();

            findViewById(viewId).setOnClickListener(v -> abrirDetalhes(locutorId));
        }
    }

    /**
     * Busca os locutores da API
     */
    private void buscarLocutoresDaAPI() {
        locutorService = new LocutorService();

        Log.d(TAG, "🔄 Iniciando busca de locutores da API...");

        locutorService.fetchLocutores(new LocutorService.LocutorCallback() {
            @Override
            public void onSuccess(List<Locutor> locutores) {
                Log.d(TAG, "✅ Locutores carregados com sucesso: " + locutores.size());
                LocutoresRepo.updateLocutores(locutores);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "❌ Erro ao carregar locutores: " + errorMessage);
                // Em caso de erro, os locutores padrão já estão carregados
                Toast.makeText(LocutoresActivity.this,
                        "Usando dados locais",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Abre a tela de detalhes do locutor
     */
    private void abrirDetalhes(String locutorId) {
        Intent intent = new Intent(this, DetalhesLocutorActivity.class);
        intent.putExtra(EXTRA_LOCUTOR_ID, locutorId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locutorService != null) {
            locutorService.shutdown();
        }
    }
}
