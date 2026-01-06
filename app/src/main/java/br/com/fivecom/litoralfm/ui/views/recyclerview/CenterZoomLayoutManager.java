package br.com.fivecom.litoralfm.ui.views.recyclerview;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterZoomLayoutManager extends LinearLayoutManager {

    private final float mShrinkAmount = 0.3f; // 1 - 0.8
    private final float mMinAlpha = 0.3f;

    public CenterZoomLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleChildViews();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
        scaleChildViews();
        return scrolled;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollVerticallyBy(dy, recycler, state);
        scaleChildViews();
        return scrolled;
    }

    private void scaleChildViews() {
        float mid = getOrientation() == HORIZONTAL ? getWidth() / 2.0f : getHeight() / 2.0f;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) continue;

            float childMid = getOrientation() == HORIZONTAL
                    ? (getDecoratedLeft(child) + getDecoratedRight(child)) / 2f
                    : (getDecoratedTop(child) + getDecoratedBottom(child)) / 2f;

            float distance = Math.abs(mid - childMid) / mid;
            distance = Math.min(1f, distance);

            float scale = 1 - mShrinkAmount * distance;
            float alpha = 1 - (1 - mMinAlpha) * distance;

            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(alpha);
        }
    }
}
