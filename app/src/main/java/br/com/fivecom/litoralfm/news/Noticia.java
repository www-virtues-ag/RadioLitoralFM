// Noticia.java
package br.com.fivecom.litoralfm.news;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.fivecom.litoralfm.news.BodyElement;
import br.com.fivecom.litoralfm.news.FeedItem;

/**
 * Equivalente ao struct Noticia do Swift.
 */
public class Noticia {

    private int id;
    private String titulo;
    private String descricao;
    private String conteudo;
    private String data;
    private String imagem;
    private String link;
    private List<Integer> categoriaIds;
    private String categoriaNome;

    public Noticia(int id,
                   String titulo,
                   String descricao,
                   String conteudo,
                   String data,
                   String imagem,
                   String link,
                   List<Integer> categoriaIds,
                   String categoriaNome) {

        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.conteudo = conteudo;
        this.data = data;
        this.imagem = imagem;
        this.link = link;
        this.categoriaIds = categoriaIds != null ? categoriaIds : new ArrayList<Integer>();
        this.categoriaNome = categoriaNome;
    }

    /**
     * Construtor "a partir de FeedItem" (nova API).
     */
    public static Noticia fromFeedItem(FeedItem feedItem) {
        // usar hash do headline como ID temporário, igual ao Swift
        int generatedId = feedItem.getHeadline() != null
                ? feedItem.getHeadline().hashCode()
                : (int) System.currentTimeMillis();

        // Extrair descrição: usar description se disponível, senão usar primeiro texto do body
        String descricao = feedItem.getDescription();
        String primeiroTextoBody = null;
        
        // monta o conteúdo com base no body
        StringBuilder conteudoBuilder = new StringBuilder();
        if (feedItem.getBody() != null) {
            boolean primeiroTextoEncontrado = false;
            for (BodyElement element : feedItem.getBody()) {
                if (element.getData() == null) continue;

                // Tentar diferentes chaves possíveis no data
                Object textObj = element.getData().get("text");
                Object valueObj = element.getData().get("value");
                Object contentObj = element.getData().get("content");
                Object dataObj = element.getData().get("data");

                String texto = null;
                if (textObj instanceof String && !((String) textObj).trim().isEmpty()) {
                    texto = (String) textObj;
                } else if (valueObj instanceof String && !((String) valueObj).trim().isEmpty()) {
                    texto = (String) valueObj;
                } else if (contentObj instanceof String && !((String) contentObj).trim().isEmpty()) {
                    texto = (String) contentObj;
                } else if (dataObj instanceof String && !((String) dataObj).trim().isEmpty()) {
                    texto = (String) dataObj;
                }

                if (texto != null && !texto.trim().isEmpty()) {
                    String textoLimpo = texto.trim();
                    
                    // Se ainda não encontrou o primeiro texto e a descrição está vazia, usar como descrição
                    if (!primeiroTextoEncontrado && (descricao == null || descricao.trim().isEmpty())) {
                        primeiroTextoBody = textoLimpo;
                        // Limitar tamanho da descrição (primeiros 200 caracteres)
                        if (primeiroTextoBody.length() > 200) {
                            primeiroTextoBody = primeiroTextoBody.substring(0, 200) + "...";
                        }
                        primeiroTextoEncontrado = true;
                        Log.d("Noticia", "Primeiro texto do body usado como descrição: " + primeiroTextoBody.substring(0, Math.min(50, primeiroTextoBody.length())) + "...");
                    }
                    conteudoBuilder.append(textoLimpo).append("\n\n");
                }
            }
        }

        String conteudo = conteudoBuilder.toString().trim();
        
        // Se descrição estiver vazia, usar o primeiro texto do body
        if ((descricao == null || descricao.trim().isEmpty()) && primeiroTextoBody != null) {
            descricao = primeiroTextoBody;
            Log.d("Noticia", "Descrição extraída do primeiro texto do body");
        } else if (descricao != null && !descricao.trim().isEmpty()) {
            Log.d("Noticia", "Descrição do campo description: " + descricao.substring(0, Math.min(50, descricao.length())) + "...");
        } else {
            Log.w("Noticia", "Nenhuma descrição encontrada para a notícia");
        }

        return new Noticia(
                generatedId,
                feedItem.getHeadline(),
                descricao != null ? descricao : "",
                conteudo,
                feedItem.getPublished(),
                (feedItem.getImage() == null || feedItem.getImage().isEmpty())
                        ? null
                        : feedItem.getImage(),
                feedItem.getShareLink(),
                new ArrayList<Integer>(),
                feedItem.getSection()
        );
    }

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getConteudo() { return conteudo; }
    public String getData() { return data; }
    public String getImagem() { return imagem; }
    public String getLink() { return link; }
    public List<Integer> getCategoriaIds() { return categoriaIds; }
    public String getCategoriaNome() { return categoriaNome; }

    // **********************
    //  Formatação de datas
    // **********************

    /**
     * Equivalente ao var dataFormatada em Swift:
     * "14h09 - 19/05/2025"
     */
    public String getDataFormatada() {
        if (data == null || data.isEmpty()) {
            return "";
        }

        // Vários formatos possíveis (igual ao Swift)
        String[] formatos = new String[]{
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        };

        Date parsedDate = null;
        for (String formato : formatos) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(formato, Locale.US);
                parser.setLenient(true);
                parsedDate = parser.parse(data);
                if (parsedDate != null) break;
            } catch (ParseException e) {
                // tenta o próximo formato
            }
        }

        if (parsedDate == null) {
            Log.w("Noticia", "Não foi possível fazer parse da data: " + data);
            return data;
        }

        // Formatar saída "14h09 - 19/05/2025"
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH", new Locale("pt", "BR"));
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", new Locale("pt", "BR"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

        String hour = hourFormat.format(parsedDate);
        String minute = minuteFormat.format(parsedDate);
        String dateStr = dateFormat.format(parsedDate);

        return hour + "h" + minute + " - " + dateStr;
    }

    public String getDataSimples() {
        return getDataFormatada();
    }

    public String getCategoriaFormatada() {
        if (categoriaNome == null) {
            return "Categoria";
        }
        String lower = categoriaNome.toLowerCase(Locale.ROOT);
        if (lower.equals("uncategorized") || lower.equals("sem categoria")) {
            return "Categoria";
        }
        return categoriaNome;
    }
}
