package br.com.fivecom.litoralfm.ui.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.news.Categoria;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Categoria> categorias;
    private Set<Integer> selectedCategoryIds;
    private Context context;
    private OnCategorySelectionListener listener;

    public interface OnCategorySelectionListener {
        void onCategorySelectionChanged(Set<Integer> selectedIds);
    }

    public CategoryAdapter(Context context, List<Categoria> categorias) {
        this.context = context;
        this.categorias = categorias != null ? categorias : new ArrayList<>();
        this.selectedCategoryIds = new HashSet<>();
    }

    public void setOnCategorySelectionListener(OnCategorySelectionListener listener) {
        this.listener = listener;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias != null ? categorias : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedCategoryIds() {
        return new HashSet<>(selectedCategoryIds);
    }

    public void clearSelection() {
        selectedCategoryIds.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onCategorySelectionChanged(selectedCategoryIds);
        }
    }

    public void setSelectedCategoryIds(Set<Integer> selectedIds) {
        this.selectedCategoryIds = selectedIds != null ? new HashSet<>(selectedIds) : new HashSet<>();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onCategorySelectionChanged(selectedCategoryIds);
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);
        holder.bind(categoria, selectedCategoryIds.contains(categoria.getId()));
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView txtCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }

        public void bind(Categoria categoria, boolean isSelected) {
            txtCategory.setText(categoria.getNome());

            // Atualiza visual baseado na seleção
            if (isSelected) {
                txtCategory.setBackgroundResource(R.drawable.bg_category_selected);
                txtCategory.setTextColor(ContextCompat.getColor(context, R.color.white));
                txtCategory.setAlpha(1.0f); // Cor normal quando selecionada
            } else {
                txtCategory.setBackgroundResource(R.drawable.bg_category);
                txtCategory.setTextColor(ContextCompat.getColor(context, R.color.white));
                txtCategory.setAlpha(0.5f); // Opaca quando não selecionada
            }
            
            // Remove o listener anterior para evitar múltiplos listeners
            itemView.setOnClickListener(null);

            itemView.setOnClickListener(v -> {
                int categoryId = categoria.getId();
                
                // Se clicou em "Todas" (ID 0)
                if (categoryId == 0) {
                    if (selectedCategoryIds.contains(0)) {
                        // Se já estava selecionada, desmarca (segundo clique)
                        // Mas não permite ficar sem nenhuma selecionada - mantém "Todas"
                        // Na verdade, se clicar novamente em "Todas" já selecionada, mantém selecionada
                        // (não desmarca para evitar estado vazio)
                        return; // Não faz nada se já está selecionada
                    } else {
                        // Seleciona apenas "Todas" e desmarca todas as outras (primeiro clique)
                        selectedCategoryIds.clear();
                        selectedCategoryIds.add(0);
                        notifyDataSetChanged(); // Atualiza todos os itens
                    }
                } else {
                    // Se clicou em outra categoria
                    if (selectedCategoryIds.contains(categoryId)) {
                        // Desmarca a categoria (segundo clique)
                        selectedCategoryIds.remove(categoryId);
                        
                        // Se não há mais nenhuma categoria selecionada, seleciona "Todas" como padrão
                        if (selectedCategoryIds.isEmpty()) {
                            selectedCategoryIds.add(0);
                            notifyDataSetChanged(); // Atualiza todos os itens
                        } else {
                            // Força atualização do item desmarcado para garantir que fique opaco
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                // Primeiro atualiza o visual imediatamente
                                txtCategory.setBackgroundResource(R.drawable.bg_category);
                                txtCategory.setTextColor(ContextCompat.getColor(context, R.color.white));
                                txtCategory.setAlpha(0.5f); // Opaca quando não selecionada
                                
                                // Depois notifica o RecyclerView para garantir consistência
                                notifyItemChanged(position);
                            } else {
                                notifyDataSetChanged(); // Fallback se posição inválida
                            }
                        }
                    } else {
                        // Remove "Todas" se estiver selecionada (primeiro clique)
                        boolean hadTodas = selectedCategoryIds.contains(0);
                        selectedCategoryIds.remove(0);
                        // Adiciona a categoria selecionada
                        selectedCategoryIds.add(categoryId);
                        
                        if (hadTodas) {
                            // Se tinha "Todas" selecionada, atualiza todos os itens
                            notifyDataSetChanged();
                        } else {
                            // Apenas atualiza este item
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                notifyItemChanged(position);
                            } else {
                                notifyDataSetChanged(); // Fallback se posição inválida
                            }
                        }
                    }
                }
                
                if (listener != null) {
                    listener.onCategorySelectionChanged(selectedCategoryIds);
                }
            });
        }
    }
}
