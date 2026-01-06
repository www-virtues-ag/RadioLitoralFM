package br.com.fivecom.litoralfm.models.locutores;

import android.util.Log;

public class Locutor {
    private static final String TAG = "Locutor";
    
    private String id;
    private String nome;
    private String fotoUrl;         // URL da foto vinda da API
    private String descricao;       // Descrição do locutor
    private String facebookUrl;
    private String instagramUrl;
    private String whatsappUrl;

    // Construtor padrão (necessário para algumas operações)
    public Locutor() {
    }

    // Construtor completo
    public Locutor(String id, String nome, String fotoUrl, String descricao,
                   String facebookUrl, String instagramUrl, String whatsappUrl) {
        this.id = id;
        this.nome = nome;
        this.fotoUrl = fotoUrl;
        this.descricao = descricao;
        this.facebookUrl = facebookUrl;
        this.instagramUrl = instagramUrl;
        this.whatsappUrl = whatsappUrl;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFotoUrl() {
        Log.d(TAG, "🖼️ [Locutor:" + nome + "] fotoURL - String: '" + fotoUrl + "'");
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFacebookUrl() {
        if (facebookUrl == null || facebookUrl.isEmpty()) {
            Log.d(TAG, "👤 [Locutor:" + nome + "] Facebook vazio");
            return null;
        }
        Log.d(TAG, "👤 [Locutor:" + nome + "] Facebook - String: '" + facebookUrl + "'");
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getInstagramUrl() {
        if (instagramUrl == null || instagramUrl.isEmpty()) {
            Log.d(TAG, "📸 [Locutor:" + nome + "] Instagram vazio");
            return null;
        }
        Log.d(TAG, "📸 [Locutor:" + nome + "] Instagram - String: '" + instagramUrl + "'");
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getWhatsappUrl() {
        if (whatsappUrl == null || whatsappUrl.isEmpty()) {
            Log.d(TAG, "💬 [Locutor:" + nome + "] WhatsApp vazio");
            return null;
        }
        // Formatar URL do WhatsApp
        String cleanNumber = whatsappUrl.replaceAll("[^0-9]", "");
        String formattedUrl = "https://wa.me/" + cleanNumber;
        Log.d(TAG, "💬 [Locutor:" + nome + "] WhatsApp - String: '" + whatsappUrl + 
                "' → Clean: '" + cleanNumber + "' → URL: '" + formattedUrl + "'");
        return formattedUrl;
    }

    public void setWhatsappUrl(String whatsappUrl) {
        this.whatsappUrl = whatsappUrl;
    }
}
