package br.com.fivecom.litoralfm.ui.news;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.news.Noticia;

/**
 * Adapter para exibir notícias usando o layout activity_cards
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Noticia> noticias;
    private Context context;

    public NewsAdapter(Context context) {
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
                .inflate(R.layout.activity_cards, parent, false);
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

        private ShapeableImageView ivNoticia;
        private TextView tvTitulo;
        private TextView txtDescricaoNoticia;
        private TextView txtDataHora;
        private TextView txtCategoriaBadge;
        private ImageView btnVerMais;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNoticia = itemView.findViewById(R.id.ivNoticia);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            txtDescricaoNoticia = itemView.findViewById(R.id.txt_descricao_noticia);
            txtDataHora = itemView.findViewById(R.id.txt_data_hora);
            txtCategoriaBadge = itemView.findViewById(R.id.txt_categoria_badge);
            btnVerMais = itemView.findViewById(R.id.btnVerMais);
        }

        public void bind(final Noticia noticia) {
            // Título
            if (tvTitulo != null) {
                tvTitulo.setText(noticia.getTitulo() != null ? noticia.getTitulo() : "");
            }

            // Descrição
            if (txtDescricaoNoticia != null) {
                String descricao = noticia.getDescricao() != null ? noticia.getDescricao() : "";
                txtDescricaoNoticia.setText(descricao);
            }

            // Data/Hora formatada
            if (txtDataHora != null) {
                String dataFormatada = noticia.getDataFormatada();
                // Formato simplificado para o card: "14h 19/12"
                if (dataFormatada != null && !dataFormatada.isEmpty()) {
                    // Extrai hora e data do formato "14h09 - 19/05/2025"
                    String[] parts = dataFormatada.split(" - ");
                    if (parts.length >= 2) {
                        String hora = parts[0].replace("h", "h "); // "14h09" -> "14h 09"
                        String data = parts[1]; // "19/05/2025"
                        // Pega apenas dia/mês
                        String[] dataParts = data.split("/");
                        if (dataParts.length >= 2) {
                            txtDataHora.setText(hora + " " + dataParts[0] + "/" + dataParts[1]);
                        } else {
                            txtDataHora.setText(dataFormatada);
                        }
                    } else {
                        txtDataHora.setText(dataFormatada);
                    }
                } else {
                    txtDataHora.setText("");
                }
            }

            // Categoria badge
            if (txtCategoriaBadge != null) {
                String categoria = noticia.getCategoriaFormatada();
                txtCategoriaBadge.setText(categoria != null ? categoria : "Notícia");
            }

            // Imagem usando Glide
            if (ivNoticia != null) {
                if (noticia.getImagem() != null && !noticia.getImagem().isEmpty()) {
                    Glide.with(context)
                            .load(noticia.getImagem())
                            .placeholder(R.drawable.live)
                            .error(R.drawable.live)
                            .centerCrop()
                            .into(ivNoticia);
                } else {
                    ivNoticia.setImageResource(R.drawable.live);
                }
            }

            // Click listener para abrir detalhes (no card inteiro)
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

            // Click listener no botão "Ver Mais"
            if (btnVerMais != null) {
                btnVerMais.setOnClickListener(new View.OnClickListener() {
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
}
