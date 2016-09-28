package gun0912.tedbottompicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.GallerySheetView;
import gun0912.tedbottompicker.GalleryViewHolder;
import gun0912.tedbottompicker.PickerTile;
import gun0912.tedbottompicker.R;
import gun0912.tedbottompicker.TileType;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class ImageGalleryAdapter extends SelectableAdapter<GalleryViewHolder> {

    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private Context context;
    private GallerySheetView gallerySheetView;
    private ArrayList<PickerTile> pickerTiles;

    public ImageGalleryAdapter(Context context, GallerySheetView gallerySheetView) {
        this.context = context;
        this.gallerySheetView = gallerySheetView;

        setupGalleryCameraTiles();

        if (gallerySheetView.getRemoteImages() != null) {
            setupRemoteImages(gallerySheetView.getRemoteImages());
        } else {
            setupLocalImages();
        }
    }

    private void setupGalleryCameraTiles() {
        pickerTiles = new ArrayList<>();

        if (gallerySheetView.showCamera) {
            pickerTiles.add(new PickerTile(TileType.CAMERA));
        }

        if (gallerySheetView.showGallery) {
            pickerTiles.add(new PickerTile(TileType.GALLERY));
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
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {
        PickerTile pickerTile = getItem(position);

        switch (pickerTile.getTileType()) {
            case GALLERY:
                holder.ivThumbnail.setBackgroundResource(gallerySheetView.backgroundGallery);
                holder.ivThumbnail.setImageDrawable(gallerySheetView.iconGallery);

                break;
            case CAMERA:
                holder.ivThumbnail.setBackgroundResource(gallerySheetView.backgroundCamera);
                holder.ivThumbnail.setImageDrawable(gallerySheetView.iconCamera);

                break;
            case IMAGE:
                gallerySheetView.imageProvider.onProvideImage(holder.ivThumbnail, pickerTile.getImageUri());

                break;
            case REMOTE:
                gallerySheetView.imageProvider.onProvideImage(holder.ivThumbnail, pickerTile.getImageUri().toString());

                break;
            default:
                break;
        }

        if (gallerySheetView.multiSelection)
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
                while (imageCursor.moveToNext() && count < gallerySheetView.maxCount) {
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
            pickerTiles.add(new PickerTile(Uri.parse(remoteImage), TileType.REMOTE));
        }
    }

    public PickerTile getItem(int position) {
        return pickerTiles.get(position);
    }

}
