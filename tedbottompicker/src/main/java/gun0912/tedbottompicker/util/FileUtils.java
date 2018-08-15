package gun0912.tedbottompicker.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class FileUtils {
  private FileUtils() {
  } //private constructor to enforce Singleton pattern

  /** TAG for log messages. */
  static final String TAG = "FileUtils";
  private static final boolean DEBUG = false; // Set to true to enable logging

  public static final String MIME_TYPE_AUDIO = "audio/*";
  public static final String MIME_TYPE_TEXT = "text/*";
  public static final String MIME_TYPE_IMAGE = "image/*";
  public static final String MIME_TYPE_VIDEO = "video/*";
  public static final String MIME_TYPE_APP = "application/*";

  public static final String HIDDEN_PREFIX = ".";

  /**
   * Gets the extension of a file name, like ".png" or ".jpg".
   *
   * @return Extension including the dot("."); "" if there is no extension; null if uri was null.
   */
  public static String getExtension(String uri) {
    if (uri == null) {
      return null;
    }

    int dot = uri.lastIndexOf(".");
    if (dot >= 0) {
      return uri.substring(dot);
    } else {
      // No extension.
      return "";
    }
  }

  /**
   * @return Whether the URI is a local one.
   */
  public static boolean isLocal(String url) {
    if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
      return true;
    }
    return false;
  }

  /**
   * @return True if Uri is a MediaStore Uri.
   */
  public static boolean isMediaUri(Uri uri) {
    return "media".equalsIgnoreCase(uri.getAuthority());
  }

  /**
   * Convert File into Uri.
   *
   * @return uri
   */
  public static Uri getUri(File file) {
    if (file != null) {
      return Uri.fromFile(file);
    }
    return null;
  }

  /**
   * Returns the path only (without file name).
   */
  public static File getPathWithoutFilename(File file) {
    if (file != null) {
      if (file.isDirectory()) {
        // no file to be split off. Return everything
        return file;
      } else {
        String filename = file.getName();
        String filepath = file.getAbsolutePath();

        // Construct path without file name.
        String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
        if (pathwithoutname.endsWith("/")) {
          pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
        }
        return new File(pathwithoutname);
      }
    }
    return null;
  }

  /**
   * @return The MIME type for the given file.
   */
  public static String getMimeType(File file) {

    String extension = getExtension(file.getName());

    if (extension.length() > 0) {
      return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
    }

    return "application/octet-stream";
  }

  /**
   * @return The MIME type for the give Uri.
   */
  public static String getMimeType(Context context, Uri uri) {
    File file = new File(getPath(context, uri));
    return getMimeType(file);
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is {@link LocalStorageProvider}.
   */
  public static boolean isLocalStorageDocument(Uri uri) {
    return LocalStorageProvider.AUTHORITY.equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is ExternalStorageProvider.
   */
  public static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is DownloadsProvider.
   */
  public static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is MediaProvider.
   */
  public static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is Google Photos.
   */
  public static boolean isGooglePhotosUri(Uri uri) {
    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
  }

  /**
   * Get the value of the data column for this Uri. This is useful for
   * MediaStore Uris, and other file-based ContentProviders.
   *
   * @param context The context.
   * @param uri The Uri to query.
   * @param selection (Optional) Filter used in the query.
   * @param selectionArgs (Optional) Selection arguments used in the query.
   * @return The value of the _data column, which is typically a file path.
   * @author paulburke
   */
  public static String getDataColumn(Context context, Uri uri, String selection,
      String[] selectionArgs) {

    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = {
        column
    };

    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        if (DEBUG) DatabaseUtils.dumpCursor(cursor);

        final int column_index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(column_index);
      }
    } finally {
      if (cursor != null) cursor.close();
    }
    return null;
  }

  /**
   * Get a file path from a Uri. This will get the the path for Storage Access
   * Framework Documents, as well as the _data field for the MediaStore and
   * other file-based ContentProviders.<br>
   * <br>
   * Callers should check whether the path is local before assuming it
   * represents a local file.
   *
   * @param context The context.
   * @param uri The Uri to query.
   * @see #isLocal(String)
   * @see #getFile(Context, Uri)
   */
  @TargetApi(Build.VERSION_CODES.KITKAT) public static String getPath(final Context context,
      final Uri uri) {

    if (DEBUG) {
      Log.d(TAG + " File -", "Authority: " + uri.getAuthority() +
          ", Fragment: " + uri.getFragment() +
          ", Port: " + uri.getPort() +
          ", Query: " + uri.getQuery() +
          ", Scheme: " + uri.getScheme() +
          ", Host: " + uri.getHost() +
          ", Segments: " + uri.getPathSegments().toString());
    }

    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
      // LocalStorageProvider
      if (isLocalStorageDocument(uri)) {
        // The path is the id
        return DocumentsContract.getDocumentId(uri);
      }
      // ExternalStorageProvider
      else if (isExternalStorageDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
          return Environment.getExternalStorageDirectory() + "/" + split[1];
        }

        // TODO handle non-primary volumes
      }
      // DownloadsProvider
      else if (isDownloadsDocument(uri)) {

        final String id = DocumentsContract.getDocumentId(uri);
        final Uri contentUri =
            ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                Long.valueOf(id));

        return getDataColumn(context, contentUri, null, null);
      }
      // MediaProvider
      else if (isMediaDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[] {
            split[1]
        };

        return getDataColumn(context, contentUri, selection, selectionArgs);
      }
    }
    // MediaStore (and general)
    else if ("content".equalsIgnoreCase(uri.getScheme())) {

      // Return the remote address
      if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

      return getDataColumn(context, uri, null, null);
    }
    // File
    else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }

    return null;
  }

  /**
   * Convert Uri into File, if possible.
   *
   * @return file A local file that the Uri was pointing to, or null if the Uri is unsupported or
   * pointed to a remote resource.
   * @see #getPath(Context, Uri)
   */
  public static File getFile(Context context, Uri uri) {
    if (uri != null) {
      String path = getPath(context, uri);
      if (path != null && isLocal(path)) {
        return new File(path);
      }
    }
    return null;
  }

  /**
   * Get the file size in a human-readable string.
   */
  public static String getReadableFileSize(int size) {
    final int BYTES_IN_KILOBYTES = 1024;
    final DecimalFormat dec = new DecimalFormat("###.#");
    final String KILOBYTES = " KB";
    final String MEGABYTES = " MB";
    final String GIGABYTES = " GB";
    float fileSize = 0;
    String suffix = KILOBYTES;

    if (size > BYTES_IN_KILOBYTES) {
      fileSize = size / BYTES_IN_KILOBYTES;
      if (fileSize > BYTES_IN_KILOBYTES) {
        fileSize = fileSize / BYTES_IN_KILOBYTES;
        if (fileSize > BYTES_IN_KILOBYTES) {
          fileSize = fileSize / BYTES_IN_KILOBYTES;
          suffix = GIGABYTES;
        } else {
          suffix = MEGABYTES;
        }
      }
    }
    return String.valueOf(dec.format(fileSize) + suffix);
  }

  /**
   * Attempt to retrieve the thumbnail of given File from the MediaStore. This
   * should not be called on the UI thread.
   */
  public static Bitmap getThumbnail(Context context, File file) {
    return getThumbnail(context, getUri(file), getMimeType(file));
  }

  /**
   * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
   * should not be called on the UI thread.
   */
  public static Bitmap getThumbnail(Context context, Uri uri) {
    return getThumbnail(context, uri, getMimeType(context, uri));
  }

  /**
   * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
   * should not be called on the UI thread.
   */
  public static Bitmap getThumbnail(Context context, Uri uri, String mimeType) {

    if (!isMediaUri(uri)) {
      return null;
    }

    Bitmap bm = null;
    if (uri != null) {
      final ContentResolver resolver = context.getContentResolver();
      Cursor cursor = null;
      try {
        cursor = resolver.query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
          final int id = cursor.getInt(0);

          if (mimeType.contains("video")) {
            bm = MediaStore.Video.Thumbnails.getThumbnail(resolver, id,
                MediaStore.Video.Thumbnails.MINI_KIND, null);
          } else if (mimeType.contains(FileUtils.MIME_TYPE_IMAGE)) {
            bm = MediaStore.Images.Thumbnails.getThumbnail(resolver, id,
                MediaStore.Images.Thumbnails.MINI_KIND, null);
          }
        }
      } catch (Exception e) {
      } finally {
        if (cursor != null) cursor.close();
      }
    }
    return bm;
  }

  /**
   * File and folder comparator. TODO Expose sorting option method
   */
  public static Comparator<File> sComparator = new Comparator<File>() {
    @Override public int compare(File f1, File f2) {
      // Sort alphabetically by lower case, which is much cleaner
      return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
    }
  };

  /**
   * File (not directories) filter.
   */
  public static FileFilter sFileFilter = new FileFilter() {
    @Override public boolean accept(File file) {
      final String fileName = file.getName();
      // Return files only (not directories) and skip hidden files
      return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
    }
  };

  /**
   * Folder (directories) filter.
   */
  public static FileFilter sDirFilter = new FileFilter() {
    @Override public boolean accept(File file) {
      final String fileName = file.getName();
      // Return directories only and skip hidden directories
      return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
    }
  };

  /**
   * Get the Intent for selecting content to be used in an Intent Chooser.
   *
   * @return The intent for opening a file with Intent.createChooser()
   */
  public static Intent createGetContentIntent() {
    // Implicitly allow the user to select a particular kind of data
    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    // The MIME data type filter
    intent.setType("*/*");
    // Only return URIs that can be opened with ContentResolver
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    return intent;
  }

  public static String getUniqueFilename(String prefix) {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    return prefix + timeStamp + ".jpg";
  }

  public static File createImageFile(Context context, String prefix) throws IOException {
    // Create an image file name
    File swipestoxDir = new File(context.getExternalCacheDirs()[0], "/swipestoximages");
    if (!swipestoxDir.exists()) {
      swipestoxDir.mkdir();
    }
    File image = new File(swipestoxDir, getUniqueFilename(prefix));
    image.createNewFile();
    return image;
  }

  public static File createVideoFile(Context context) throws IOException {
    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), context.getPackageName());

    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d(TAG, "Failed to create directory.");
        return null;
      }
    }

    String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
    File mediaFile;

    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
        "VID_" + timeStamp + ".mp4");

    return mediaFile;
  }

  public static boolean checkExists(Context context, String filename) {
    File[] files = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_PICTURES);
    File file = new File(files[0], filename);
    return file.isFile();
  }

  public static String getPath(Context context, String filename) {
    File[] files = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_PICTURES);
    return new File(files[0], filename).getAbsolutePath();
  }

  public static File createImageThumbFile(Context context, String path) throws IOException {
    // Create an image file name
    String imageFileName = path + "thumb" + ".jpg";
    File[] files = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_PICTURES);
    File image = new File(files[0], imageFileName);
    image.createNewFile();
    return image;
  }

  public static File createFile(Context context, String path) throws IOException {
    // Create an image file name
    String imageFileName = path;
    File[] files = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_PICTURES);
    File image = new File(files[0], imageFileName);
    image.createNewFile();
    return image;
  }

  public static File saveFile(Context context, String path, Bitmap bitmap) throws IOException {
    // Create an image file name
    String imageFileName = path;
    File[] files = ContextCompat.getExternalCacheDirs(context);
    File image = new File(files[0], imageFileName);
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(image);
      // Use the compress method on the BitMap object to write image to the OutputStream
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      fos.close();
    }
    return image;
  }

  public static File createAudioFile(Context context) throws IOException {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String videoFileName = "AUDIO_" + timeStamp + ".mp4";
    File[] files = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_PICTURES);
    File image = new File(files[0], videoFileName);
    image.createNewFile();
    return image;
  }

  //TODO Nihad check for out of memory exception
  public static File saveAndScaleFileFromContent(Context context, Uri uri, String name)
      throws IOException {
    InputStream inputStream = context.getContentResolver().openInputStream(uri);
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
    float ratio = Math.min(
        (float) 1024 / bitmap.getWidth(),
        (float) 1024 / bitmap.getHeight());
    int width = Math.round((float) ratio * bitmap.getWidth());
    int height = Math.round((float) ratio * bitmap.getHeight());

    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width,
        height, true);
    inputStream.close();
    return saveFile(context, name, newBitmap);
  }

  public static void deleteDirectoryRecursively(File directory) {
    if (directory.isDirectory()) {
      for (File file : directory.listFiles()) {
        deleteDirectoryRecursively(file);
      }
    }
    directory.delete();
  }

  public static String copyBundledRealmFile(InputStream inputStream, String outFileName,
      Context context) {
    try {
      File file = new File(context.getFilesDir(), outFileName);
      FileOutputStream outputStream = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buf)) > 0) {
        outputStream.write(buf, 0, bytesRead);
      }
      outputStream.close();
      return file.getAbsolutePath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}