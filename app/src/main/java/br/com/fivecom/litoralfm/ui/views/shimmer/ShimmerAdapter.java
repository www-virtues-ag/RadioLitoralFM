package br.com.fivecom.litoralfm.ui.views.shimmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerFrameLayout;

import br.com.fivecom.litoralfm.R;

public final class ShimmerAdapter extends RecyclerView.Adapter<ShimmerViewHolder> {

    /**
     * A contract to change shimmer view type.
     */
    public interface ItemViewType {

        @LayoutRes
        int getItemViewType(@ShimmerRecyclerView.LayoutType int layoutManagerType, int position);
    }

    private Shimmer shimmer;

    @LayoutRes
    private int layout;

    private int itemCount;

    private int layoutManagerType;

    private ItemViewType itemViewType;


    @RecyclerView.Orientation
    private int mLayoutOrientation;


    ShimmerAdapter(@LayoutRes int layout, int itemCount, int layoutManagerType,
                   ItemViewType itemViewType, Shimmer shimmer, int layoutOrientation) {
        this.layout = layout;
        this.itemCount = validateCount(itemCount);
        this.layoutManagerType = layoutManagerType;
        this.itemViewType = itemViewType;
        this.shimmer = shimmer;
        this.mLayoutOrientation = layoutOrientation;
    }

    @Override
    public int getItemViewType(int position) {
        return (itemViewType != null)
                ? itemViewType.getItemViewType(layoutManagerType, position)
                : layout; /* default */
    }

    @NonNull
    @Override
    public ShimmerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        /* inflate view holder layout and then attach provided view in it. */
        View view = inflater.inflate(R.layout.shimmer_viewholder_layout,
                parent, false);

        if (mLayoutOrientation == RecyclerView.HORIZONTAL) {
            view.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        return new ShimmerViewHolder((ShimmerFrameLayout) inflater
                .inflate(viewType, (ShimmerFrameLayout) view,
                        true /* attach to view holder layout */));
    }

    @Override
    public void onBindViewHolder(@NonNull ShimmerViewHolder holder, int position) {
        holder.bindView(shimmer);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Internal APIs
    ///////////////////////////////////////////////////////////////////////////

    void setShimmer(Shimmer shimmer) {
        this.shimmer = shimmer;
    }

    void setLayout(@LayoutRes int layout) {
        this.layout = layout;
    }

    void setCount(int count) {
        this.itemCount = validateCount(count);
    }

    void setShimmerItemViewType(@ShimmerRecyclerView.LayoutType int layoutManagerType, ItemViewType itemViewType) {
        this.layoutManagerType = layoutManagerType;
        this.itemViewType = itemViewType;
    }

    /**
     * Validates if provided item count is greater than reasonable number
     * of items and returns max number of items allowed.
     * <p>
     * Try to save memory produced by shimmer layouts.
     *
     * @param count input number.
     * @return valid count number.
     */
    private int validateCount(int count) {
        return count < 20 ? count : 20;
    }
}
