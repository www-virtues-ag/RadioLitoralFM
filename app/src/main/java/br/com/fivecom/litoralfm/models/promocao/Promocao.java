package br.com.fivecom.litoralfm.models.promocao;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo de Promoção
 * Equivalente ao struct Promocao do Swift
 */
public class Promocao implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String titulo;
    private String descricao;
    private String imagem;
    private String link;
    private String data;
    
    /**
     * Construtor padrão
     */
    public Promocao() {
    }
    
    /**
     * Construtor a partir do WordPressPost
     */
    public Promocao(WordPressPost post, String imagemURL) {
        this.id = post.id;
        this.titulo = stripHTML(post.title != null ? post.title.rendered : "");
        this.descricao = stripHTML(post.excerpt != null ? post.excerpt.rendered : "");
        this.imagem = imagemURL;
        this.link = post.link != null ? post.link : "";
        this.data = post.date != null ? post.date : "";
    }
    
    /**
     * Construtor manual para testes
     */
    public Promocao(int id, String titulo, String descricao, String imagem, String link, String data) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.imagem = imagem;
        this.link = link;
        this.data = data;
    }
    
    /**
     * Remove tags HTML de uma string
     */
    private String stripHTML(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        return html
                .replaceAll("<p>", "")
                .replaceAll("</p>", "\n")
                .replaceAll("<br>", "\n")
                .replaceAll("<br/>", "\n")
                .replaceAll("<br />", "\n")
                .replaceAll("<[^>]+>", "")
                .trim();
    }
    
    /**
     * Retorna a URL da imagem
     */
    public String getImagemURL() {
        if (imagem == null || imagem.isEmpty()) {
            return null;
        }
        return imagem;
    }
    
    /**
     * Retorna a URL do link
     */
    public String getLinkURL() {
        if (link == null || link.isEmpty()) {
            return null;
        }
        return link;
    }
    
    /**
     * Formata a data para exibição no formato: "14h09 - 19/05/2025"
     */
    public String getDataFormatada() {
        if (data == null || data.isEmpty()) {
            return "";
        }
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date date = null;
        
        try {
            // Tentar formato ISO 8601 sem timezone
            date = inputFormat.parse(data);
        } catch (ParseException e) {
            // Tentar formato ISO 8601 com timezone
            try {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
                date = inputFormat.parse(data);
            } catch (ParseException e2) {
                // Se falhar, retornar a data original
                return data;
            }
        }
        
        if (date == null) {
            return data;
        }
        
        // Formatar hora
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH", new Locale("pt", "BR"));
        String hour = hourFormat.format(date);
        
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", new Locale("pt", "BR"));
        String minute = minuteFormat.format(date);
        
        // Formatar data
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        String dateStr = dateFormat.format(date);
        
        return hour + "h" + minute + " - " + dateStr;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getImagem() {
        return imagem;
    }
    
    public void setImagem(String imagem) {
        this.imagem = imagem;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
}

