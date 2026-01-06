package br.com.fivecom.litoralfm.news;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.news.Noticia;
import br.com.fivecom.litoralfm.ui.news.NoticiaDetailActivity;

/**
 * Adapter para exibir notícias na RecyclerView horizontal
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Noticia> noticias;
    private Context context;
    private static final RequestOptions glideOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.live)
            .error(R.drawable.live);

    public NewsAdapter(Context context) {
        this.context = context;
        this.noticias = new ArrayList<>();
    }

    public void setNoticias(List<Noticia> newNoticias) {
        if (newNoticias == null) {
            newNoticias = new ArrayList<>();
        }
        
        // Usar DiffUtil para atualizações eficientes
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NoticiaDiffCallback(this.noticias, newNoticias));
        this.noticias = newNoticias;
        diffResult.dispatchUpdatesTo(this);
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

        private ImageView imgNoticia;
        private TextView txtTitulo;
        private TextView txtCategoria;
        private TextView txtData;
        private TextView txtDescricao;
        private ImageView btnVerMais;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs do layout activity_cards.xml
            imgNoticia = itemView.findViewById(R.id.ivNoticia);
            txtTitulo = itemView.findViewById(R.id.tvTitulo);
            txtCategoria = itemView.findViewById(R.id.txt_categoria_badge);
            txtData = itemView.findViewById(R.id.txt_data_hora);
            txtDescricao = itemView.findViewById(R.id.txt_descricao_noticia);
            btnVerMais = itemView.findViewById(R.id.btnVerMais);
        }

        public void bind(final Noticia noticia) {
            // Título
            if (txtTitulo != null) {
                txtTitulo.setText(noticia.getTitulo() != null ? noticia.getTitulo() : "");
            }

            // Categoria
            if (txtCategoria != null) {
                String categoria = noticia.getCategoriaFormatada();
                txtCategoria.setText(categoria);
            }

            // Data formatada
            if (txtData != null) {
                String dataFormatada = noticia.getDataFormatada();
                txtData.setText(dataFormatada);
            }

            // Descrição
            if (txtDescricao != null) {
                String descricao = noticia.getDescricao();
                if (descricao != null && !descricao.trim().isEmpty()) {
                    txtDescricao.setText(descricao);
                    txtDescricao.setVisibility(View.VISIBLE);
                } else {
                    // Se não houver descrição, usar início do conteúdo
                    String conteudo = noticia.getConteudo();
                    if (conteudo != null && !conteudo.trim().isEmpty()) {
                        String descricaoDoConteudo = conteudo.trim();
                        if (descricaoDoConteudo.length() > 200) {
                            descricaoDoConteudo = descricaoDoConteudo.substring(0, 200) + "...";
                        }
                        txtDescricao.setText(descricaoDoConteudo);
                        txtDescricao.setVisibility(View.VISIBLE);
                    } else {
                        txtDescricao.setVisibility(View.GONE);
                    }
                }
            }

            // Imagem usando Glide com otimizações
            if (imgNoticia != null) {
                if (noticia.getImagem() != null && !noticia.getImagem().isEmpty()) {
                    Glide.with(context)
                            .load(noticia.getImagem())
                            .apply(glideOptions)
                            .into(imgNoticia);
                } else {
                    imgNoticia.setImageResource(R.drawable.live);
                }
            }

            // Click listener reutilizado (não criar novo a cada bind)
            itemView.setOnClickListener(v -> openNoticiaDetail(noticia));
            if (btnVerMais != null) {
                btnVerMais.setOnClickListener(v -> openNoticiaDetail(noticia));
            }
        }
        
        private void openNoticiaDetail(Noticia noticia) {
            if (noticia == null) {
                Log.e("NewsAdapter", "❌ ERRO: Notícia é null");
                return;
            }
            
            Context activityContext = context;
            
            // Obter Activity do contexto
            android.app.Activity activity = null;
            if (activityContext instanceof android.app.Activity) {
                activity = (android.app.Activity) activityContext;
            } else if (activityContext instanceof android.content.ContextWrapper) {
                while (activityContext instanceof android.content.ContextWrapper) {
                    if (activityContext instanceof android.app.Activity) {
                        activity = (android.app.Activity) activityContext;
                        break;
                    }
                    activityContext = ((android.content.ContextWrapper) activityContext).getBaseContext();
                }
            }
            
            // Se ainda não encontrou, tentar obter do Fragment
            if (activity == null && context instanceof android.view.ContextThemeWrapper) {
                android.view.ContextThemeWrapper wrapper = (android.view.ContextThemeWrapper) context;
                activity = (android.app.Activity) wrapper.getBaseContext();
            }
            
            if (activity == null) {
                Log.e("NewsAdapter", "❌ ERRO: Não foi possível obter Activity do contexto");
                // Tentar usar o contexto diretamente como fallback
                try {
                    Intent intent = new Intent(context, NoticiaDetailActivity.class);
                    intent.putExtra("noticia_id", noticia.getId());
                    intent.putExtra("noticia_titulo", noticia.getTitulo());
                    intent.putExtra("noticia_descricao", noticia.getDescricao());
                    intent.putExtra("noticia_conteudo", noticia.getConteudo());
                    intent.putExtra("noticia_data", noticia.getData());
                    intent.putExtra("noticia_imagem", noticia.getImagem());
                    intent.putExtra("noticia_categoria", noticia.getCategoriaFormatada());
                    intent.putExtra("noticia_link", noticia.getLink());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    Log.d("NewsAdapter", "✅ Activity iniciada usando contexto direto");
                    return;
                } catch (Exception e) {
                    Log.e("NewsAdapter", "❌ ERRO ao iniciar Activity com contexto direto: " + e.getMessage(), e);
                    return;
                }
            }
            
            Intent intent = new Intent(activity, NoticiaDetailActivity.class);
            intent.putExtra("noticia_id", noticia.getId());
            intent.putExtra("noticia_titulo", noticia.getTitulo());
            intent.putExtra("noticia_descricao", noticia.getDescricao());
            intent.putExtra("noticia_conteudo", noticia.getConteudo());
            intent.putExtra("noticia_data", noticia.getData());
            intent.putExtra("noticia_imagem", noticia.getImagem());
            intent.putExtra("noticia_categoria", noticia.getCategoriaFormatada());
            intent.putExtra("noticia_link", noticia.getLink());
            
            try {
                activity.startActivity(intent);
                Log.d("NewsAdapter", "✅ Activity iniciada com sucesso - Notícia: " + (noticia.getTitulo() != null ? noticia.getTitulo().substring(0, Math.min(30, noticia.getTitulo().length())) : "sem título"));
            } catch (android.content.ActivityNotFoundException e) {
                Log.e("NewsAdapter", "❌ ERRO: Activity não encontrada: " + e.getMessage(), e);
                Toast.makeText(activity, "Erro ao abrir notícia", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("NewsAdapter", "❌ ERRO ao abrir notícia: " + e.getMessage(), e);
                Toast.makeText(activity, "Erro ao abrir notícia", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // DiffUtil callback para atualizações eficientes
    private static class NoticiaDiffCallback extends DiffUtil.Callback {
        private final List<Noticia> oldList;
        private final List<Noticia> newList;
        
        NoticiaDiffCallback(List<Noticia> oldList, List<Noticia> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        
        @Override
        public int getOldListSize() {
            return oldList.size();
        }
        
        @Override
        public int getNewListSize() {
            return newList.size();
        }
        
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }
        
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Noticia oldNoticia = oldList.get(oldItemPosition);
            Noticia newNoticia = newList.get(newItemPosition);
            return oldNoticia.getId() == newNoticia.getId() &&
                   java.util.Objects.equals(oldNoticia.getTitulo(), newNoticia.getTitulo());
        }
    }
}
