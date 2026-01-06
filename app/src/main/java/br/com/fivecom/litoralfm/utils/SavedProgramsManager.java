package br.com.fivecom.litoralfm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.util.ScheduleNotificationHelper;

/**
 * Gerenciador para salvar e recuperar programas salvos pelo usuário
 */
public class SavedProgramsManager {

    private static final String PREFS_NAME = "saved_programs_prefs";
    private static final String KEY_SAVED_PROGRAMS = "saved_programs_list";
    private static final String KEY_SAVED_PROGRAM_IDS = "saved_program_ids";
    private static final String KEY_READ_PROGRAM_IDS = "read_program_ids";

    private final SharedPreferences prefs;
    private final Gson gson;
    private final Context context;
    private final ScheduleNotificationHelper notificationHelper;

    public SavedProgramsManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.notificationHelper = new ScheduleNotificationHelper(this.context);
        
        // Garante que o canal de notificação está criado
        ScheduleNotificationHelper.createNotificationChannel(this.context);
    }

    /**
     * Salva um programa na lista de salvos
     */
    public void saveProgram(ProgramaAPI programa) {
        if (programa == null) return;

        List<ProgramaAPI> savedPrograms = getSavedPrograms();
        
        // Verifica se já existe (evita duplicatas)
        String programKey = getProgramKey(programa);
        boolean alreadyExists = false;
        for (ProgramaAPI p : savedPrograms) {
            if (getProgramKey(p).equals(programKey)) {
                alreadyExists = true;
                break;
            }
        }

        if (!alreadyExists) {
            savedPrograms.add(programa);
            saveProgramsList(savedPrograms);
            
            // Agenda notificação 5 minutos antes do programa começar
            scheduleNotification(programa);
            
            // Programa novo começa como não lido (não adiciona ao Set de lidos)
        }

        // Atualiza o Set de IDs também (para compatibilidade com ScheduleAdapter)
        Set<String> savedIds = getSavedProgramIds();
        savedIds.add(programKey);
        saveProgramIds(savedIds);
    }

    /**
     * Remove um programa da lista de salvos
     */
    public void removeProgram(ProgramaAPI programa) {
        if (programa == null) return;

        List<ProgramaAPI> savedPrograms = getSavedPrograms();
        String programKey = getProgramKey(programa);

        savedPrograms.removeIf(p -> getProgramKey(p).equals(programKey));
        saveProgramsList(savedPrograms);

        // Cancela a notificação do programa
        cancelNotification(programa);

        // Remove também da lista de lidos
        Set<String> readIds = getReadProgramIds();
        readIds.remove(programKey);
        saveReadProgramIds(readIds);

        // Atualiza o Set de IDs também
        Set<String> savedIds = getSavedProgramIds();
        savedIds.remove(programKey);
        saveProgramIds(savedIds);
    }

    /**
     * Verifica se um programa está salvo
     */
    public boolean isProgramSaved(ProgramaAPI programa) {
        if (programa == null) return false;
        
        String programKey = getProgramKey(programa);
        Set<String> savedIds = getSavedProgramIds();
        return savedIds.contains(programKey);
    }

    /**
     * Marca um programa como lido
     */
    public void markAsRead(ProgramaAPI programa) {
        if (programa == null) return;
        
        String programKey = getProgramKey(programa);
        Set<String> readIds = getReadProgramIds();
        readIds.add(programKey);
        saveReadProgramIds(readIds);
    }

    /**
     * Marca todos os programas como lidos
     */
    public void markAllAsRead() {
        List<ProgramaAPI> savedPrograms = getSavedPrograms();
        Set<String> readIds = getReadProgramIds();
        
        for (ProgramaAPI programa : savedPrograms) {
            String programKey = getProgramKey(programa);
            readIds.add(programKey);
        }
        
        saveReadProgramIds(readIds);
    }

    /**
     * Verifica se um programa foi lido
     */
    public boolean isProgramRead(ProgramaAPI programa) {
        if (programa == null) return false;
        
        String programKey = getProgramKey(programa);
        Set<String> readIds = getReadProgramIds();
        return readIds.contains(programKey);
    }

    /**
     * Retorna o Set de IDs dos programas lidos
     */
    private Set<String> getReadProgramIds() {
        Set<String> read = prefs.getStringSet(KEY_READ_PROGRAM_IDS, null);
        return read != null ? read : new HashSet<>();
    }

    /**
     * Salva o Set de IDs dos programas lidos
     */
    private void saveReadProgramIds(Set<String> readIds) {
        prefs.edit().putStringSet(KEY_READ_PROGRAM_IDS, new HashSet<>(readIds)).apply();
    }

    /**
     * Retorna todos os programas salvos
     */
    public List<ProgramaAPI> getSavedPrograms() {
        String json = prefs.getString(KEY_SAVED_PROGRAMS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<ProgramaAPI>>() {}.getType();
            List<ProgramaAPI> programs = gson.fromJson(json, type);
            return programs != null ? programs : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retorna o Set de IDs dos programas salvos (para compatibilidade)
     */
    public Set<String> getSavedProgramIds() {
        Set<String> saved = prefs.getStringSet(KEY_SAVED_PROGRAM_IDS, null);
        return saved != null ? saved : new HashSet<>();
    }

    /**
     * Salva a lista completa de programas
     */
    private void saveProgramsList(List<ProgramaAPI> programs) {
        String json = gson.toJson(programs);
        prefs.edit().putString(KEY_SAVED_PROGRAMS, json).apply();
    }

    /**
     * Salva o Set de IDs (para compatibilidade)
     */
    private void saveProgramIds(Set<String> ids) {
        prefs.edit().putStringSet(KEY_SAVED_PROGRAM_IDS, new HashSet<>(ids)).apply();
    }

    /**
     * Gera uma chave única para um programa
     */
    private String getProgramKey(ProgramaAPI programa) {
        if (programa == null) return "";
        String id = programa.getId();
        if (id != null && !id.trim().isEmpty()) {
            return id;
        }
        // Fallback: use title + start time
        String title = programa.getTitle() != null ? programa.getTitle() : "";
        String inicio = programa.getHrInicio() != null ? programa.getHrInicio() : "";
        return (title + "|" + inicio).trim();
    }

    /**
     * Limpa todos os programas salvos
     */
    public void clearAll() {
        // Cancela todas as notificações antes de limpar
        List<ProgramaAPI> savedPrograms = getSavedPrograms();
        for (ProgramaAPI programa : savedPrograms) {
            cancelNotification(programa);
        }
        
        prefs.edit()
                .remove(KEY_SAVED_PROGRAMS)
                .remove(KEY_SAVED_PROGRAM_IDS)
                .remove(KEY_READ_PROGRAM_IDS)
                .apply();
    }

    /**
     * Agenda uma notificação para um programa (5 minutos antes)
     */
    private void scheduleNotification(ProgramaAPI programa) {
        if (programa == null || notificationHelper == null) {
            return;
        }

        String programKey = getProgramKey(programa);
        String title = programa.getTitle() != null ? programa.getTitle() : "Programa";
        String hrInicio = programa.getHrInicio();
        String dayOfWeek = programa.getNrDiaSemana();

        if (hrInicio != null && !hrInicio.trim().isEmpty()) {
            // Agenda notificação 5 minutos antes do programa começar
            notificationHelper.scheduleNotification(programKey, title, hrInicio, dayOfWeek, 5);
            Log.d("SavedProgramsManager", "Notificação agendada para: " + title + " (5 min antes)");
        } else {
            Log.w("SavedProgramsManager", "Não foi possível agendar notificação: hrInicio vazio");
        }
    }

    /**
     * Cancela a notificação de um programa
     */
    private void cancelNotification(ProgramaAPI programa) {
        if (programa == null || notificationHelper == null) {
            return;
        }

        String programKey = getProgramKey(programa);
        notificationHelper.cancelNotification(programKey);
        Log.d("SavedProgramsManager", "Notificação cancelada para: " + programKey);
    }
}

