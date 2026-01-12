package br.com.fivecom.litoralfm.ui.news.selection;

import java.util.Set;

/**
 * Estratégia para categorias normais (ID != 0).
 * 
 * Comportamento:
 * - Se categoria já está selecionada: Desmarca
 * - Se não há mais seleções após desmarcar: Seleciona "Todas" automaticamente
 * - Se categoria não está selecionada: Remove "Todas" e adiciona esta categoria
 */
public class RegularCategoryStrategy implements CategorySelectionStrategy {

    @Override
    public boolean handleClick(Set<Integer> selectedIds, int categoryId) {
        boolean needsFullUpdate = false;

        if (selectedIds.contains(categoryId)) {
            // Categoria já selecionada - desmarca
            selectedIds.remove(categoryId);

            // Se não há mais nenhuma categoria selecionada, seleciona "Todas" como padrão
            if (selectedIds.isEmpty()) {
                selectedIds.add(0); // 0 = "Todas"
                needsFullUpdate = true; // Precisa atualizar todas as categorias
            }
            // Se ainda há outras categorias, só precisa atualizar este item

        } else {
            // Categoria não selecionada - adiciona
            boolean hadTodas = selectedIds.contains(0);
            selectedIds.remove(0); // Remove "Todas" se estiver selecionada
            selectedIds.add(categoryId);

            // Se tinha "Todas" selecionada, precisa atualizar todas as categorias
            needsFullUpdate = hadTodas;
        }

        return needsFullUpdate;
    }
}
