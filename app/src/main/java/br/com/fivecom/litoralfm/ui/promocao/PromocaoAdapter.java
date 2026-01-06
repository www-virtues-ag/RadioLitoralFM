package br.com.fivecom.litoralfm.ui.promocao;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.promocao.Promocao;

/**
 * Adapter para exibir promoções na RecyclerView
 * Equivalente ao PromocaoCardView do Swift
 */
public class PromocaoAdapter extends RecyclerView.Adapter<PromocaoAdapter.PromocaoViewHolder> {

    private Context context;
    private List<Promocao> promocoes;
    private OnPromocaoClickListener listener;

    public interface OnPromocaoClickListener {
        void onPromocaoClick(Promocao promocao);
    }

    public PromocaoAdapter(Context context, List<Promocao> promocoes) {
        this.context = context;
        this.promocoes = promocoes;
    }

    public void setOnPromocaoClickListener(OnPromocaoClickListener listener) {
        this.listener = listener;
    }

    public void updatePromocoes(List<Promocao> newPromocoes) {
        this.promocoes = newPromocoes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromocaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_promotion, parent, false);
        return new PromocaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromocaoViewHolder holder, int position) {
        Promocao promocao = promocoes.get(position);
        holder.bind(promocao);
    }

    @Override
    public int getItemCount() {
        return (promocoes == null) ? 0 : promocoes.size();
    }

    class PromocaoViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivPromoImg;
        TextView ivTituloPromo;
        TextView ivTxtPromo;

        public PromocaoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPromoImg = itemView.findViewById(R.id.iv_promo_img);
            ivTituloPromo = itemView.findViewById(R.id.iv_titulo_promo);
            ivTxtPromo = itemView.findViewById(R.id.iv_txt_promo);
        }

        public void bind(Promocao promocao) {
            // Título
            if (promocao.getTitulo() != null && !promocao.getTitulo().isEmpty()) {
                ivTituloPromo.setText(promocao.getTitulo().toUpperCase());
            } else {
                ivTituloPromo.setText("TÍTULO DA PROMOÇÃO");
            }

            // Descrição
            String descricao = promocao.getDescricao();
            if (descricao != null && !descricao.isEmpty()) {
                ivTxtPromo.setText(descricao);
            } else {
                ivTxtPromo.setText("Descrição da promoção");
            }

            // Carregar imagem com Glide
            String imageUrl = promocao.getImagemURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.forma_prog_main)
                        .error(R.drawable.forma_prog_main)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop();

                Glide.with(context)
                        .load(imageUrl)
                        .apply(options)
                        .into(ivPromoImg);
            } else {
                ivPromoImg.setImageResource(R.drawable.forma_prog_main);
            }

            // Click listener - abre a tela de detalhes
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPromocaoClick(promocao);
                } else {
                    // Fallback: abrir tela de detalhes
                    Intent intent = new Intent(context, DetailPromocaoActivity.class);
                    intent.putExtra("promocao_item", promocao);
                    context.startActivity(intent);
                }
            });
        }
    }
}

