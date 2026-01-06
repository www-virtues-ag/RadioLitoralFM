package br.com.fivecom.litoralfm.ui.views.shimmer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerFrameLayout;

final class ShimmerViewHolder extends RecyclerView.ViewHolder {

    private final ShimmerFrameLayout mShimmer;

    ShimmerViewHolder(@NonNull ShimmerFrameLayout itemView) {
        super(itemView);
        this.mShimmer = itemView;
    }

    final void bindView(Shimmer shimmer) {
        mShimmer.setShimmer(shimmer).startShimmer();
    }
}
