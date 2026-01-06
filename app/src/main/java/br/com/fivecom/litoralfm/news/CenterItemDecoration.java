package br.com.fivecom.litoralfm.news;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemDecoration para centralizar os cards no RecyclerView
 */
public class CenterItemDecoration extends RecyclerView.ItemDecoration {
    
    private static final int CARD_WIDTH_DP = 240; // Largura do card em dp (aproximadamente @dimen/_240sdp)
    
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        
        // Calcular o espaço disponível
        int parentWidth = parent.getWidth();
        int paddingStart = parent.getPaddingStart();
        int paddingEnd = parent.getPaddingEnd();
        int availableWidth = parentWidth - paddingStart - paddingEnd;
        
        // Medir a largura do view se ainda não foi medido
        if (view.getMeasuredWidth() == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
        }
        
        int childWidth = view.getMeasuredWidth();
        
        if (childWidth > 0 && availableWidth > childWidth) {
            // Centralizar o item
            int leftRightMargin = (availableWidth - childWidth) / 2;
            outRect.left = leftRightMargin;
            outRect.right = leftRightMargin;
        }
    }
}

