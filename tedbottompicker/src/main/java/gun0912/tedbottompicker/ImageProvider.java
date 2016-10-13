package gun0912.tedbottompicker;

import android.net.Uri;
import android.widget.ImageView;

/**
 * Created by broto on 9/28/16.
 */

public interface ImageProvider {

    void onProvideImage(ImageView imageView, Uri imageUri);
    void onProvideImage(ImageView imageView, String imageUrl);
}