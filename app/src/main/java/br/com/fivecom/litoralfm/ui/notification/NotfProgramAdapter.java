package br.com.fivecom.litoralfm.ui.notification;

import android.content.Context;
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
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;
import br.com.fivecom.litoralfm.utils.SavedProgramsManager;

/**
 * Adapter para exibir os programas salvos na lista de notificações
 */
public class NotfProgramAdapter extends RecyclerView.Adapter<NotfProgramAdapter.NotfProgramViewHolder> {

    private final Context context;
    private List<ProgramaAPI> programas = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private SavedProgramsManager savedProgramsManager;

    public interface OnItemClickListener {
        void onItemClick(ProgramaAPI programa, int position);
    }

    public NotfProgramAdapter(Context context) {
        this.context = context.getApplicationContext();
        this.savedProgramsManager = new SavedProgramsManager(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateProgramas(List<ProgramaAPI> newProgramas) {
        this.programas.clear();
        if (newProgramas != null) {
            this.programas.addAll(newProgramas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotfProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notf_program, parent, false);
        return new NotfProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotfProgramViewHolder holder, int position) {
        ProgramaAPI programa = programas.get(position);
        holder.bind(programa, position);
    }

    @Override
    public int getItemCount() {
        return programas.size();
    }

    class NotfProgramViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProgram;
        private final ImageView imgUnreadIndicator;
        private final TextView txtTituloProgram;
        private final TextView txtDescProgram;

        public NotfProgramViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProgram = itemView.findViewById(R.id.img_program);
            imgUnreadIndicator = itemView.findViewById(R.id.img_unread_indicator);
            txtTituloProgram = itemView.findViewById(R.id.txt_titulo_program);
            txtDescProgram = itemView.findViewById(R.id.txt_desc_program);
        }

        public void bind(ProgramaAPI programa, int position) {
            // Título
            if (txtTituloProgram != null && programa.getTitle() != null) {
                txtTituloProgram.setText(programa.getTitle());
            }

            // Descrição - mostra apresentador ou descrição
            if (txtDescProgram != null) {
                String descricao = "";
                if (programa.getNmLocutor() != null && !programa.getNmLocutor().isEmpty()) {
                    descricao = programa.getNmLocutor();
                } else if (programa.getDescription() != null && !programa.getDescription().isEmpty()) {
                    // Remove HTML se houver
                    descricao = programa.getDescription().replaceAll("<[^>]*>", "").trim();
                    // Limita o tamanho
                    if (descricao.length() > 100) {
                        descricao = descricao.substring(0, 100) + "...";
                    }
                }
                
                if (descricao.isEmpty()) {
                    // Mostra horário como fallback
                    String horario = formatHorario(programa.getHrInicio(), programa.getHrFinal());
                    descricao = horario.isEmpty() ? "Programa salvo" : horario;
                }
                
                txtDescProgram.setText(descricao);
            }

            // Verifica se o programa foi lido
            boolean isRead = savedProgramsManager.isProgramRead(programa);

            // Imagem
            if (imgProgram != null && programa.getImage() != null && !programa.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(programa.getImage())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .placeholder(R.drawable.live)
                        .error(R.drawable.live)
                        .into(imgProgram);
            } else if (imgProgram != null) {
                imgProgram.setImageResource(R.drawable.live);
            }

            // Aplica opacidade se não foi lido
            if (imgProgram != null) {
                if (isRead) {
                    imgProgram.setAlpha(1.0f); // Totalmente visível
                } else {
                    imgProgram.setAlpha(0.5f); // Opaco (50% de opacidade)
                }
            }

            // Mostra/oculta o indicador de não lido
            if (imgUnreadIndicator != null) {
                if (isRead) {
                    imgUnreadIndicator.setVisibility(View.GONE);
                } else {
                    imgUnreadIndicator.setVisibility(View.VISIBLE);
                }
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(programa, position);
                }
            });
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

