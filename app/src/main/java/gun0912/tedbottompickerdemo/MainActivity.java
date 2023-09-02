package gun0912.tedbottompickerdemo;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedRxBottomPicker;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private List<Uri> selectedUriList;
    private Uri selectedUri;
    private Disposable singleImageDisposable;
    private Disposable multiImageDisposable;
    private RequestManager requestManager;

    private ImageView ivImage;
    private TextView tvFilePath;
    private ViewGroup selectedImagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFilePath = findViewById(R.id.tv_path);
        ivImage = findViewById(R.id.iv_image);
        selectedImagesContainer = findViewById(R.id.selected_photos_container);
        requestManager = Glide.with(this);
        setImageAndVideoPickerButton();
        setSingleShowButton();
        setMultiShowButton();
        setRxSingleShowButton();
        setRxMultiShowButton();
    }

    private void setSingleShowButton() {
        Button btnSingleShow = findViewById(R.id.btn_single_show);
        btnSingleShow.setOnClickListener(view -> {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    TedBottomPicker.with(MainActivity.this)
                            //.setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                            .setSelectedUri(selectedUri)
                            //.showVideoMedia()
                            .setPeekHeight(1200)
                            .show(uri -> {
                                selectedUri = uri;

                                tvFilePath.setText(selectedUri.toString());
                                ivImage.setVisibility(View.VISIBLE);
                                selectedImagesContainer.setVisibility(View.GONE);

                                requestManager
                                        .load(uri)
                                        .into(ivImage);
                            });
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    showNoPermissionToast(deniedPermissions);
                }
            };

            checkPermission(permissionlistener);
        });
    }

    private void setImageAndVideoPickerButton() {
        Button btnSingleShow = findViewById(R.id.btn_single_image_video);
        btnSingleShow.setOnClickListener(view -> {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    TedBottomPicker.with(MainActivity.this)
                            .setSelectedUri(selectedUri)
                            .showImageAndVideoMedia()
                            .setPeekHeight(1200)
                            .show(uri -> {
                                selectedUri = uri;

                                tvFilePath.setText(selectedUri.toString());
                                ivImage.setVisibility(View.VISIBLE);
                                selectedImagesContainer.setVisibility(View.GONE);

                                requestManager
                                        .load(uri)
                                        .into(ivImage);
                            });
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    showNoPermissionToast(deniedPermissions);
                }
            };

            checkPermission(permissionlistener);
        });
    }

    private void showNoPermissionToast(ArrayList<String> deniedPermissions) {
        Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
    }

    private void setMultiShowButton() {
        Button btnMultiShow = findViewById(R.id.btn_multi_show);
        btnMultiShow.setOnClickListener(view -> {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    TedBottomPicker.with(MainActivity.this)
                            //.setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                            .setPeekHeight(1600)
                            .showTitle(false)
                            .setCompleteButtonText("Done")
                            .setEmptySelectionText("No Select")
                            .setSelectedUriList(selectedUriList)
                            .showMultiImage(uriList -> {
                                selectedUriList = uriList;
                                showUriList(uriList);
                            });
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    showNoPermissionToast(deniedPermissions);
                }
            };

            checkPermission(permissionlistener);
        });
    }

    private void setRxSingleShowButton() {
        Button btnSingleShow = findViewById(R.id.btn_rx_single_show);
        btnSingleShow.setOnClickListener(view -> {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    singleImageDisposable = TedRxBottomPicker.with(MainActivity.this)
                            //.setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                            .setSelectedUri(selectedUri)
                            .showVideoMedia()
                            .setPeekHeight(1200)
                            .show()
                            .subscribe(uri -> {
                                selectedUri = uri;

                                tvFilePath.setText(selectedUri.toString());
                                ivImage.setVisibility(View.VISIBLE);
                                selectedImagesContainer.setVisibility(View.GONE);

                                requestManager
                                        .load(uri)
                                        .into(ivImage);

                            }, Throwable::printStackTrace);
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    showNoPermissionToast(deniedPermissions);
                }
            };

            checkPermission(permissionlistener);
        });
    }

    private void setRxMultiShowButton() {
        Button btnRxMultiShow = findViewById(R.id.btn_rx_multi_show);
        btnRxMultiShow.setOnClickListener(view -> {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    multiImageDisposable = TedRxBottomPicker.with(MainActivity.this)
                            //.setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                            .setPeekHeight(1600)
                            .showTitle(false)
                            .setCompleteButtonText("Done")
                            .setEmptySelectionText("No Select")
                            .setSelectedUriList(selectedUriList)
                            .showMultiImage()
                            .subscribe(uris -> {
                                selectedUriList = uris;
                                showUriList(uris);
                            }, Throwable::printStackTrace);
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    showNoPermissionToast(deniedPermissions);
                }
            };

            checkPermission(permissionlistener);
        });
    }

    private void checkPermission(PermissionListener permissionlistener) {
        TedPermission.with(MainActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void showUriList(List<Uri> uriList) {
        // Remove all views before
        // adding the new ones.
        selectedImagesContainer.removeAllViews();

        ivImage.setVisibility(View.GONE);
        selectedImagesContainer.setVisibility(View.VISIBLE);

        int widthPixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        int heightPixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());


        for (Uri uri : uriList) {
            View imageHolder = LayoutInflater.from(this).inflate(R.layout.image_item, null);
            ImageView thumbnail = imageHolder.findViewById(R.id.media_image);

            requestManager
                    .load(uri.toString())
                    .apply(new RequestOptions().fitCenter())
                    .into(thumbnail);

            selectedImagesContainer.addView(imageHolder);

            thumbnail.setLayoutParams(new FrameLayout.LayoutParams(widthPixel, heightPixel));
        }
    }

    @Override
    protected void onDestroy() {
        if (singleImageDisposable != null && !singleImageDisposable.isDisposed()) {
            singleImageDisposable.dispose();
        }
        if (multiImageDisposable != null && !multiImageDisposable.isDisposed()) {
            multiImageDisposable.dispose();
        }
        super.onDestroy();
    }
}
