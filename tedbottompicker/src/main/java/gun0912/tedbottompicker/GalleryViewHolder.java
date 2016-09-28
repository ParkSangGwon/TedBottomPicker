package gun0912.tedbottompicker;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import gun0912.tedbottompicker.view.TedSquareFrameLayout;
import gun0912.tedbottompicker.view.TedSquareImageView;

/**
 * Created by broto on 9/28/16.
 */

public class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

    public TedSquareFrameLayout root;
    public TedSquareImageView ivThumbnail;
    public RelativeLayout layoutSelectedOverlay;
    public ImageView imgSelectedIcon;

    private ClickListener listener;

    public GalleryViewHolder(View view, ClickListener listener) {
        super(view);
        this.listener = listener;

        root = (TedSquareFrameLayout) view.findViewById(R.id.root);
        ivThumbnail = (TedSquareImageView) view.findViewById(R.id.iv_thumbnail);
        layoutSelectedOverlay = (RelativeLayout) view.findViewById(R.id.layout_selected_overlay);
        imgSelectedIcon = (ImageView) view.findViewById(R.id.img_selected_icon);

        root.setOnClickListener(this);
        root.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onItemClicked(getAdapterPosition ());
        }
    }

    @Override
    public boolean onLongClick (View view) {
        if (listener != null) {
            return listener.onItemLongClicked(getAdapterPosition ());
        }
        return false;
    }

    public interface ClickListener {
        void onItemClicked(int position);

        boolean onItemLongClicked(int position);
    }
}