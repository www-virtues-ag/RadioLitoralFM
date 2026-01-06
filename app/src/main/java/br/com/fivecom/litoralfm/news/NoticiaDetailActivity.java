package br.com.fivecom.litoralfm.news;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

import br.com.fivecom.litoralfm.news.NoticiaService;
import br.com.fivecom.litoralfm.R;

/**
 * Activity para exibir detalhes completos de uma notícia
 */
public class NoticiaDetailActivity extends AppCompatActivity {

    private TextView txtCategoriaBadge;
    private TextView txtTituloNoticia;
    private TextView txtDescricaoNoticia;
    private TextView txtDataHora;
    private TextView txtConteudoNoticia;
    private ImageView imgNoticiaPrincipal;
    private ImageView btnFechar;
    private ImageView btnShareWhatsapp;
    private ImageView btnShareFacebook;
    private ImageView btnShareInstagram;
    private ImageView btnShareX;
    private ImageView btnShareLink;

    // Dados da notícia
    private int noticiaId;
    private String titulo;
    private String descricao;
    private String conteudo;
    private String data;
    private String imagem;
    private String categoria;
    private String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal);

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
        txtCategoriaBadge = findViewById(R.id.txt_categoria_badge);
        txtTituloNoticia = findViewById(R.id.txt_titulo_noticia);
        txtDescricaoNoticia = findViewById(R.id.txt_descricao_noticia);
        txtDataHora = findViewById(R.id.txt_data_hora);
        txtConteudoNoticia = findViewById(R.id.txt_conteudo_noticia);
        imgNoticiaPrincipal = findViewById(R.id.img_noticia_principal);
        btnFechar = findViewById(R.id.btn_fechar);
        btnShareWhatsapp = findViewById(R.id.btn_share_whatsapp);
        btnShareFacebook = findViewById(R.id.btn_share_facebook);
        btnShareInstagram = findViewById(R.id.btn_share_instagram);
        btnShareX = findViewById(R.id.btn_share_x);
        btnShareLink = findViewById(R.id.btn_share_link);
    }

    private void receberDadosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            noticiaId = intent.getIntExtra("noticia_id", 0);
            titulo = intent.getStringExtra("noticia_titulo");
            descricao = intent.getStringExtra("noticia_descricao");
            conteudo = intent.getStringExtra("noticia_conteudo");
            data = intent.getStringExtra("noticia_data");
            imagem = intent.getStringExtra("noticia_imagem");
            categoria = intent.getStringExtra("noticia_categoria");
            link = intent.getStringExtra("noticia_link");
            
            Log.d("NoticiaDetail", "Dados recebidos - Categoria: " + categoria + ", Descrição: " + 
                (descricao != null ? descricao.substring(0, Math.min(50, descricao.length())) + "..." : "null"));
        }
    }

    private void configurarViews() {
        // Categoria - garantir que está visível
        View categoryContainer = findViewById(R.id.category);
        if (categoryContainer != null) {
            categoryContainer.setVisibility(View.VISIBLE);
        }
        
        if (txtCategoriaBadge != null) {
            txtCategoriaBadge.setVisibility(View.VISIBLE);
            if (categoria != null && !categoria.isEmpty()) {
                String categoriaFormatada = formatarCategoria(categoria);
                txtCategoriaBadge.setText(categoriaFormatada);
                Log.d("NoticiaDetail", "Categoria recebida: " + categoria + " -> Formatada: " + categoriaFormatada);
            } else {
                txtCategoriaBadge.setText("Notícia");
                Log.d("NoticiaDetail", "Categoria vazia, usando padrão: Notícia");
            }
        } else {
            Log.e("NoticiaDetail", "txtCategoriaBadge é null!");
        }

        // Título
        txtTituloNoticia.setText(titulo != null ? titulo : "");

        // Descrição - sempre exibir se houver conteúdo
        if (descricao != null && !descricao.trim().isEmpty()) {
            txtDescricaoNoticia.setText(descricao.trim());
            txtDescricaoNoticia.setVisibility(View.VISIBLE);
            Log.d("NoticiaDetail", "Descrição exibida: " + descricao.substring(0, Math.min(50, descricao.length())) + "...");
        } else {
            // Se não houver descrição, tentar usar o início do conteúdo
            if (conteudo != null && !conteudo.trim().isEmpty()) {
                String descricaoDoConteudo = conteudo.trim();
                // Pegar primeiros 200 caracteres do conteúdo como descrição
                if (descricaoDoConteudo.length() > 200) {
                    descricaoDoConteudo = descricaoDoConteudo.substring(0, 200) + "...";
                }
                txtDescricaoNoticia.setText(descricaoDoConteudo);
                txtDescricaoNoticia.setVisibility(View.VISIBLE);
                Log.d("NoticiaDetail", "Usando início do conteúdo como descrição");
            } else {
                txtDescricaoNoticia.setVisibility(View.GONE);
                Log.d("NoticiaDetail", "Descrição e conteúdo vazios, ocultando TextView");
            }
        }

        // Data formatada
        if (data != null && !data.isEmpty()) {
            String dataFormatada = formatarData(data);
            txtDataHora.setText(dataFormatada);
        }

        // Conteúdo
        if (conteudo != null && !conteudo.isEmpty()) {
            txtConteudoNoticia.setText(conteudo);
        } else {
            txtConteudoNoticia.setText(getString(R.string.news_content_not_available));
        }

        // Imagem
        if (imagem != null && !imagem.isEmpty()) {
            Glide.with(this)
                    .load(imagem)
                    .placeholder(R.drawable.live)
                    .error(R.drawable.live)
                    .centerCrop()
                    .into(imgNoticiaPrincipal);
        } else {
            imgNoticiaPrincipal.setImageResource(R.drawable.live);
        }
    }

    private void configurarListeners() {
        // Botão Fechar
        btnFechar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Compartilhar WhatsApp
        btnShareWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartilharWhatsApp();
            }
        });

        // Compartilhar Facebook
        btnShareFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartilharFacebook();
            }
        });

        // Compartilhar Instagram
        btnShareInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartilharInstagram();
            }
        });

        // Compartilhar X (Twitter)
        btnShareX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartilharX();
            }
        });

        // Copiar link
        btnShareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copiarLink();
            }
        });
    }

    private String formatarCategoria(String cat) {
        if (cat == null || cat.isEmpty()) {
            return "Notícia";
        }
        
        String lower = cat.toLowerCase();
        if (lower.equals("uncategorized") || lower.equals("sem categoria")) {
            return "Notícia";
        }
        
        // Tentar buscar o nome formatado do NoticiaService
        NoticiaService noticiaService = NoticiaService.getInstance();
        String nomeFormatado = noticiaService.getNomeCategoriaPorSlug(cat);
        if (nomeFormatado != null && !nomeFormatado.isEmpty()) {
            return nomeFormatado;
        }
        
        // Se não encontrar, capitalizar a primeira letra
        if (cat.length() > 0) {
            return cat.substring(0, 1).toUpperCase() + (cat.length() > 1 ? cat.substring(1) : "");
        }
        
        return cat;
    }

    private String formatarData(String dataString) {
        // Usar o mesmo formato que já está na classe Noticia
        // Por simplicidade, se já vier formatado, usar direto
        // Formato esperado: "14h09 - 19/05/2025"

        // Tentar formatar se necessário
        if (dataString.contains("T")) {
            // É um formato ISO, tentar converter
            try {
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                        java.util.Locale.US);
                java.util.Date date = inputFormat.parse(dataString);

                if (date != null) {
                    java.text.SimpleDateFormat hourFormat = new java.text.SimpleDateFormat("HH",
                            new java.util.Locale("pt", "BR"));
                    java.text.SimpleDateFormat minuteFormat = new java.text.SimpleDateFormat("mm",
                            new java.util.Locale("pt", "BR"));
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy",
                            new java.util.Locale("pt", "BR"));

                    String hour = hourFormat.format(date);
                    String minute = minuteFormat.format(date);
                    String dateStr = dateFormat.format(date);

                    return hour + "h" + minute + "   " + dateStr;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dataString;
    }

    private void compartilharWhatsApp() {
        if (link == null || link.isEmpty()) {
            Toast.makeText(this, "Link da notícia não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Abrir WhatsApp com a URL da notícia
            String urlWhatsApp = "https://wa.me/?text=" + Uri.encode(link);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(urlWhatsApp));
            startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(this, "Erro ao abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void compartilharFacebook() {
        if (link == null || link.isEmpty()) {
            Toast.makeText(this, "Link da notícia não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Tentar abrir app do Facebook
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("fb://facewebmodal/f?href=" + Uri.encode(link)));
            startActivity(intent);
        } catch (Exception ex) {
            // Se o app não estiver instalado, abrir no navegador
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.facebook.com/sharer/sharer.php?u=" + Uri.encode(link)));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao abrir Facebook", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void compartilharInstagram() {
        if (link == null || link.isEmpty()) {
            Toast.makeText(this, "Link da notícia não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Tentar abrir app do Instagram
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link);
            intent.setPackage("com.instagram.android");
            startActivity(intent);
        } catch (Exception ex) {
            // Se o app não estiver instalado, copiar link e mostrar mensagem
            copiarLink();
            Toast.makeText(this, "Link copiado! Cole no Instagram", Toast.LENGTH_LONG).show();
        }
    }

    private void compartilharX() {
        if (link == null || link.isEmpty()) {
            Toast.makeText(this, "Link da notícia não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Tentar abrir app do X (Twitter)
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link);
            intent.setPackage("com.twitter.android");
            startActivity(intent);
        } catch (Exception ex) {
            // Se o app não estiver instalado, abrir no navegador
            try {
                String urlX = "https://twitter.com/intent/tweet?url=" + Uri.encode(link);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(urlX));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao abrir X (Twitter)", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void copiarLink() {
        if (link == null || link.isEmpty()) {
            Toast.makeText(this, "Link da notícia não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        // Copiar link para clipboard
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Link da Notícia", link);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Link copiado!", Toast.LENGTH_SHORT).show();
        }
    }

    private String montarTextoCompartilhamento() {
        StringBuilder texto = new StringBuilder();

        if (titulo != null && !titulo.isEmpty()) {
            texto.append(titulo).append("\n\n");
        }

        if (descricao != null && !descricao.isEmpty()) {
            texto.append(descricao).append("\n\n");
        }

        if (link != null && !link.isEmpty()) {
            texto.append("Leia mais: ").append(link);
        }

        return texto.toString();
    }
}
