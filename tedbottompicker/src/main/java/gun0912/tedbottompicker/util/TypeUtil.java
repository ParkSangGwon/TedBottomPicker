package gun0912.tedbottompicker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public class TypeUtil {

  public static boolean isContentVideo(Context context, Uri uri) {
    String mimeType = null;
    if (uri != null) {
      if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
        ContentResolver cr = context.getContentResolver();
        mimeType = cr.getType(uri);
      } else {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
            .toString());
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.toLowerCase());
      }
    }

    return mimeType != null && mimeType.contains("video");
  }
}
