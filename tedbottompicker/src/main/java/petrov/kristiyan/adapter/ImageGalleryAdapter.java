package petrov.kristiyan.adapter;

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
import android.widget.ImageView;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import petrov.kristiyan.R;
import petrov.kristiyan.TedBottomPicker;
import petrov.kristiyan.view.TedSquareFrameLayout;
import petrov.kristiyan.view.TedSquareImageView;

/**
 * @author Kristiyan Petrov (kristiyan@igenius.net)
 */

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.GalleryViewHolder> {


    private ArrayList<PickerTile> pickerTiles;
    private Context context;
    private TedBottomPicker.Builder builder;
    private OnItemClickListener onItemClickListener;
    private ArrayList<Uri> selectedUriList;


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
        return new GalleryViewHolder(view);
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

        } else {
            Uri uri = pickerTile.getImageUri();
            if (builder.imageProvider == null) {
                throw new RuntimeException("Must provide a custom image Provider such as Picasso/Glide/Fresco etc...");
            } else {
                builder.imageProvider.onProvideImage(holder.iv_thumbnail, uri);
            }


            isSelected = selectedUriList.contains(uri);


        }

        holder.selected_icon.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
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
        void onItemClick(View view, int position);
    }


    public static class PickerTile {

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int GALLERY = 3;
        final Uri imageUri;
        final
        @TileType
        int tileType;

        PickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        PickerTile(@NonNull Uri imageUri) {
            this(imageUri, IMAGE);
        }

        PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
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

        boolean isImageTile() {
            return tileType == IMAGE;
        }

        boolean isCameraTile() {
            return tileType == CAMERA;
        }

        boolean isGalleryTile() {
            return tileType == GALLERY;
        }

        @Override
        public String toString() {
            if (isImageTile()) {
                return "ImageTile: " + imageUri;
            } else if (isCameraTile()) {
                return "CameraTile";
            } else if (isGalleryTile()) {
                return "PickerTile";
            } else {
                return "Invalid item";
            }
        }

        @IntDef({IMAGE, CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        @interface TileType {
        }

        @IntDef({CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        @interface SpecialTileType {
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {

        TedSquareFrameLayout root;
        TedSquareImageView iv_thumbnail;
        ImageView selected_icon;

        GalleryViewHolder(View view) {
            super(view);
            root = view.findViewById(R.id.root);
            iv_thumbnail = view.findViewById(R.id.iv_thumbnail);
            selected_icon = view.findViewById(R.id.selected_icon);
        }

    }


}
