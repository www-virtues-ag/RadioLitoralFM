package br.com.fivecom.litoralfm.ui.views.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int top;
    private final int bottom;

    public SpaceItemDecoration(Context context, int top, int bottom) {
        this.top = context.getResources().getDimensionPixelSize(top);
        this.bottom = context.getResources().getDimensionPixelSize(bottom);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();
        if (position == 0)
            outRect.top = top;
        if (position == itemCount - 1)
            outRect.bottom = bottom;
    }
}
