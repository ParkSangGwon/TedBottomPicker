package gun0912.tedbottompicker.entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.net.URLConnection;

public class MediaPickerEntity implements Parcelable {
  private Uri uri;
  private MEDIA_TYPE type;

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel parcel, int i) {
    parcel.writeParcelable(uri, i);
  }

  public static final Parcelable.Creator<MediaPickerEntity> CREATOR
      = new Parcelable.Creator<MediaPickerEntity>() {
    public MediaPickerEntity createFromParcel(Parcel in) {
      return new MediaPickerEntity(in);
    }

    public MediaPickerEntity[] newArray(int size) {
      return new MediaPickerEntity[size];
    }
  };

  private MediaPickerEntity(Parcel in) {
    uri = in.readParcelable(getClass().getClassLoader());
  }

  public enum MEDIA_TYPE {
    CAMERA,
    GALLERY,
    PICKER,
    PDF
  }

  public MediaPickerEntity setType(MEDIA_TYPE type) {
    this.type = type;
    return this;
  }

  public MediaPickerEntity(Uri uri) {
    this.uri = uri;
  }

  public Uri getUri() {
    return uri;
  }

  public MEDIA_TYPE getType() {
    return type;
  }

  public boolean isVideo() {
    String mimeType = URLConnection.guessContentTypeFromName(uri.getPath());
    return mimeType != null && mimeType.startsWith("video");
  }

  @Override public boolean equals(Object obj) {
    MediaPickerEntity mediaPickerEntity = (MediaPickerEntity) obj;
    return hashCode() == mediaPickerEntity.hashCode();
  }

  @Override public int hashCode() {
    if (uri != null) {
      return uri.hashCode();
    }

    return 0;
  }
}
