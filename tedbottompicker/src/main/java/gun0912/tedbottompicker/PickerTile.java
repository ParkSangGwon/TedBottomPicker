package gun0912.tedbottompicker;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static gun0912.tedbottompicker.TileType.IMAGE;
import static gun0912.tedbottompicker.TileType.REMOTE;

/**
 * Created by broto on 9/28/16.
 */

public class PickerTile {

    private Uri imageUri;
    private final TileType tileType;

    public PickerTile(TileType tileType) {
        this.tileType = tileType;
    }

    public PickerTile(@NonNull Uri imageUri) {
        this(imageUri, IMAGE);
    }

    public PickerTile(@NonNull String imageUrl) {
        this(Uri.parse(imageUrl), REMOTE);
    }

    public PickerTile(@Nullable Uri imageUri, TileType tileType) {
        this.imageUri = imageUri;
        this.tileType = tileType;
    }

    @Nullable
    public Uri getImageUri() {
        return imageUri;
    }

    public TileType getTileType() {
        return tileType;
    }
}
