package gun0912.tedbottompicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import gun0912.tedbottompicker.R;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.view.TedSquareFrameLayout;
import gun0912.tedbottompicker.view.TedSquareImageView;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.GalleryViewHolder> {


    ArrayList<PickerTile> pickerTiles;
    Context context;
    TedBottomPicker.Builder builder;
    OnItemClickListener onItemClickListener;
    ArrayList<Uri> selectedUriList;


    public ImageGalleryAdapter(Context context, TedBottomPicker.Builder builder) {

        this.context = context;
        this.builder = builder;

        pickerTiles = new ArrayList<>();
        selectedUriList = new ArrayList<>();

        if (builder.showCamera) {
            pickerTiles.add(new PickerTile(PickerTile.CAMERA));
        }

        if (builder.showGallery) {
            pickerTiles.add(new PickerTile(PickerTile.GALLERY));
        }

        if (builder.remoteImages == null) {
            Cursor imageCursor = null;
            try {
                final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};
                final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";


                imageCursor = context.getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
                //imageCursor = sContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);


                if (imageCursor != null) {

                    int count = 0;
                    while (imageCursor.moveToNext() && count < builder.previewMaxCount) {
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
        } else {
            for (String remoteImage : builder.remoteImages) {
                pickerTiles.add(new PickerTile(remoteImage));
            }
        }
    }

    public void setSelectedUriList(ArrayList<Uri> selectedUriList, Uri uri) {
        this.selectedUriList = selectedUriList;

        int position = -1;


        PickerTile pickerTile;
        for (int i = 0; i < pickerTiles.size(); i++) {
            pickerTile = pickerTiles.get(i);
            if (pickerTile.isImageTile() && pickerTile.getImageUri().equals(uri)) {
                position = i;
                break;
            }

        }


        if (position > 0) {
            notifyItemChanged(position);
        }


    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.tedbottompicker_grid_item, null);
        final GalleryViewHolder holder = new GalleryViewHolder(view);


        return holder;
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, final int position) {

        PickerTile pickerTile = getItem(position);


        boolean isSelected = false;

        if (pickerTile.isCameraTile()) {
            holder.iv_thumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
            holder.iv_thumbnail.setImageDrawable(builder.cameraTileDrawable);
        } else if (pickerTile.isGalleryTile()) {
            holder.iv_thumbnail.setBackgroundResource(builder.galleryTileBackgroundResId);
            holder.iv_thumbnail.setImageDrawable(builder.galleryTileDrawable);
        } else if (builder.remoteImages != null){

            String imageUrl = pickerTile.imageUrl;

            if (builder.imageProvider == null) {
                Glide.with(context)
                        .load(imageUrl)
                        .thumbnail(0.1f)
                        .dontAnimate()
                        .centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error)
                        .into(holder.iv_thumbnail);
            } else {
                builder.imageProvider.onProvideImage(holder.iv_thumbnail, imageUrl);
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
                        .into(holder.iv_thumbnail);
            } else {
                builder.imageProvider.onProvideImage(holder.iv_thumbnail, uri);
            }


            isSelected = selectedUriList.contains(uri);
        }

        if (holder.root instanceof FrameLayout) {

            Drawable foregroundDrawable;

            if(builder.selectedForegroundDrawable!=null){
                foregroundDrawable = builder.selectedForegroundDrawable;
            }else{
                foregroundDrawable = ContextCompat.getDrawable(context,R.drawable.gallery_photo_selected);
            }

            ((FrameLayout) holder.root).setForeground(isSelected ? foregroundDrawable : null);
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

    public PickerTile getItem(int position) {
        return pickerTiles.get(position);
    }

    @Override
    public int getItemCount() {
        return pickerTiles.size();
    }

    public void setOnItemClickListener(
            OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }


    public static class PickerTile {

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int GALLERY = 3;
        public static final int REMOTE = 4;

        protected Uri imageUri;
        protected String imageUrl;
        @TileType
        int tileType;

        PickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        protected PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
            this.imageUri = imageUri;
            this.tileType = tileType;
        }

        PickerTile(@NonNull Uri imageUri) {
            this(imageUri, IMAGE);
        }

        PickerTile(@NonNull String imageUrl) {
            this.imageUrl = imageUrl;
            this.tileType = REMOTE;
        }

        @Nullable
        public String getImageUrl() {
            return imageUrl;
        }

        @Nullable
        public Uri getImageUri() {
            return imageUri;
        }

        @TileType
        public int getTileType() {
            return tileType;
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
                return "RemoteTile" + imageUrl;
            } else {
                return "Invalid item";
            }
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


        TedSquareImageView iv_thumbnail;

        public GalleryViewHolder(View view) {
            super(view);
            root = (TedSquareFrameLayout) view.findViewById(R.id.root);
            iv_thumbnail = (TedSquareImageView) view.findViewById(R.id.iv_thumbnail);

        }

    }


}
