// Noticia.java
package br.com.fivecom.litoralfm.models.news;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // monta o conteúdo com base no body
        StringBuilder conteudoBuilder = new StringBuilder();
        if (feedItem.getBody() != null) {
            for (BodyElement element : feedItem.getBody()) {
                if (element.getData() == null) continue;

                Object textObj = element.getData().get("text");
                Object valueObj = element.getData().get("value");

                if (textObj instanceof String) {
                    conteudoBuilder.append((String) textObj).append("\n\n");
                } else if (valueObj instanceof String) {
                    conteudoBuilder.append((String) valueObj).append("\n\n");
                }
            }
        }

        String conteudo = conteudoBuilder.toString().trim();

        return new Noticia(
                generatedId,
                feedItem.getHeadline(),
                feedItem.getDescription(),
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
        
        // Se categoriaNome é um slug, converte para nome
        // Mapeamento de slugs para nomes (de acordo com a API)
        java.util.Map<String, String> slugToName = new java.util.HashMap<>();
        slugToName.put("opiniao", "Opinião");
        slugToName.put("cotidiano", "Cotidiano");
        slugToName.put("economia", "Economia");
        slugToName.put("politica", "Política");
        slugToName.put("mundo", "Mundo");
        slugToName.put("hz-cultura", "HZ Cultura");
        slugToName.put("hz-agenda-cultural", "HZ Agenda Cultural");
        slugToName.put("hz-gastronomia", "HZ Gastronomia");
        slugToName.put("hz-turismo", "HZ Turismo");
        slugToName.put("hz-turismos", "HZ Turismo"); // Fallback (variação do slug)
        slugToName.put("hz-tv-e-famosos", "HZ TV + Famosos");
        slugToName.put("esportes", "Esportes");
        
        String lower = categoriaNome.toLowerCase(Locale.ROOT);
        
        // Se é um slug conhecido, retorna o nome formatado
        if (slugToName.containsKey(lower)) {
            return slugToName.get(lower);
        }
        
        if (lower.equals("uncategorized") || lower.equals("sem categoria")) {
            return "Categoria";
        }
        
        // Se não encontrou, retorna o valor original (pode ser um nome já formatado)
        return categoriaNome;
    }
}
