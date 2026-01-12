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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.locutores.Locutor;
import br.com.fivecom.litoralfm.services.LocutorService;

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

        // Botão voltar
        if (findViewById(R.id.btn_voltar) != null) {
            findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        }

        // Inicia com placeholders do XML (ver renderLocutores se for necessário
        // resetar)

        // Buscar locutores da API
        buscarLocutoresDaAPI();
    }

    /**
     * Renderiza os locutores nos 8 slots fixos
     */
    private void renderLocutores(List<Locutor> locutores) {
        if (locutores == null)
            return;

        for (int i = 1; i <= 8; i++) {
            // IDs gerados dinamicamente
            int btnId = getResources().getIdentifier("btn_locutor_" + i, "id", getPackageName());
            int imgId = getResources().getIdentifier("img_locutor_" + i, "id", getPackageName());
            int nameId = getResources().getIdentifier("name_locutor_" + i, "id", getPackageName());

            android.view.View btn = findViewById(btnId);
            android.widget.ImageView img = findViewById(imgId);
            android.widget.TextView name = findViewById(nameId);

            if (btn == null)
                continue;

            if (i <= locutores.size()) {
                Locutor loc = locutores.get(i - 1);
                btn.setVisibility(android.view.View.VISIBLE);
                if (name != null)
                    name.setText(loc.getNome());

                // Carregar imagem com Glide
                if (img != null) {
                    String fotoUrl = loc.getFotoUrl();
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        if (fotoUrl.startsWith("http")) {
                            com.bumptech.glide.Glide.with(this)
                                    .load(fotoUrl)
                                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.circle_litoral)
                                    .error(R.drawable.circle_litoral)
                                    .into(img);
                        } else {
                            int drawableId = getResources().getIdentifier(fotoUrl, "drawable", getPackageName());
                            img.setImageResource(drawableId != 0 ? drawableId : R.drawable.circle_litoral);
                        }
                    } else {
                        img.setImageResource(R.drawable.circle_litoral);
                    }
                }

                // Click listener
                btn.setOnClickListener(v -> abrirDetalhes(loc.getId()));
            } else {
                // Esconder slots extras
                btn.setVisibility(android.view.View.GONE);
            }
        }
    }

    /**
     * Busca os locutores da API
     */
    private void buscarLocutoresDaAPI() {
        locutorService = new LocutorService();

        // Recuperar o ID da rádio selecionada das SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int radioId = prefs.getInt("selected_radio_id", 10223); // Default Grande Vitória
        String radioIdStr = String.valueOf(radioId);

        Log.d(TAG, "🔄 Iniciando busca de locutores para rádio: " + radioIdStr);

        locutorService.fetchLocutores(radioIdStr, new LocutorService.LocutorCallback() {
            @Override
            public void onSuccess(List<Locutor> locutores) {
                if (locutores == null || locutores.isEmpty()) {
                    Log.w(TAG, "⚠️ API retornou lista vazia de locutores");
                    runOnUiThread(() -> mostrarAvisoVazio());
                    return;
                }

                Log.d(TAG, "✅ Locutores carregados com sucesso: " + locutores.size());
                LocutoresRepo.updateLocutores(locutores);
                runOnUiThread(() -> renderLocutores(locutores));
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "❌ Erro ao carregar locutores: " + errorMessage);
                runOnUiThread(() -> mostrarAvisoVazio());
            }
        });
    }

    /**
     * Exibe um AlertDialog de aviso quando não há locutores
     */
    private void mostrarAvisoVazio() {
        if (isFinishing())
            return;

        LocutoresMessageDialog dialog = LocutoresMessageDialog.newInstance();
        dialog.show(getSupportFragmentManager(), "LocutoresMessageDialog");
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
