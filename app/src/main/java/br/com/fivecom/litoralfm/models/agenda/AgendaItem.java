package br.com.fivecom.litoralfm.models.agenda;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AgendaItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @SerializedName("identificador")
    public int identificador;
    
    @SerializedName("titulo")
    public String titulo;
    
    @SerializedName("imagem")
    public String imagem;
    
    @SerializedName("inicio")
    public String inicio;
    
    @SerializedName("final")
    public String finalDate;
    
    @SerializedName("horaInicio")
    public String horaInicio;
    
    @SerializedName("horaFinal")
    public String horaFinal;
    
    @SerializedName("descricao")
    public String descricao;
    
    @SerializedName("recorrencia")
    public String recorrencia;
    
    @SerializedName("local")
    public String local;

    /**
     * Get ID for identification
     */
    public int getId() {
        return identificador;
    }

    /**
     * Get data property for compatibility
     */
    public String getData() {
        return inicio;
    }

    /**
     * Get image URL - builds full URL if relative
     */
    public String getImagemURL() {
        if (imagem == null || imagem.isEmpty()) {
            return null;
        }

        // If it's already a complete URL, return it directly
        if (imagem.startsWith("http://") || imagem.startsWith("https://")) {
            return imagem;
        }

        // If it's relative, construct the full URL
        String baseURL = "https://devapi.virtueslab.app";
        if (imagem.startsWith("/")) {
            return baseURL + imagem;
        } else {
            return baseURL + "/" + imagem;
        }
    }

    /**
     * Format start date for display (dd/MM/yyyy)
     */
    public String getDataFormatada() {
        if (inicio == null || inicio.isEmpty()) {
            return "";
        }
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

        try {
            Date date = inputFormat.parse(inicio);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return inicio;
        }
    }

    /**
     * Formatted date with time (if available)
     */
    public String getDataHoraFormatada() {
        String resultado = getDataFormatada();

        if (horaInicio != null && !horaInicio.isEmpty()) {
            resultado += " às " + horaInicio;
        }

        return resultado;
    }

    /**
     * Full period (start to end)
     */
    public String getPeriodoFormatado() {
        String resultado = getDataFormatada();

        if (horaInicio != null && !horaInicio.isEmpty()) {
            resultado += " às " + horaInicio;
        }

        if (finalDate != null && !finalDate.isEmpty()) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

            try {
                Date finalDateObj = inputFormat.parse(finalDate);
                resultado += " até " + outputFormat.format(finalDateObj);

                if (horaFinal != null && !horaFinal.isEmpty()) {
                    resultado += " às " + horaFinal;
                }
            } catch (ParseException e) {
                // Ignore
            }
        }

        return resultado;
    }

    /**
     * Description without HTML tags
     */
    public String getDescricaoSemHTML() {
        if (descricao == null) {
            return "";
        }

        return descricao
                .replaceAll("<p>", "")
                .replaceAll("</p>", "\n")
                .replaceAll("<br>", "\n")
                .replaceAll("<br/>", "\n")
                .replaceAll("<[^>]+>", "")
                .trim();
    }

    /**
     * Get Date object from inicio string for sorting
     */
    public Date getInicioDate() {
        if (inicio == null || inicio.isEmpty()) {
            return null;
        }
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            return format.parse(inicio);
        } catch (ParseException e) {
            return null;
        }
    }
}
