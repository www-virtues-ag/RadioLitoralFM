package br.com.fivecom.litoralfm.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.agenda.AgendaItem;

/**
 * Activity para exibir detalhes completos de um evento da agenda
 */
public class DetailAgendaActivity extends AppCompatActivity {

    private TextView txtRecorrenciaBadge;
    private TextView txtTituloAgenda;
    private TextView txtDataHora;
    private TextView txtLocal;
    private TextView txtDescricaoAgenda;
    private ImageView imgAgendaPrincipal;
    private ImageView btnFechar;
    private View containerLocal;
    private View badgeRecorrencia;

    // Dados do evento
    private AgendaItem agendaItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_agenda);

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
        badgeRecorrencia = findViewById(R.id.badge_recorrencia);
        txtRecorrenciaBadge = findViewById(R.id.txt_recorrencia_badge);
        txtTituloAgenda = findViewById(R.id.txt_titulo_agenda);
        txtDataHora = findViewById(R.id.txt_data_hora);
        txtLocal = findViewById(R.id.txt_local);
        txtDescricaoAgenda = findViewById(R.id.txt_descricao_agenda);
        imgAgendaPrincipal = findViewById(R.id.img_agenda_principal);
        btnFechar = findViewById(R.id.btn_fechar);
        containerLocal = findViewById(R.id.container_local);
    }

    private void receberDadosIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("agenda_item")) {
            agendaItem = (AgendaItem) intent.getSerializableExtra("agenda_item");
        }
    }

    private void configurarViews() {
        if (agendaItem == null) {
            finish();
            return;
        }

        // Título
        txtTituloAgenda.setText(agendaItem.titulo != null ? agendaItem.titulo : "");

        // Recorrência
        if (agendaItem.recorrencia != null && !agendaItem.recorrencia.isEmpty()) {
            txtRecorrenciaBadge.setText(formatarRecorrencia(agendaItem.recorrencia));
            badgeRecorrencia.setVisibility(View.VISIBLE);
        } else {
            badgeRecorrencia.setVisibility(View.GONE);
        }

        // Data/Hora - usar período formatado se houver data final, senão usar data/hora formatada
        String dataHoraTexto;
        if (agendaItem.finalDate != null && !agendaItem.finalDate.isEmpty() && 
            !agendaItem.finalDate.equals(agendaItem.inicio)) {
            dataHoraTexto = agendaItem.getPeriodoFormatado();
        } else {
            dataHoraTexto = agendaItem.getDataHoraFormatada();
        }
        
        if (dataHoraTexto != null && !dataHoraTexto.isEmpty()) {
            txtDataHora.setText(dataHoraTexto);
        } else {
            txtDataHora.setText("Data não informada");
        }

        // Local
        if (agendaItem.local != null && !agendaItem.local.isEmpty()) {
            txtLocal.setText(agendaItem.local);
            containerLocal.setVisibility(View.VISIBLE);
        } else {
            containerLocal.setVisibility(View.GONE);
        }

        // Descrição
        String descricao = agendaItem.getDescricaoSemHTML();
        if (descricao != null && !descricao.isEmpty()) {
            txtDescricaoAgenda.setText(descricao);
        } else {
            txtDescricaoAgenda.setText("Sem descrição disponível");
        }

        // Imagem usando Glide
        String imagemUrl = agendaItem.getImagemURL();
        if (imagemUrl != null && !imagemUrl.isEmpty()) {
            Glide.with(this)
                    .load(imagemUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .centerCrop()
                    .into(imgAgendaPrincipal);
        } else {
            imgAgendaPrincipal.setImageResource(R.drawable.ic_launcher);
        }
    }

    private void configurarListeners() {
        // Botão Fechar
        btnFechar.setOnClickListener(v -> finish());
    }

    private String formatarRecorrencia(String recorrencia) {
        if (recorrencia == null || recorrencia.isEmpty()) {
            return "Evento";
        }
        
        String lower = recorrencia.toLowerCase();
        if (lower.contains("diaria") || lower.contains("diário")) {
            return "Diário";
        } else if (lower.contains("semanal")) {
            return "Semanal";
        } else if (lower.contains("mensal")) {
            return "Mensal";
        } else if (lower.contains("anual")) {
            return "Anual";
        }
        
        // Retorna a primeira letra maiúscula
        return recorrencia.substring(0, 1).toUpperCase() + recorrencia.substring(1).toLowerCase();
    }
}




