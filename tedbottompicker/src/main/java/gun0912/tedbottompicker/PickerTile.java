package gun0912.tedbottompicker;

import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by broto on 9/28/16.
 */

public class PickerTile {

    @IntDef({IMAGE, CAMERA, GALLERY, REMOTE})
    @Retention(RetentionPolicy.SOURCE)
    @interface TileType {
    }

    @IntDef({CAMERA, GALLERY})
    @Retention(RetentionPolicy.SOURCE)
    @interface SpecialTileType {
    }

    public static final int IMAGE = 1;
    public static final int CAMERA = 2;
    public static final int GALLERY = 3;
    public static final int REMOTE = 4;

    final Uri imageUri;
    @TileType
    final int tileType;

    public PickerTile(@SpecialTileType int tileType) {
        this(null, tileType);
    }

    public PickerTile(@NonNull Uri imageUri) {
        this(imageUri, IMAGE);
    }

    public PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
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
}
