package br.com.fivecom.litoralfm.ui.news;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

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
        setContentView(R.layout.activity_noticia_detail);

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
        }
    }

    private void configurarViews() {
        // Categoria
        if (categoria != null && !categoria.isEmpty()) {
            String categoriaFormatada = formatarCategoria(categoria);
            txtCategoriaBadge.setText(categoriaFormatada);
        } else {
            txtCategoriaBadge.setText("Notícia");
        }

        // Título
        txtTituloNoticia.setText(titulo != null ? titulo : "");

        // Descrição
        if (descricao != null && !descricao.isEmpty()) {
            txtDescricaoNoticia.setText(descricao);
            txtDescricaoNoticia.setVisibility(View.VISIBLE);
        } else {
            txtDescricaoNoticia.setVisibility(View.GONE);
        }

        // Data formatada
        if (data != null && !data.isEmpty()) {
            String dataFormatada = formatarData(data);
            txtDataHora.setText(dataFormatada);
        }

        // Conteúdo (AGORA renderiza corretamente conteúdo vindo como HTML ou texto cru)
        CharSequence conteudoFinal = formatarConteudoNoticia(conteudo);
        if (conteudoFinal != null && conteudoFinal.length() > 0) {
            txtConteudoNoticia.setText(conteudoFinal);
        } else {
            txtConteudoNoticia.setText(getString(R.string.news_content_not_available));
        }

        // Imagem usando Glide
        if (imagem != null && !imagem.isEmpty()) {
            Glide.with(this)
                    .load(imagem)
                    .placeholder(R.drawable.bg_noticia)
                    .error(R.drawable.bg_noticia)
                    .centerCrop()
                    .into(imgNoticiaPrincipal);
        } else {
            imgNoticiaPrincipal.setImageResource(R.drawable.bg_noticia);
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
    }

    private String formatarCategoria(String cat) {
        if (cat == null || cat.isEmpty()) {
            return "Notícia";
        }
        String lower = cat.toLowerCase();
        if (lower.equals("uncategorized") || lower.equals("sem categoria")) {
            return "Notícia";
        }
        return cat;
    }

    private String formatarData(String dataString) {
        // Formato esperado: "14h09 - 19/05/2025"
        // Tentar formatar se necessário (ISO)
        if (dataString != null && dataString.contains("T")) {
            try {
                java.text.SimpleDateFormat inputFormat =
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);

                java.util.Date date = inputFormat.parse(dataString);

                if (date != null) {
                    java.text.SimpleDateFormat hourFormat =
                            new java.text.SimpleDateFormat("HH", new java.util.Locale("pt", "BR"));
                    java.text.SimpleDateFormat minuteFormat =
                            new java.text.SimpleDateFormat("mm", new java.util.Locale("pt", "BR"));
                    java.text.SimpleDateFormat dateFormat =
                            new java.text.SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("pt", "BR"));

                    String hour = hourFormat.format(date);
                    String minute = minuteFormat.format(date);
                    String dateStr = dateFormat.format(date);

                    return hour + "h" + minute + "   " + dateStr;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dataString != null ? dataString : "";
    }

    /**
     * Formata o conteúdo da notícia para exibir corretamente no TextView.
     * Aceita conteúdo vindo como:
     * - HTML (<p>, <br>, entidades &nbsp; etc.)
     * - Texto cru com \n
     * - Conteúdo com lixo do WordPress (<!-- wp:... -->)
     */
    private CharSequence formatarConteudoNoticia(String raw) {
        if (raw == null) return "";

        // Normaliza e remove "lixos" comuns
        String content = limparTexto(raw);

        if (content.isEmpty()) return "";

        // Se parece HTML, converte para texto exibível
        boolean looksHtml = content.contains("<") && content.contains(">");

        if (looksHtml) {
            // Antes de converter, trocamos alguns padrões para preservar parágrafo/linhas
            content = content
                    .replaceAll("(?i)<br\\s*/?>", "\n")
                    .replaceAll("(?i)</p\\s*>", "\n\n")
                    .replaceAll("(?i)<p\\s*>", "")
                    .replaceAll("(?i)<p[^>]*>", "") // remove <p class="...">
                    .replaceAll("(?i)</div\\s*>", "\n")
                    .replaceAll("(?i)<div[^>]*>", "")
                    .replaceAll("(?s)<!--.*?-->", ""); // remove comentários HTML (inclui wp)

            // Converte entidades e tags restantes
            Spanned sp;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sp = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
            } else {
                sp = Html.fromHtml(content);
            }

            // Depois do fromHtml, limpamos novamente espaços e linhas duplicadas
            String finalText = limparTexto(sp.toString());
            return finalText;
        }

        // Texto cru: só normaliza quebras/espacos e retorna
        return content;
    }

    /**
     * Limpa texto:
     * - remove "null"/"undefined"
     * - remove tags WP do tipo [shortcode]
     * - normaliza quebras e espaços
     */
    private String limparTexto(String s) {
        if (s == null) return "";

        String out = s;

        // Remoções comuns (API/WordPress)
        out = out.replace("\u00A0", " "); // nbsp
        out = out.replaceAll("(?i)\\bnull\\b", "");
        out = out.replaceAll("(?i)\\bundefined\\b", "");
        out = out.replaceAll("(?s)<!--\\s*wp:.*?-->", "");  // wp comments
        out = out.replaceAll("(?s)<!--\\s*/wp:.*?-->", "");
        out = out.replaceAll("(?s)\\[/?[^\\]]+\\]", "");    // [shortcodes]

        // Normaliza quebras
        out = out.replace("\r\n", "\n").replace("\r", "\n");

        // Remove espaços por linha e reduz linhas em branco
        out = out.replaceAll("[ \\t]+", " ");
        out = out.replaceAll("\\n{3,}", "\n\n"); // no máximo 2 quebras seguidas

        return out.trim();
    }

    private void compartilharWhatsApp() {
        String textoCompartilhar = montarTextoCompartilhamento();

        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textoCompartilhar);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "WhatsApp não instalado", Toast.LENGTH_SHORT).show();
        }
    }

    private void compartilharFacebook() {
        String textoCompartilhar = montarTextoCompartilhamento();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textoCompartilhar);

        try {
            shareIntent.setPackage("com.facebook.katana");
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            shareIntent.setPackage(null);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
        }
    }

    private void compartilharInstagram() {
        Toast.makeText(this, "Link copiado! Cole no Instagram Stories", Toast.LENGTH_LONG).show();

        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        android.content.ClipData clip = android.content.ClipData.newPlainText(
                "Notícia", montarTextoCompartilhamento());

        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    private void compartilharX() {
        String textoCompartilhar = montarTextoCompartilhamento();

        try {
            Intent tweetIntent = new Intent(Intent.ACTION_SEND);
            tweetIntent.putExtra(Intent.EXTRA_TEXT, textoCompartilhar);
            tweetIntent.setType("text/plain");
            tweetIntent.setPackage("com.twitter.android");
            startActivity(tweetIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, textoCompartilhar);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
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
