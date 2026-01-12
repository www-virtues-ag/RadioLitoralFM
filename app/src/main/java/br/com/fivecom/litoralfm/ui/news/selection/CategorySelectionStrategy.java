package br.com.fivecom.litoralfm.ui.news.selection;

import java.util.Set;

/**
 * Strategy Pattern para lógica de seleção de categorias.
 * Elimina condicionais aninhados complexos no CategoryAdapter.
 * 
 * Cada estratégia implementa comportamento específico para um tipo de
 * categoria.
 */
public interface CategorySelectionStrategy {

    /**
     * Executa a lógica de seleção para uma categoria.
     * 
     * @param selectedIds Conjunto de IDs de categorias atualmente selecionadas
     *                    (será modificado)
     * @param categoryId  ID da categoria clicada
     * @return true se deve notificar mudança completa (notifyDataSetChanged),
     *         false se pode notificar apenas o item (notifyItemChanged)
     */
    boolean handleClick(Set<Integer> selectedIds, int categoryId);
}
