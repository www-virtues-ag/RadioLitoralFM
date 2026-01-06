package br.com.fivecom.litoralfm.ui.main.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.utils.SavedProgramsManager;

/**
 * Adapter para exibir a lista de programas na programação
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private static final String PREFS_NAME = "schedule_prefs";
    private static final String KEY_SAVED_PROGRAMS = "saved_programs";

    private final Context context;
    private final SharedPreferences prefs;
    private final SavedProgramsManager savedProgramsManager;
    private List<ProgramaAPI> programas = new ArrayList<>();
    private Set<String> savedProgramIds = new HashSet<>();
    private OnItemClickListener onItemClickListener;
    private OnSaveChangedListener onSaveChangedListener;

    /**
     * Listener for when save state changes
     */
    public interface OnSaveChangedListener {
        void onSaveChanged(ProgramaAPI programa, boolean isSaved);
    }

    public void setOnSaveChangedListener(OnSaveChangedListener listener) {
        this.onSaveChangedListener = listener;
    }

    // Use just the program ID to ensure same program is recognized across all lists
    private String getProgramKey(ProgramaAPI p) {
        if (p == null)
            return "";
        String id = p.getId();
        // Use just the ID if available
        if (id != null && !id.trim().isEmpty()) {
            return id;
        }
        // Fallback: use title + start time
        String title = p.getTitle() != null ? p.getTitle() : "";
        String inicio = p.getHrInicio() != null ? p.getHrInicio() : "";
        return (title + "|" + inicio).trim();
    }

    public interface OnItemClickListener {
        void onItemClick(ProgramaAPI programa, int position);
    }

    public ScheduleAdapter(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.savedProgramsManager = new SavedProgramsManager(context);
        loadFromPrefs();
    }

    /**
     * Load saved program IDs from SharedPreferences
     * Agora também sincroniza com SavedProgramsManager
     */
    private void loadFromPrefs() {
        // Sincroniza com SavedProgramsManager
        savedProgramIds.clear();
        savedProgramIds.addAll(savedProgramsManager.getSavedProgramIds());
        
        // Mantém compatibilidade com o SharedPreferences antigo
        Set<String> saved = prefs.getStringSet(KEY_SAVED_PROGRAMS, null);
        if (saved != null) {
            savedProgramIds.addAll(saved);
        }
    }

    /**
     * Save program IDs to SharedPreferences
     */
    private void saveToPrefs() {
        prefs.edit().putStringSet(KEY_SAVED_PROGRAMS, new HashSet<>(savedProgramIds)).apply();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateProgramas(List<ProgramaAPI> newProgramas) {
        this.programas.clear();
        if (newProgramas != null) {
            this.programas.addAll(newProgramas);
        }
        // Recarrega os IDs salvos para garantir sincronização
        loadFromPrefs();
        notifyDataSetChanged();
    }

    /**
     * Toggle the save state for a program and reorder the list
     */
    private void toggleSave(ProgramaAPI programa) {
        String key = getProgramKey(programa);
        boolean isCurrentlySaved = savedProgramIds.contains(key);
        
        if (isCurrentlySaved) {
            // Remove o programa
            savedProgramIds.remove(key);
            savedProgramsManager.removeProgram(programa);
        } else {
            // Salva o programa
            savedProgramIds.add(key);
            savedProgramsManager.saveProgram(programa);
        }
        
        // Persist the saved set and refresh the UI (do not reorder the list)
        saveToPrefs();
        if (onSaveChangedListener != null) {
            onSaveChangedListener.onSaveChanged(programa, !isCurrentlySaved);
        }
        notifyDataSetChanged();
    }

    /**
     * Check if a program is saved
     */
    public boolean isSaved(ProgramaAPI programa) {
        return savedProgramIds.contains(getProgramKey(programa));
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ProgramaAPI programa = programas.get(position);
        holder.bind(programa, position);
    }

    @Override
    public int getItemCount() {
        return programas.size();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final ImageView scheduleImage;
        private final TextView scheduleTitle;
        private final TextView scheduleTime;
        private final TextView scheduleContent;
        private final ImageView saveButton;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleImage = itemView.findViewById(R.id.schedule_item_image);
            scheduleTitle = itemView.findViewById(R.id.txt_title);
            scheduleTime = itemView.findViewById(R.id.txt_time_value);
            scheduleContent = itemView.findViewById(R.id.txt_content);
            saveButton = itemView.findViewById(R.id.bt_save_schedule);
        }

        public void bind(ProgramaAPI programa, int position) {
            if (scheduleTitle != null && programa.getTitle() != null) {
                scheduleTitle.setText(programa.getTitle());
            }

            if (scheduleTime != null) {
                String horario = formatHorario(programa.getHrInicio(), programa.getHrFinal());
                scheduleTime.setText(horario);
            }

            if (scheduleContent != null) {
                if (programa.getNmLocutor() != null && !programa.getNmLocutor().isEmpty()) {
                    scheduleContent.setText(programa.getNmLocutor());
                } else if (programa.getDescription() != null) {
                    scheduleContent.setText(programa.getDescription());
                } else {
                    scheduleContent.setText("");
                }
            }

            if (scheduleImage != null && programa.getImage() != null && !programa.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(programa.getImage())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(scheduleImage);
            } else if (scheduleImage != null) {
                scheduleImage.setImageDrawable(null);
            }

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(programa, position);
                }
            });

            // Save button logic
            if (saveButton != null) {
                boolean isSaved = savedProgramIds.contains(getProgramKey(programa));
                saveButton
                        .setImageResource(isSaved ? R.drawable.bt_save_schedule_selected : R.drawable.bt_save_schedule);
                saveButton.setOnClickListener(v -> toggleSave(programa));
            }
        }

        private String formatHorario(String inicio, String fim) {
            boolean hasInicio = inicio != null && !inicio.trim().isEmpty();
            boolean hasFinal = fim != null && !fim.trim().isEmpty();

            if (!hasInicio && !hasFinal) {
                return "";
            }
            if (hasInicio && hasFinal) {
                return inicio + " - " + fim;
            }
            if (hasInicio) {
                return inicio;
            }
            return fim;
        }
    }
}
