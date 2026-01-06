package br.com.fivecom.litoralfm.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProgramaAtualResponse {

    @SerializedName("programa")
    private List<ProgramaAtual> programa;

    public List<ProgramaAtual> getPrograma() {
        return programa;
    }

    public void setPrograma(List<ProgramaAtual> programa) {
        this.programa = programa;
    }

    public ProgramaAtual getProgramaAtual() {
        if (programa != null && !programa.isEmpty()) {
            return programa.get(0);
        }
        return null;
    }

    public static class ProgramaAtual {

        @SerializedName("imagem")
        private String imagem;

        @SerializedName("titulo")
        private String titulo;

        @SerializedName("locutor")
        private String locutor;

        @SerializedName("horario")
        private String horario;

        // Getters
        public String getImagem() {
            return imagem;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getLocutor() {
            return locutor;
        }

        public String getHorario() {
            return horario;
        }

        // Setters
        public void setImagem(String imagem) {
            this.imagem = imagem;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public void setLocutor(String locutor) {
            this.locutor = locutor;
        }

        public void setHorario(String horario) {
            this.horario = horario;
        }

        public boolean isValido() {
            return titulo != null && !titulo.isEmpty()
                    && !titulo.equals("null");
        }

        public String getTituloOuPadrao(String padrao) {
            if (titulo != null && !titulo.isEmpty() && !titulo.equals("null")) {
                return titulo;
            }
            return padrao;
        }

        public String getLocutorOuPadrao(String padrao) {
            if (locutor != null && !locutor.isEmpty() && !locutor.equals("null")) {
                return locutor;
            }
            return padrao;
        }

        @Override
        public String toString() {
            return "ProgramaAtual{" +
                    "titulo='" + titulo + '\'' +
                    ", locutor='" + locutor + '\'' +
                    ", horario='" + horario + '\'' +
                    ", imagem='" + imagem + '\'' +
                    '}';
        }
    }
}
