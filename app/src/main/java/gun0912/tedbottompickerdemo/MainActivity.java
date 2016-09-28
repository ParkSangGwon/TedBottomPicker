package gun0912.tedbottompickerdemo;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.Builder;
import gun0912.tedbottompicker.TedBottomPicker;

public class MainActivity extends AppCompatActivity {

    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivImage = (ImageView) findViewById(R.id.iv_image);
    }

    public void onLocalImagesClick(View view) {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                TedBottomPicker bottomSheetDialogFragment = new Builder(MainActivity.this)
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                Log.d("ted", "uri: " + uri);
                                Log.d("ted", "uri.getPath(): " + uri.getPath());

                                Glide.with(MainActivity.this)
                                        .load(uri)
                                        .into(ivImage);
                            }
                        })
                        .setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2)
                        .create();
                bottomSheetDialogFragment.show(getSupportFragmentManager());
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(MainActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    public void onRemoteImagesClick(View view) {
        final List<String> remoteImages = new ArrayList<>();
        remoteImages.add("http://www.techinsights.com/uploadedImages/Public_Website/Content_-_Primary/Teardowncom/Sample_Reports/sample-icon.png");
        remoteImages.add("http://www.joshuacasper.com/contents/uploads/joshua-casper-samples-free.jpg");
        remoteImages.add("http://blog.sqlauthority.com/i/b/ilovesamples.jpg");
        remoteImages.add("https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcRNpObPPjfTl8dT3IGEUn6QhipvZeU6vN6Lsdefzc3wY0OtjH3YtA");

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                TedBottomPicker bottomSheetDialogFragment = new Builder(MainActivity.this)
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                Log.d("ted", "uri: " + uri);
                                Log.d("ted", "uri.getPath(): " + uri.getPath());

                                Glide.with(MainActivity.this)
                                        .load(uri)
                                        .into(ivImage);

                            }
                        })
                        .setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2)
                        .setRemoteImages(remoteImages)
                        .create();
                bottomSheetDialogFragment.show(getSupportFragmentManager());
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(MainActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }
}
