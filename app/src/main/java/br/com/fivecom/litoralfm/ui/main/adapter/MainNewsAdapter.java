package br.com.fivecom.litoralfm.ui.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.news.Noticia;
import br.com.fivecom.litoralfm.ui.news.NoticiaDetailActivity;

/**
 * Adapter para exibir notícias no MainFragment usando o layout item_news
 */
public class MainNewsAdapter extends RecyclerView.Adapter<MainNewsAdapter.NewsViewHolder> {

    private List<Noticia> noticias;
    private Context context;

    public MainNewsAdapter(Context context) {
        this.context = context;
        this.noticias = new ArrayList<>();
    }

    public void setNoticias(List<Noticia> noticias) {
        this.noticias = noticias != null ? noticias : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addNoticias(List<Noticia> novasNoticias) {
        if (novasNoticias != null && !novasNoticias.isEmpty()) {
            int posicaoInicial = this.noticias.size();
            this.noticias.addAll(novasNoticias);
            notifyItemRangeInserted(posicaoInicial, novasNoticias.size());
        }
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Noticia noticia = noticias.get(position);
        holder.bind(noticia);
    }

    @Override
    public int getItemCount() {
        return noticias.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgNoticia;
        private TextView txtTitulo;
        private TextView txtDesc;
        private TextView txtData;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNoticia = itemView.findViewById(R.id.img_noticia);
            txtTitulo = itemView.findViewById(R.id.txt_titulo);
            txtDesc = itemView.findViewById(R.id.txt_desc);
            txtData = itemView.findViewById(R.id.txt_data);
        }

        public void bind(final Noticia noticia) {
            // Título
            if (txtTitulo != null) {
                txtTitulo.setText(noticia.getTitulo() != null ? noticia.getTitulo() : "");
            }

            // Categoria
            if (txtDesc != null) {
                String categoria = noticia.getCategoriaFormatada();
                txtDesc.setText(categoria != null ? categoria : "Notícia");
            }

            // Data formatada
            if (txtData != null) {
                String dataFormatada = noticia.getDataFormatada();
                txtData.setText(dataFormatada != null ? dataFormatada : "");
            }

            // Imagem usando Glide
            if (imgNoticia != null) {
                if (noticia.getImagem() != null && !noticia.getImagem().isEmpty()) {
                    Glide.with(context)
                            .load(noticia.getImagem())
                            .placeholder(R.drawable.live)
                            .error(R.drawable.live)
                            .centerCrop()
                            .into(imgNoticia);
                } else {
                    imgNoticia.setImageResource(R.drawable.live);
                }
            }

            // Click listener para abrir detalhes
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, NoticiaDetailActivity.class);
                    intent.putExtra("noticia_id", noticia.getId());
                    intent.putExtra("noticia_titulo", noticia.getTitulo());
                    intent.putExtra("noticia_descricao", noticia.getDescricao());
                    intent.putExtra("noticia_conteudo", noticia.getConteudo());
                    intent.putExtra("noticia_data", noticia.getData());
                    intent.putExtra("noticia_imagem", noticia.getImagem());
                    intent.putExtra("noticia_categoria", noticia.getCategoriaNome());
                    intent.putExtra("noticia_link", noticia.getLink());
                    context.startActivity(intent);
                }
            });
        }
    }
}
