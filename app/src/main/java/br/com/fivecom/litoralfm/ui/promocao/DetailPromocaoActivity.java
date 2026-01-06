package br.com.fivecom.litoralfm.ui.promocao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.promocao.Promocao;

/**
 * Activity para exibir detalhes completos de uma promoção
 * Equivalente ao SafariView do Swift
 */
public class DetailPromocaoActivity extends AppCompatActivity {

    private TextView txtTituloPromocao;
    private TextView txtDataPromocao;
    private TextView txtDescricaoPromocao;
    private ImageView imgPromocaoPrincipal;
    private ImageView btnFechar;
    private AppCompatButton btnAbrirLink;

    // Dados da promoção
    private Promocao promocao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_promocao);

        // Inicializar views
        initViews();

        // Receber dados da Intent
        receberDadosIntent();

        // Configurar views com dados
        configurarViews();

        // Configurar listeners
        configurarListeners();
    }

    private void initViews() {
        txtTituloPromocao = findViewById(R.id.txt_titulo_promocao);
        txtDataPromocao = findViewById(R.id.txt_data_promocao);
        txtDescricaoPromocao = findViewById(R.id.txt_descricao_promocao);
        imgPromocaoPrincipal = findViewById(R.id.img_promocao_principal);
        btnFechar = findViewById(R.id.btn_fechar);
        btnAbrirLink = findViewById(R.id.btn_abrir_link);
    }

    private void receberDadosIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("promocao_item")) {
            promocao = (Promocao) intent.getSerializableExtra("promocao_item");
        }
    }

    private void configurarViews() {
        if (promocao == null) {
            Toast.makeText(this, "Erro ao carregar promoção", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Título
        if (promocao.getTitulo() != null && !promocao.getTitulo().isEmpty()) {
            txtTituloPromocao.setText(promocao.getTitulo().toUpperCase());
        } else {
            txtTituloPromocao.setText("PROMOÇÃO");
        }

        // Data formatada
        String dataFormatada = promocao.getDataFormatada();
        if (dataFormatada != null && !dataFormatada.isEmpty()) {
            txtDataPromocao.setText(dataFormatada);
        } else {
            txtDataPromocao.setText("Data não disponível");
        }

        // Descrição
        String descricao = promocao.getDescricao();
        if (descricao != null && !descricao.isEmpty()) {
            txtDescricaoPromocao.setText(descricao);
        } else {
            txtDescricaoPromocao.setText("Sem descrição disponível");
        }

        // Imagem usando Glide
        String imagemUrl = promocao.getImagemURL();
        if (imagemUrl != null && !imagemUrl.isEmpty()) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.forma_prog_main)
                    .error(R.drawable.forma_prog_main)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(this)
                    .load(imagemUrl)
                    .apply(options)
                    .into(imgPromocaoPrincipal);
        } else {
            imgPromocaoPrincipal.setImageResource(R.drawable.forma_prog_main);
        }

        // Botão de abrir link - mostrar apenas se houver link
        String linkURL = promocao.getLinkURL();
        if (linkURL != null && !linkURL.isEmpty()) {
            btnAbrirLink.setVisibility(View.VISIBLE);
        } else {
            btnAbrirLink.setVisibility(View.GONE);
        }
    }

    private void configurarListeners() {
        // Botão Fechar
        btnFechar.setOnClickListener(v -> finish());

        // Botão Abrir Link
        btnAbrirLink.setOnClickListener(v -> {
            String linkURL = promocao.getLinkURL();
            if (linkURL != null && !linkURL.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkURL));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Erro ao abrir link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Link não disponível", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

