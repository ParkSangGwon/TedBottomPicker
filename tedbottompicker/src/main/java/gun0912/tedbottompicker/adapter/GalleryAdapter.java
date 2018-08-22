package gun0912.tedbottompicker.adapter;

import android.content.Context;
import android.content.CursorLoader;
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
import gun0912.tedbottompicker.R;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.entity.MediaPickerEntity;
import gun0912.tedbottompicker.util.TypeUtil;
import gun0912.tedbottompicker.view.TedSquareFrameLayout;
import gun0912.tedbottompicker.view.TedSquareImageView;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

  ArrayList<PickerTile> pickerTiles;
  Context context;
  TedBottomPicker.Builder builder;
  OnItemClickListener onItemClickListener;
  ArrayList<Uri> selectedUriList;
  private TYPE type;

  private enum TYPE {
    VIDEO_CAPTURE,
    VIDEO,
    IMAGE,
    CAMERA,
    GALLERY,
    OTHER
  }

  public GalleryAdapter(Context context, TedBottomPicker.Builder builder) {

    this.context = context;
    this.builder = builder;

    pickerTiles = new ArrayList<>();
    selectedUriList = new ArrayList<>();

    if (builder.showCamera) {
      pickerTiles.add(new PickerTile(PickerTile.CAMERA));
    }

    if (builder.showVideoCapture) {
      pickerTiles.add(new PickerTile(PickerTile.VIDEO_CAPTURE));
    }

    if (builder.showGallery) {
      pickerTiles.add(new PickerTile(PickerTile.GALLERY));
    }

    Cursor cursor = null;
    try {
      String[] projection = {
          MediaStore.Files.FileColumns._ID,
          MediaStore.Files.FileColumns.DATA,
          MediaStore.Files.FileColumns.DATE_ADDED,
          MediaStore.Files.FileColumns.MEDIA_TYPE,
          MediaStore.Files.FileColumns.MIME_TYPE,
          MediaStore.Files.FileColumns.TITLE
      };

      // Return only video and image metadata.
      String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
          + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
          + " OR "
          + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
          + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

      Uri queryUri = MediaStore.Files.getContentUri("external");

      CursorLoader cursorLoader = new CursorLoader(
          context,
          queryUri,
          projection,
          selection,
          null, // Selection args (none).
          MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
      );

      cursor = cursorLoader.loadInBackground();

      if (cursor != null) {

        int count = 0;
        while (cursor.moveToNext() && count < builder.previewMaxCount) {

          String dataIndex;
          if (builder.mediaType == TedBottomPicker.Builder.MediaType.IMAGE) {
            dataIndex = MediaStore.Images.Media.DATA;
          } else {
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

  public void setSelectedUriList(ArrayList<MediaPickerEntity> selectedUriList, Uri uri) {
    ArrayList<Uri> selectedUris = new ArrayList<>();
    for (MediaPickerEntity mediaPickerEntity : selectedUriList) {
      selectedUris.add(mediaPickerEntity.getUri());
    }
    this.selectedUriList = selectedUris;

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

    View view;
    if (viewType == TYPE.VIDEO.ordinal()) {
      view = View.inflate(context, R.layout.video_grid_item, null);
    } else {
      view = View.inflate(context, R.layout.tedbottompicker_grid_item, null);
    }

    return new GalleryViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final GalleryViewHolder holder, final int position) {

    PickerTile pickerTile = getItem(position);

    boolean isSelected = false;

    if (pickerTile.isCameraTile()) {
      holder.iv_thumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
      holder.iv_thumbnail.setImageDrawable(builder.cameraTileDrawable);
    } else if (pickerTile.isVideoCaptureTile()) {
      holder.iv_thumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
      holder.iv_thumbnail.setImageDrawable(builder.captureVideoTileDrawable);
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

  @Override public int getItemViewType(int position) {
    PickerTile pickerTile = pickerTiles.get(position);
    if (pickerTile.isVideoCaptureTile()) {
      return TYPE.VIDEO_CAPTURE.ordinal();
    } else if (TypeUtil.isContentVideo(context, pickerTile.getImageUri())) {
      return TYPE.VIDEO.ordinal();
    } else if (pickerTile.isImageTile()) {
      return TYPE.IMAGE.ordinal();
    } else if (pickerTile.isGalleryTile()) {
      return TYPE.GALLERY.ordinal();
    } else if (pickerTile.isCameraTile()) {
      return TYPE.CAMERA.ordinal();
    }

    return TYPE.OTHER.ordinal();
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
    public static final int VIDEO_CAPTURE = 3;
    public static final int GALLERY = 4;
    protected final Uri imageUri;
    protected final
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
      } else if (isVideoCaptureTile()) {
        return "VideoTile";
      } else if (isGalleryTile()) {
        return "PickerTile";
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

    public boolean isVideoCaptureTile() {
      return tileType == VIDEO_CAPTURE;
    }

    @IntDef({IMAGE, CAMERA, GALLERY, VIDEO_CAPTURE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TileType {
    }

    @IntDef({CAMERA, GALLERY, VIDEO_CAPTURE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpecialTileType {
    }
  }

  class GalleryViewHolder extends RecyclerView.ViewHolder {

    TedSquareFrameLayout root;

    TedSquareImageView iv_thumbnail;

    public GalleryViewHolder(View view) {
      super(view);
      root = view.findViewById(R.id.root);
      iv_thumbnail = view.findViewById(R.id.iv_thumbnail);
    }
  }
}
