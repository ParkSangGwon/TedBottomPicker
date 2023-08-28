package gun0912.tedbottompickerdemo;

import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.entity.MediaPickerEntity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  ImageView iv_image;
  ArrayList<Uri> selectedUriList;
  Uri selectedUri;
  private ViewGroup mSelectedImagesContainer;
  private RequestManager requestManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    iv_image = (ImageView) findViewById(R.id.iv_image);
    mSelectedImagesContainer = (ViewGroup) findViewById(R.id.selected_photos_container);
    requestManager = Glide.with(this);
    setSingleShowButton();
    setMultiShowButton();
  }

  private void setSingleShowButton() {

    Button btn_single_show = (Button) findViewById(R.id.btn_single_show);
    btn_single_show.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        PermissionListener permissionlistener = new PermissionListener() {
          @Override
          public void onPermissionGranted() {

            TedBottomPicker bottomSheetDialogFragment =
                new TedBottomPicker.Builder(MainActivity.this)
                    .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                      @Override
                      public void onImageSelected(final Uri uri) {
                        Log.d("ted", "uri: " + uri);
                        Log.d("ted", "uri.getPath(): " + uri.getPath());
                        selectedUri = uri;

                        iv_image.setVisibility(View.VISIBLE);
                        mSelectedImagesContainer.setVisibility(View.GONE);

                        requestManager
                            .load(uri)
                            .into(iv_image);
                      }
                    })
                    //.setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                    .setSelectedUri(selectedUri)
                    //.showVideoMedia()
                    .setPeekHeight(1200)
                    .create();

            bottomSheetDialogFragment.show(getSupportFragmentManager());
          }

          @Override
          public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(),
                Toast.LENGTH_SHORT).show();
          }
        };

        TedPermission.with(MainActivity.this)
            .setPermissionListener(permissionlistener)
            .setDeniedMessage(
                "If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(getRequiredPermissions())
            .check();
      }
    });
  }

  private void setMultiShowButton() {

    Button btn_multi_show = (Button) findViewById(R.id.btn_multi_show);
    btn_multi_show.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        PermissionListener permissionlistener = new PermissionListener() {
          @Override
          public void onPermissionGranted() {

            TedBottomPicker bottomSheetDialogFragment =
                new TedBottomPicker.Builder(MainActivity.this)
                    .setOnMultiImageSelectedListener(
                        new TedBottomPicker.OnMultiImageSelectedListener() {
                          @Override
                          public void onImagesSelected(ArrayList<MediaPickerEntity> entities) {
                            for (MediaPickerEntity mediaPickerEntity : entities) {
                              Log.e("LOGLOG", "selected from: " + mediaPickerEntity.getType().name());
                            }
                            //selectedUriList = uriList;
                            //showUriList(uriList);
                          }
                        })
                    .setTitleColor(android.R.color.white)
                    .setTitle("TEST TEST")
                    .setSpanCount(4)
                    .setSelectMaxCount(3)
                    .setShowPDFPicker(true)
                    .setCompleteButtonText("Done")
                    .setTopBarBackgroundResId(R.color.red)
                    .setButtonTextColor(android.R.color.white)
                    .setMultipleMediaBarTextColor(android.R.color.white)
                    .setMultipleMediaBarColor(R.color.red)
                    .create();

            bottomSheetDialogFragment.show(getSupportFragmentManager());
          }

          @Override
          public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(),
                Toast.LENGTH_SHORT).show();
          }
        };

        TedPermission.with(MainActivity.this)
            .setPermissionListener(permissionlistener)
            .setDeniedMessage(
                "If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(getRequiredPermissions())
            .check();
      }
    });
  }

  private void showUriList(ArrayList<Uri> uriList) {
    // Remove all views before
    // adding the new ones.
    mSelectedImagesContainer.removeAllViews();

    iv_image.setVisibility(View.GONE);
    mSelectedImagesContainer.setVisibility(View.VISIBLE);

    int wdpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
        getResources().getDisplayMetrics());
    int htpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
        getResources().getDisplayMetrics());

    for (Uri uri : uriList) {

      View imageHolder = LayoutInflater.from(this).inflate(R.layout.image_item, null);
      ImageView thumbnail = (ImageView) imageHolder.findViewById(R.id.media_image);

      requestManager
          .load(uri.toString())
          .apply(new RequestOptions().fitCenter())
          .into(thumbnail);

      mSelectedImagesContainer.addView(imageHolder);

      thumbnail.setLayoutParams(new FrameLayout.LayoutParams(wdpx, htpx));
    }
  }

  private String[] getRequiredPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      return new String[] {
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.CAMERA,
          Manifest.permission.RECORD_AUDIO
      };
    } else {
      return new String[] {
          Manifest.permission.CAMERA,
          Manifest.permission.RECORD_AUDIO,
          Manifest.permission.READ_MEDIA_IMAGES,
          Manifest.permission.READ_MEDIA_VIDEO
      };
    }
  }
}
