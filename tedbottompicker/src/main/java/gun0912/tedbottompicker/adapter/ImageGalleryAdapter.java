package gun0912.tedbottompicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.R;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.view.TedSquareFrameLayout;
import gun0912.tedbottompicker.view.TedSquareImageView;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.GalleryViewHolder> {

    private Context context;
    private TedBottomPicker.Builder builder;
    private ArrayList<PickerTile> pickerTiles;
    private OnItemClickListener onItemClickListener;

    public ImageGalleryAdapter(Context context, TedBottomPicker.Builder builder) {
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
        return new GalleryViewHolder(View.inflate(context, R.layout.tedbottompicker_grid_item, null));
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

    public static class PickerTile {

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int GALLERY = 3;
        public static final int REMOTE = 4;

        final Uri imageUri;
        @TileType
        final int tileType;

        PickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        PickerTile(@NonNull Uri imageUri) {
            this(imageUri, IMAGE);
        }

        protected PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
            this.imageUri = imageUri;
            this.tileType = tileType;
        }

        @Nullable
        public Uri getImageUri() {
            return imageUri;
        }

        @TileType
        public int getTileType() {
            return tileType;
        }

        public boolean isImageTile() {
            return tileType == IMAGE;
        }

        public boolean isCameraTile() {
            return tileType == CAMERA;
        }

        public boolean isGalleryTile() {
            return tileType == GALLERY;
        }

        public boolean isRemoteTile() {
            return tileType == REMOTE;
        }

        @Override
        public String toString() {
            if (isImageTile()) {
                return "ImageTile: " + imageUri;
            } else if (isCameraTile()) {
                return "CameraTile";
            } else if (isGalleryTile()) {
                return "PickerTile";
            } else if (isRemoteTile()) {
                return "RemoteTile";
            } else {
                return "Invalid item";
            }
        }

        @IntDef({IMAGE, CAMERA, GALLERY, REMOTE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface TileType {
        }

        @IntDef({CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface SpecialTileType {
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        TedSquareFrameLayout root;
        TedSquareImageView ivThumbnail;

        public GalleryViewHolder(View view) {
            super(view);
            root = (TedSquareFrameLayout) view.findViewById(R.id.root);
            ivThumbnail = (TedSquareImageView) view.findViewById(R.id.iv_thumbnail);
        }
    }
}
