package br.com.fivecom.litoralfm.ui.news.selection;

import java.util.Set;

/**
 * Estratégia para categoria "Todas" (ID = 0).
 * 
 * Comportamento:
 * - Primeiro clique: Seleciona "Todas" e desmarca todas as outras
 * - Segundo clique: Mantém "Todas" selecionada (não permite desmarcar)
 */
public class TodosCategoryStrategy implements CategorySelectionStrategy {

    @Override
    public boolean handleClick(Set<Integer> selectedIds, int categoryId) {
        // Se "Todas" já está selecionada, não faz nada
        // (não permite desmarcar "Todas" quando ela é a única selecionada)
        if (selectedIds.contains(categoryId)) {
            return false; // Não precisa atualizar nada
        }

        // Caso contrário, seleciona apenas "Todas" e desmarca todas as outras
        selectedIds.clear();
        selectedIds.add(categoryId); // categoryId = 0 (Todas)

        return true; // Precisa notifyDataSetChanged para atualizar todas as categorias
    }
}
