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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.R;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import gun0912.tedbottompicker.view.TedSquareFrameLayout;
import gun0912.tedbottompicker.view.TedSquareImageView;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {


    private ArrayList<PickerTile> pickerTiles;
    private Context context;
    private TedBottomSheetDialogFragment.BaseBuilder builder;
    private OnItemClickListener onItemClickListener;
    private List<Uri> selectedUriList;


    public GalleryAdapter(Context context, TedBottomSheetDialogFragment.BaseBuilder builder) {

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

        Cursor cursor = null;
        try {
            String[] columns;
            String orderBy;
            Uri uri;
            if (builder.mediaType == TedBottomSheetDialogFragment.BaseBuilder.MediaType.IMAGE) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{MediaStore.Images.Media.DATA};
                orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
            } else {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{MediaStore.Video.VideoColumns.DATA};
                orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC";
            }




            cursor = context.getApplicationContext().getContentResolver().query(uri, columns, null, null, orderBy);
            //imageCursor = sContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);


            if (cursor != null) {

                int count = 0;
                while (cursor.moveToNext() && count < builder.previewMaxCount) {

                    String dataIndex;
                    if (builder.mediaType == TedBottomSheetDialogFragment.BaseBuilder.MediaType.IMAGE) {
                        dataIndex = MediaStore.Images.Media.DATA;
                    }else{
                        dataIndex = MediaStore.Video.VideoColumns.DATA;
                    }
                    String imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex));
                    File imageFile = new File(imageLocation);
                    pickerTiles.add(new PickerTile(Uri.fromFile(imageFile)));
                    count++;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


    }

    public void setSelectedUriList(List<Uri> selectedUriList, @NonNull Uri uri) {
        this.selectedUriList = selectedUriList;

        int position = -1;


        PickerTile pickerTile;
        for (int i = 0; i < pickerTiles.size(); i++) {
            pickerTile = pickerTiles.get(i);
            if (pickerTile.isImageTile() && uri.equals(pickerTile.getImageUri())) {
                position = i;
                break;
            }
        }


        if (position >= 0) {
            notifyItemChanged(position);
        }


    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.tedbottompicker_grid_item, null);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, int position) {

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
                Glide.with(context)
                        .load(uri)
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().centerCrop()
                                .placeholder(R.drawable.ic_gallery)
                                .error(R.drawable.img_error))
                        .into(holder.iv_thumbnail);
            } else {
                builder.imageProvider.onProvideImage(holder.iv_thumbnail, uri);
            }


            isSelected = selectedUriList.contains(uri);


        }


        if (holder.root != null) {

            Drawable foregroundDrawable;

            if (builder.selectedForegroundDrawable != null) {
                foregroundDrawable = builder.selectedForegroundDrawable;
            } else {
                foregroundDrawable = ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected);
            }

            holder.root.setForeground(isSelected ? foregroundDrawable : null);
        }


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
        private final Uri imageUri;
        private final
        @TileType
        int tileType;

        private PickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        private PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
            this.imageUri = imageUri;
            this.tileType = tileType;
        }

        PickerTile(@NonNull Uri imageUri) {
            this(imageUri, IMAGE);
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
            } else {
                return "Invalid item";
            }
        }

        private boolean isImageTile() {
            return tileType == IMAGE;
        }

        private boolean isCameraTile() {
            return tileType == CAMERA;
        }

        private boolean isGalleryTile() {
            return tileType == GALLERY;
        }

        @IntDef({IMAGE, CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        private @interface TileType {
        }

        @IntDef({CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        private @interface SpecialTileType {
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {

        TedSquareFrameLayout root;


        TedSquareImageView iv_thumbnail;

        private GalleryViewHolder(View view) {
            super(view);
            root = view.findViewById(R.id.root);
            iv_thumbnail = view.findViewById(R.id.iv_thumbnail);

        }

    }


}
