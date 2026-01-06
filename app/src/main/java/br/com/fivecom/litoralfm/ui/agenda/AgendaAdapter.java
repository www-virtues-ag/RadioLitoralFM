package br.com.fivecom.litoralfm.ui.agenda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.agenda.AgendaItem;

/**
 * Adapter for agenda items displayed in a 2-column grid
 * Based on Swift AgendaView implementation
 */
public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.AgendaViewHolder> {

    private Context context;
    private List<AgendaItem> agenda;
    private OnAgendaItemClickListener listener;

    public interface OnAgendaItemClickListener {
        void onAgendaItemClick(AgendaItem item);
    }

    public AgendaAdapter(Context context, List<AgendaItem> agenda) {
        this.context = context;
        this.agenda = agenda;
    }

    public void setOnAgendaItemClickListener(OnAgendaItemClickListener listener) {
        this.listener = listener;
    }

    public void updateAgenda(List<AgendaItem> newAgenda) {
        this.agenda = newAgenda;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AgendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_agenda, parent, false);
        return new AgendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgendaViewHolder holder, int position) {
        AgendaItem item = agenda.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return (agenda == null) ? 0 : agenda.size();
    }

    class AgendaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAgenda;
        TextView txtTitulo;
        TextView txtDescricao;

        public AgendaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAgenda = itemView.findViewById(R.id.img_agenda);
            txtTitulo = itemView.findViewById(R.id.txt_titulo_agenda);
            txtDescricao = itemView.findViewById(R.id.txt_desc_agenda);
        }

        public void bind(AgendaItem item) {
            // Título
            if (item.titulo != null && !item.titulo.isEmpty()) {
                txtTitulo.setText(item.titulo);
            } else {
                txtTitulo.setText("Título");
            }

            // Descrição
            String descricao = item.getDescricaoSemHTML();
            if (descricao != null && !descricao.isEmpty()) {
                txtDescricao.setText(descricao);
            } else {
                txtDescricao.setText("Conteúdo Agenda");
            }

            // Carregar imagem com Glide
            String imageUrl = item.getImagemURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.live)
                        .error(R.drawable.live)
                        .centerCrop()
                        .into(imgAgenda);
            } else {
                imgAgenda.setImageResource(R.drawable.live);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAgendaItemClick(item);
                }
            });
        }
    }
}
