package gun0912.tedbottompicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.Builder;
import gun0912.tedbottompicker.GalleryViewHolder;
import gun0912.tedbottompicker.PickerTile;
import gun0912.tedbottompicker.R;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class ImageGalleryAdapter extends SelectableAdapter<GalleryViewHolder> {

    private Context context;
    private Builder builder;
    private ArrayList<PickerTile> pickerTiles;
    private OnItemClickListener onItemClickListener;

    public ImageGalleryAdapter(Context context, Builder builder) {
        this.context = context;
        this.builder = builder;

        pickerTiles = new ArrayList<>();

        if (builder.showCamera) {
            pickerTiles.add(new PickerTile(PickerTile.CAMERA));
        }

        if (builder.showGallery) {
            pickerTiles.add(new PickerTile(PickerTile.GALLERY));
        }

        if (builder.remoteImages != null) {
            setupRemoteImages(builder.remoteImages);
        } else {
            setupLocalImages();
        }
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        GalleryViewHolder.ClickListener listener = new GalleryViewHolder.ClickListener() {
            @Override
            public void onItemClicked(int position) {

            }

            @Override
            public boolean onItemLongClicked(int position) {
                return false;
            }
        };

        return new GalleryViewHolder(View.inflate(context, R.layout.tedbottompicker_grid_item, null), listener);
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, final int position) {
        PickerTile pickerTile = getItem(position);

        if (pickerTile.isCameraTile()) {
            holder.ivThumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
            holder.ivThumbnail.setImageDrawable(builder.cameraTileDrawable);
        } else if (pickerTile.isGalleryTile()) {
            holder.ivThumbnail.setBackgroundResource(builder.galleryTileBackgroundResId);
            holder.ivThumbnail.setImageDrawable(builder.galleryTileDrawable);
        } else if(pickerTile.isRemoteTile()) {
            String imageUrl = pickerTile.getImageUri().toString();
            if (builder.imageProvider == null) {
                Glide.with(context)
                        .load(imageUrl)
                        .dontAnimate()
                        .centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error)
                        .into(holder.ivThumbnail);
            } else {
                builder.imageProvider.onProvideImage(holder.ivThumbnail, pickerTile.getImageUri());
            }
        } else {
            Uri uri = pickerTile.getImageUri();
            if (builder.imageProvider == null) {
                Glide.with(context)
                        .load(uri)
                        .thumbnail(0.1f)
                        .dontAnimate()
                        .centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error)
                        .into(holder.ivThumbnail);
            } else {
                builder.imageProvider.onProvideImage(holder.ivThumbnail, uri);
            }
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }

        holder.layoutSelectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return pickerTiles.size();
    }

    private void setupLocalImages() {
        Cursor imageCursor = null;
        try {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};
            final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";

            imageCursor = context.getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

            if (imageCursor != null) {
                int count = 0;
                while (imageCursor.moveToNext() && count < builder.maxCount) {
                    String imageLocation = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File imageFile = new File(imageLocation);
                    pickerTiles.add(new PickerTile(Uri.fromFile(imageFile)));
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (imageCursor != null && !imageCursor.isClosed()) {
                imageCursor.close();
            }
        }
    }

    private void setupRemoteImages(List<String> remoteImages) {
        for (String remoteImage : remoteImages) {
            pickerTiles.add(new PickerTile(Uri.parse(remoteImage), PickerTile.REMOTE));
        }
    }

    public PickerTile getItem(int position) {
        return pickerTiles.get(position);
    }

    public void setOnItemClickListener(
            OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
