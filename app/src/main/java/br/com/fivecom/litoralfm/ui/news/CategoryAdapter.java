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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.news.Categoria;
import br.com.fivecom.litoralfm.ui.news.selection.CategorySelectionStrategy;
import br.com.fivecom.litoralfm.ui.news.selection.RegularCategoryStrategy;
import br.com.fivecom.litoralfm.ui.news.selection.TodosCategoryStrategy;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Categoria> categorias;
    private Set<Integer> selectedCategoryIds;
    private Context context;
    private OnCategorySelectionListener listener;

    // Strategy Pattern - elimina condicionais complexos
    private final Map<Integer, CategorySelectionStrategy> strategies;

    public interface OnCategorySelectionListener {
        void onCategorySelectionChanged(Set<Integer> selectedIds);
    }

    public CategoryAdapter(Context context, List<Categoria> categorias) {
        this.context = context;
        this.categorias = categorias != null ? categorias : new ArrayList<>();
        this.selectedCategoryIds = new HashSet<>();

        // Inicializa strategies
        this.strategies = new HashMap<>();
        this.strategies.put(0, new TodosCategoryStrategy()); // ID 0 = "Todas"
        // Todas as outras categorias usam RegularCategoryStrategy como padrão
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
        private final RegularCategoryStrategy defaultStrategy = new RegularCategoryStrategy();

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }

        public void bind(Categoria categoria, boolean isSelected) {
            txtCategory.setText(categoria.getNome());

            // Atualiza visual baseado na seleção
            updateVisualState(isSelected);

            // Remove listener anterior para evitar múltiplos listeners
            itemView.setOnClickListener(null);

            // Define click listener com Strategy Pattern
            itemView.setOnClickListener(v -> {
                int categoryId = categoria.getId();

                // Seleciona a estratégia apropriada
                // ID 0 = TodosCategoryStrategy, outros = RegularCategoryStrategy
                CategorySelectionStrategy strategy = strategies.getOrDefault(categoryId, defaultStrategy);

                // Executa a estratégia e verifica se precisa atualizar todos os itens
                boolean needsFullUpdate = strategy.handleClick(selectedCategoryIds, categoryId);

                // Atualiza UI
                if (needsFullUpdate) {
                    // Atualiza todos os itens (quando "Todas" é selecionada/desmarcada)
                    notifyDataSetChanged();
                } else {
                    // Atualiza apenas este item e o visual imediatamente
                    boolean nowSelected = selectedCategoryIds.contains(categoryId);
                    updateVisualState(nowSelected);

                    // Garante consistência com RecyclerView
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        notifyItemChanged(position);
                    }
                }

                // Notifica listener sobre mudança
                if (listener != null) {
                    listener.onCategorySelectionChanged(selectedCategoryIds);
                }
            });
        }

        /**
         * Atualiza o visual do item baseado no estado de seleção.
         * Método extraído para evitar duplicação de código.
         */
        private void updateVisualState(boolean isSelected) {
            if (isSelected) {
                txtCategory.setBackgroundResource(R.drawable.bg_category_selected);
                txtCategory.setTextColor(ContextCompat.getColor(context, R.color.white));
                txtCategory.setAlpha(1.0f); // Cor normal quando selecionada
            } else {
                txtCategory.setBackgroundResource(R.drawable.bg_category);
                txtCategory.setTextColor(ContextCompat.getColor(context, R.color.white));
                txtCategory.setAlpha(0.5f); // Opaca quando não selecionada
            }
        }
    }
}
