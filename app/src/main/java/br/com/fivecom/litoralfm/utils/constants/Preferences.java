package br.com.fivecom.litoralfm.utils.constants;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;


public class Preferences {

    private final SharedPreferences preferences;
    private final Gson gson;

    public Preferences(@NonNull Context context) {
        preferences = context.getSharedPreferences(context.getPackageName() + "_data", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public boolean isAnimationEnabled() {
        return preferences.getBoolean("animation", true);
    }

    public void setAnimationEnabled(boolean check) {
        preferences.edit().putBoolean("animation", check).apply();
    }

    public int getColorPlayer() {
        return preferences.getInt("color_player", R.color.white);
    }

    public void setColorPlayer(int color) {
        preferences.edit().putInt("color_player", color).apply();
    }

    /**
     * Retorna o programa atual baseado no horário e dia da semana
     * Adaptado para usar ProgramaAPI ao invés de Scheduler
     */
    public ProgramaAPI getProgramaAtual() {
        Calendar calendar = Calendar.getInstance();
        int diaSemanaAtual = calendar.get(Calendar.DAY_OF_WEEK);
        int horaAtual = calendar.get(Calendar.HOUR_OF_DAY);
        int minutoAtual = calendar.get(Calendar.MINUTE);
        int minutosAtuais = horaAtual * 60 + minutoAtual;

        List<ProgramaAPI> programas = getProgramasSalvos();
        if (programas == null || programas.isEmpty()) {
            return null;
        }

        String diaSemanaAPI = String.valueOf(diaSemanaAtual);

        for (ProgramaAPI programa : programas) {
            // Verifica se o programa é do dia atual
            if (programa.getNrDiaSemana() == null || !programa.getNrDiaSemana().equals(diaSemanaAPI)) {
                continue;
            }

            String hrInicio = programa.getHrInicio();
            String hrFinal = programa.getHrFinal();

            if (hrInicio == null || hrFinal == null) {
                continue;
            }

            try {
                int minutosInicio = parseHorario(hrInicio);
                int minutosFinal = parseHorario(hrFinal);

                // Verifica se o horário atual está dentro do intervalo do programa
                if (minutosFinal < minutosInicio) {
                    // Programa que cruza a meia-noite
                    if (minutosAtuais >= minutosInicio || minutosAtuais < minutosFinal) {
                        return programa;
                    }
                } else {
                    // Programa normal no mesmo dia
                    if (minutosAtuais >= minutosInicio && minutosAtuais < minutosFinal) {
                        return programa;
                    }
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return null;
    }

    /**
     * Método de compatibilidade - mantém o nome antigo mas retorna ProgramaAPI
     * @deprecated Use getProgramaAtual() ao invés disso
     */
    @Deprecated
    public ProgramaAPI getSchedulerNow() {
        return getProgramaAtual();
    }

    public void setIcon(int id) {
        preferences.edit().putInt("icon", id).apply();
    }

    public int getIcon() {
        return preferences.getInt("icon", 0);
    }

    public void setNightModeStatus(int id) {
        preferences.edit().putInt("night", id).apply();
    }

    public int nightModeStatus() {
        return preferences.getInt("night", AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void setRemember(boolean check) {
        preferences.edit().putBoolean("remember", check).apply();
    }

    public boolean getRemember() {
        return preferences.getBoolean("remember", false);
    }

    /**
     * Verifica se um programa está na lista de programas salvos
     */
    public boolean isScheduled(@NonNull ProgramaAPI programa) {
        List<ProgramaAPI> programas = getProgramasSalvos();
        if (programas == null || programas.isEmpty()) {
            return false;
        }

        for (ProgramaAPI p : programas) {
            if (programasIguais(p, programa)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adiciona um programa à lista de programas salvos
     */
    public void addScheduled(@NonNull ProgramaAPI programa) {
        List<ProgramaAPI> programas = getProgramasSalvos();
        if (programas == null) {
            programas = new ArrayList<>();
        }

        // Verifica se já existe antes de adicionar
        if (!isScheduled(programa)) {
            programas.add(programa);
            salvarProgramas(programas);
        }
    }

    /**
     * Remove um programa da lista de programas salvos
     */
    public void removeScheduled(@NonNull ProgramaAPI programa) {
        List<ProgramaAPI> programas = getProgramasSalvos();
        if (programas == null || programas.isEmpty()) {
            return;
        }

        programas.removeIf(p -> programasIguais(p, programa));
        salvarProgramas(programas);
    }

    /**
     * Salva uma lista completa de programas
     */
    public void salvarProgramas(@NonNull List<ProgramaAPI> programas) {
        String json = gson.toJson(programas);
        preferences.edit().putString("programas", json).apply();
    }

    /**
     * Retorna a lista de programas salvos
     */
    public List<ProgramaAPI> getProgramasSalvos() {
        String json = preferences.getString("programas", null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            java.lang.reflect.Type type = new TypeToken<List<ProgramaAPI>>(){}.getType();
            List<ProgramaAPI> programas = gson.fromJson(json, type);
            return programas != null ? programas : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Converte um horário no formato "HH:mm" ou "HHmm" para minutos totais do dia
     */
    private int parseHorario(String horario) {
        if (horario == null || horario.isEmpty()) {
            return 0;
        }

        try {
            int hora = 0;
            int minuto = 0;

            if (horario.contains(":")) {
                String[] parts = horario.split(":");
                hora = Integer.parseInt(parts[0]);
                minuto = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            } else if (horario.length() >= 2) {
                hora = Integer.parseInt(horario.substring(0, Math.min(2, horario.length())));
                if (horario.length() > 2) {
                    minuto = Integer.parseInt(horario.substring(2));
                }
            }

            return hora * 60 + minuto;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Compara dois programas para verificar se são iguais
     * Compara por ID, título, horário e dia da semana
     */
    private boolean programasIguais(ProgramaAPI p1, ProgramaAPI p2) {
        if (p1 == p2) return true;
        if (p1 == null || p2 == null) return false;

        // Compara por ID se ambos tiverem
        if (p1.getId() != null && p2.getId() != null) {
            return p1.getId().equals(p2.getId());
        }

        // Se não tiverem ID, compara por título, horário e dia
        boolean tituloIgual = (p1.getTitle() == null && p2.getTitle() == null) ||
                (p1.getTitle() != null && p1.getTitle().equals(p2.getTitle()));

        boolean horarioIgual = (p1.getHrInicio() == null && p2.getHrInicio() == null) ||
                (p1.getHrInicio() != null && p1.getHrInicio().equals(p2.getHrInicio()));

        boolean diaIgual = (p1.getNrDiaSemana() == null && p2.getNrDiaSemana() == null) ||
                (p1.getNrDiaSemana() != null && p1.getNrDiaSemana().equals(p2.getNrDiaSemana()));

        return tituloIgual && horarioIgual && diaIgual;
    }

    // Métodos auxiliares mantidos para compatibilidade
    private int parse(String s) {
        return parseHorario(s);
    }

    private String zero(int s) {
        if (s > 9) return "" + s;
        return "0" + s;
    }

    /*private String getJson1() {
        return preferences.getString("news", "");
    }

    public String getJson() {
        return ("[" + preferences.getString("news", "") + "]")
                .replace("[,", "[")
                .replace(",]", "]");
    }

    public void setJson(@NonNull String string) {
        if (!getJson1().isEmpty()) string = getJson1() + "," + string;
        preferences.edit().putString("news", string).apply();
    }

    public void removeJson(@NonNull String string) {
        preferences.edit().putString("news", getJson1().replace("," + string, "")).apply();
        preferences.edit().putString("news", getJson1().replace(string, "")).apply();
    }

    public boolean containsJson(@NonNull String string) {
        return getJson1().contains(string);
    }*/
}
