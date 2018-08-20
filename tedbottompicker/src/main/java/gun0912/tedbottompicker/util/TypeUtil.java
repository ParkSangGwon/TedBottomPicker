package gun0912.tedbottompicker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public class TypeUtil {

  public static boolean isContentVideo(Context context, Uri uri) {
    if (uri != null) {
      ContentResolver cr = context.getContentResolver();
      String mime = cr.getType(uri);

      return mime != null && mime.contains("video");
    }

    return false;
  }
}
