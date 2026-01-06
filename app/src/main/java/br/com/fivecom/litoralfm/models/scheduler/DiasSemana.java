package br.com.fivecom.litoralfm.models.scheduler;

import com.google.gson.annotations.SerializedName;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Modelo para os dias da semana com programas
 */
public class DiasSemana {
    
    @SerializedName("segunda")
    private List<Programa> segunda;
    
    @SerializedName("terca")
    private List<Programa> terca;
    
    @SerializedName("quarta")
    private List<Programa> quarta;
    
    @SerializedName("quinta")
    private List<Programa> quinta;
    
    @SerializedName("sexta")
    private List<Programa> sexta;
    
    @SerializedName("sabado")
    private List<Programa> sabado;
    
    @SerializedName("domingo")
    private List<Programa> domingo;

    public DiasSemana() {}

    public List<Programa> getSegunda() {
        return segunda != null ? segunda : Collections.emptyList();
    }

    public void setSegunda(List<Programa> segunda) {
        this.segunda = segunda;
    }

    public List<Programa> getTerca() {
        return terca != null ? terca : Collections.emptyList();
    }

    public void setTerca(List<Programa> terca) {
        this.terca = terca;
    }

    public List<Programa> getQuarta() {
        return quarta != null ? quarta : Collections.emptyList();
    }

    public void setQuarta(List<Programa> quarta) {
        this.quarta = quarta;
    }

    public List<Programa> getQuinta() {
        return quinta != null ? quinta : Collections.emptyList();
    }

    public void setQuinta(List<Programa> quinta) {
        this.quinta = quinta;
    }

    public List<Programa> getSexta() {
        return sexta != null ? sexta : Collections.emptyList();
    }

    public void setSexta(List<Programa> sexta) {
        this.sexta = sexta;
    }

    public List<Programa> getSabado() {
        return sabado != null ? sabado : Collections.emptyList();
    }

    public void setSabado(List<Programa> sabado) {
        this.sabado = sabado;
    }

    public List<Programa> getDomingo() {
        return domingo != null ? domingo : Collections.emptyList();
    }

    public void setDomingo(List<Programa> domingo) {
        this.domingo = domingo;
    }

    /**
     * Retorna os programas para um dia específico da semana
     * @param diaSemana dia da semana (Calendar.SUNDAY = 1, Calendar.MONDAY = 2, etc.)
     * @return lista de programas do dia
     */
    public List<Programa> programasParaDia(int diaSemana) {
        switch (diaSemana) {
            case Calendar.SUNDAY:
                return getDomingo();
            case Calendar.MONDAY:
                return getSegunda();
            case Calendar.TUESDAY:
                return getTerca();
            case Calendar.WEDNESDAY:
                return getQuarta();
            case Calendar.THURSDAY:
                return getQuinta();
            case Calendar.FRIDAY:
                return getSexta();
            case Calendar.SATURDAY:
                return getSabado();
            default:
                return Collections.emptyList();
        }
    }
}
