package br.com.fivecom.litoralfm.ui.locutores;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import br.com.fivecom.litoralfm.ui.locutores.LocutoresRepo;
import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.locutores.Locutor;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.core.Intents;
import br.com.fivecom.litoralfm.utils.core.WebViewCacheManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import androidx.annotation.Nullable;
import android.graphics.drawable.Drawable;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

public class DetalhesLocutorActivity extends AppCompatActivity {
    private static final String TAG = "DetalhesLocutorActivity";

    private ImageView btnVoltar;
    private ImageView imgLocutor;
    private TextView txtNome;
    private TextView txtBio;
    private ImageView btnFacebook;
    private ImageView btnInstagram;
    private ImageView btnWhatsapp;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_locutores);

        // Views
        btnVoltar = findViewById(R.id.btn_voltar);
        imgLocutor = findViewById(R.id.img_locutor);

        txtNome = findViewById(R.id.txt_nome_locutor);
        txtBio = findViewById(R.id.txt_descricao_locutor);

        btnFacebook = findViewById(R.id.btn_facebook);
        btnInstagram = findViewById(R.id.btn_instagram);
        btnWhatsapp = findViewById(R.id.btn_x); // Reutilizando o botão X para WhatsApp

        btnVoltar.setOnClickListener(v -> finish());

        // Pega o ID do locutor
        String id = getIntent().getStringExtra(LocutoresActivity.EXTRA_LOCUTOR_ID);
        if (id == null) {
            Log.e(TAG, "ID do locutor não fornecido");
            finish();
            return;
        }

        // Busca no repositório
        Locutor locutor = LocutoresRepo.get(id);
        if (locutor == null) {
            Log.e(TAG, "Locutor não encontrado para ID: " + id);
            finish();
            return;
        }

        // Preenche UI
        carregarImagem(locutor);
        txtNome.setText(locutor.getNome());
        // txtBio mantém o texto fixo do XML conforme solicitado

        // Redes sociais (se não tiver, esconde o botão)
        setupSocial(btnFacebook, locutor.getFacebookUrl());
        setupSocial(btnInstagram, locutor.getInstagramUrl());
        setupSocial(btnWhatsapp, locutor.getWhatsappUrl());

        // Configura WebView do banner
        setupWebView();
    }

    /**
     * Carrega a imagem do locutor usando Glide
     */
    private void carregarImagem(Locutor locutor) {
        String fotoUrl = locutor.getFotoUrl();

        if (fotoUrl == null || fotoUrl.isEmpty()) {
            Log.w(TAG, "URL da foto não disponível para " + locutor.getNome());
            return;
        }

        Log.d(TAG, "Carregando imagem de: " + fotoUrl);

        // Se a URL não começar com http/https, assumir que é um nome de arquivo local
        // e tentar carregar do drawable
        if (!fotoUrl.startsWith("http")) {
            int drawableId = getDrawableIdFromName(fotoUrl);
            if (drawableId != 0) {
                imgLocutor.setImageResource(drawableId);
            }
        } else {
            // Carregar da URL usando Glide com listener para diagnóstico
            Glide.with(this)
                    .load(fotoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.circle_litoral)
                    .error(R.drawable.circle_litoral)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                                boolean isFirstResource) {
                            Log.e(TAG, "❌ [Details] Falha ao carregar imagem: " + fotoUrl, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "✅ [Details] Imagem carregada: " + fotoUrl + " (Fonte: " + dataSource + ")");
                            return false;
                        }
                    })
                    .into(imgLocutor);
        }
    }

    /**
     * Obtém o ID do drawable a partir do nome
     */
    private int getDrawableIdFromName(String name) {
        try {
            return getResources().getIdentifier(name, "drawable", getPackageName());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter drawable: " + name, e);
            return 0;
        }
    }

    /**
     * Configura os botões de redes sociais
     */
    private void setupSocial(ImageView btn, String url) {
        if (url == null || url.trim().isEmpty()) {
            btn.setVisibility(View.GONE);
            btn.setOnClickListener(null);
            return;
        }
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(v -> openUrl(url));
    }

    /**
     * Abre uma URL no navegador
     */
    private void openUrl(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir URL: " + url, e);
        }
    }

    /**
     * Configura a WebView do banner
     */
    private void setupWebView() {
        webView = findViewById(R.id.webView);
        if (webView == null)
            return;

        webView.setBackgroundColor(0x00000000);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); // Usa cache quando disponível
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            private boolean isActivityAlive() {
                if (isFinishing())
                    return false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isActivityAlive()) {
                    Intents.website_internal(DetalhesLocutorActivity.this, url);
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (view != null && isActivityAlive()) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (view != null && isActivityAlive()) {
                    view.setVisibility(View.GONE);
                }
            }
        });

        // Carrega a URL do banner
        if (data != null && data.radios != null && !data.radios.isEmpty() && Constants.ID >= 0
                && Constants.ID < data.radios.size()) {
            String pubUrl = String.format(
                    Intents.decode(getString(R.string.pub)),
                    data.radios.get(Constants.ID).id,
                    "Android " + Build.VERSION.RELEASE,
                    Build.MANUFACTURER + " - " + Build.MODEL);

            // Verifica se a URL já está carregada na WebView
            String currentUrl = webView.getUrl();
            boolean urlChanged = !pubUrl.equals(currentUrl);

            // Verifica se precisa recarregar baseado no cache manager
            boolean shouldReload = WebViewCacheManager.shouldReload(this, pubUrl);

            // Só recarrega se a URL mudou ou se o cache expirou
            if (urlChanged || shouldReload) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(pubUrl);
                Log.d(TAG, "✅ WebView carregando URL: " + pubUrl + (urlChanged ? " (URL mudou)" : " (cache expirado)"));
            } else {
                // URL já está carregada e cache ainda válido, apenas torna visível
                webView.setVisibility(View.VISIBLE);
                Log.d(TAG, "⏭️ WebView usando cache para URL: " + pubUrl);
            }
        } else {
            Log.w(TAG, "⚠️ Dados da rádio não disponíveis para carregar o banner");
            webView.setVisibility(View.GONE);
        }
    }
}
