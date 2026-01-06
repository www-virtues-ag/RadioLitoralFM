package br.com.fivecom.litoralfm.ui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;

/**
 * Activity para exibir detalhes completos de um programa
 */
public class DetailProgramaActivity extends AppCompatActivity {

    private ImageView imgPrograma;
    private TextView txtTituloPrograma;
    private TextView txtHorario;
    private TextView txtApresentador;
    private TextView txtRadio;
    private TextView txtDescricaoPrograma;
    private ImageView btnFechar;
    private View containerApresentador;
    private View containerRadio;

    // Dados do programa
    private ProgramaAPI programa;
    private String nomeRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_programa);

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
        imgPrograma = findViewById(R.id.img_programa_principal);
        txtTituloPrograma = findViewById(R.id.txt_titulo_programa);
        txtHorario = findViewById(R.id.txt_horario);
        txtApresentador = findViewById(R.id.txt_apresentador);
        txtRadio = findViewById(R.id.txt_radio);
        txtDescricaoPrograma = findViewById(R.id.txt_descricao_programa);
        btnFechar = findViewById(R.id.btn_fechar);
        containerApresentador = findViewById(R.id.container_apresentador);
        containerRadio = findViewById(R.id.container_radio);
    }

    private void receberDadosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("programa_item")) {
                programa = (ProgramaAPI) intent.getSerializableExtra("programa_item");
            }
            if (intent.hasExtra("nome_radio")) {
                nomeRadio = intent.getStringExtra("nome_radio");
            }
        }
    }

    private void configurarViews() {
        if (programa == null) {
            finish();
            return;
        }

        // Título
        if (programa.getTitle() != null && !programa.getTitle().isEmpty()) {
            txtTituloPrograma.setText(programa.getTitle());
        } else {
            txtTituloPrograma.setText("Programa");
        }

        // Horário (início - fim)
        String horario = formatarHorario(programa.getHrInicio(), programa.getHrFinal());
        if (horario != null && !horario.isEmpty()) {
            txtHorario.setText(horario);
        } else {
            txtHorario.setText("Horário não informado");
        }

        // Apresentador
        if (programa.getNmLocutor() != null && !programa.getNmLocutor().isEmpty()) {
            txtApresentador.setText(programa.getNmLocutor());
            if (containerApresentador != null) {
                containerApresentador.setVisibility(View.VISIBLE);
            }
        } else {
            if (containerApresentador != null) {
                containerApresentador.setVisibility(View.GONE);
            }
        }

        // Nome da rádio
        if (nomeRadio != null && !nomeRadio.isEmpty()) {
            txtRadio.setText(nomeRadio);
            if (containerRadio != null) {
                containerRadio.setVisibility(View.VISIBLE);
            }
        } else {
            if (containerRadio != null) {
                containerRadio.setVisibility(View.GONE);
            }
        }

        // Descrição
        String descricao = programa.getDescription();
        if (descricao != null && !descricao.isEmpty()) {
            // Remove HTML se houver
            descricao = removerHTML(descricao);
            txtDescricaoPrograma.setText(descricao);
        } else {
            txtDescricaoPrograma.setText("Sem descrição disponível");
        }

        // Imagem usando Glide
        String imagemUrl = programa.getImage();
        if (imagemUrl != null && !imagemUrl.isEmpty()) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(this)
                    .load(imagemUrl)
                    .apply(options)
                    .into(imgPrograma);
        } else {
            imgPrograma.setImageResource(R.drawable.ic_launcher);
        }
    }

    private void configurarListeners() {
        // Botão Fechar
        btnFechar.setOnClickListener(v -> finish());
    }

    private String formatarHorario(String inicio, String fim) {
        boolean hasInicio = inicio != null && !inicio.trim().isEmpty();
        boolean hasFinal = fim != null && !fim.trim().isEmpty();

        if (!hasInicio && !hasFinal) {
            return "";
        }
        if (hasInicio && hasFinal) {
            return inicio + " - " + fim;
        }
        if (hasInicio) {
            return "A partir de " + inicio;
        }
        return "Até " + fim;
    }

    private String removerHTML(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }
}

