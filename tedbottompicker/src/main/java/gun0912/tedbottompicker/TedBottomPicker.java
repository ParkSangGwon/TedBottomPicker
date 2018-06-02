package gun0912.tedbottompicker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedonactivityresult.TedOnActivityResult;
import com.gun0912.tedonactivityresult.listener.OnActivityResultListener;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gun0912.tedbottompicker.adapter.GalleryAdapter;
import gun0912.tedbottompicker.util.RealPathUtil;

public class TedBottomPicker extends BottomSheetDialogFragment {

    public static final String TAG = "TedBottomPicker";
    static final String EXTRA_CAMERA_IMAGE_URI = "camera_image_uri";
    static final String EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri";
    public static Builder builder;
    GalleryAdapter imageGalleryAdapter;
    View view_title_container;
    TextView tv_title;
    Button btn_done;

    FrameLayout selected_photos_container_frame;
    HorizontalScrollView hsv_selected_photos;
    LinearLayout selected_photos_container;

    TextView selected_photos_empty;
    View contentView;
    ArrayList<Uri> selectedUriList;
    ArrayList<Uri> tempUriList;
    private Uri cameraImageUri;
    private RecyclerView rc_gallery;
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {


        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Log.d(TAG, "onStateChanged() newState: " + newState);
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }


        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            Log.d(TAG, "onSlide() slideOffset: " + slideOffset);
        }
    };
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSavedInstanceState(savedInstanceState);

        //  setRetainInstance(true);
    }

    private void setupSavedInstanceState(Bundle savedInstanceState) {


        if (savedInstanceState == null) {
            cameraImageUri = builder.selectedUri;
            tempUriList = builder.selectedUriList;
        } else {
            cameraImageUri = savedInstanceState.getParcelable(EXTRA_CAMERA_IMAGE_URI);
            tempUriList = savedInstanceState.getParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI);
        }


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri);
        outState.putParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI, selectedUriList);
        super.onSaveInstanceState(outState);

    }

    public void show(FragmentManager fragmentManager) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, getTag());
        ft.commitAllowingStateLoss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onViewCreated(View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);


    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.tedbottompicker_content_view, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            if (builder != null && builder.peekHeight > 0) {
                ((BottomSheetBehavior) behavior).setPeekHeight(builder.peekHeight);
            }

        }

        initView(contentView);

        setTitle();
        setRecyclerView();
        setSelectionView();

        selectedUriList = new ArrayList<>();


        if (builder.onImageSelectedListener != null && cameraImageUri != null) {
            addUri(cameraImageUri);
        } else if (builder.onMultiImageSelectedListener != null && tempUriList != null) {
            for (Uri uri : tempUriList) {
                addUri(uri);
            }
        }

        setDoneButton();
        checkMultiMode();
    }

    private void setSelectionView() {

        if (builder.emptySelectionText != null) {
            selected_photos_empty.setText(builder.emptySelectionText);
        }


    }

    private void setDoneButton() {

        if (builder.completeButtonText != null) {
            btn_done.setText(builder.completeButtonText);
        }

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onMultiSelectComplete();


            }
        });
    }

    private void onMultiSelectComplete() {

        if (selectedUriList.size() < builder.selectMinCount) {
            String message;
            if (builder.selectMinCountErrorText != null) {
                message = builder.selectMinCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_min_count), builder.selectMinCount);
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }


        builder.onMultiImageSelectedListener.onImagesSelected(selectedUriList);
        dismissAllowingStateLoss();
    }

    private void checkMultiMode() {
        if (!isMultiSelect()) {
            btn_done.setVisibility(View.GONE);
            selected_photos_container_frame.setVisibility(View.GONE);
        }

    }

    private void initView(View contentView) {

        view_title_container = contentView.findViewById(R.id.view_title_container);
        rc_gallery = (RecyclerView) contentView.findViewById(R.id.rc_gallery);
        tv_title = (TextView) contentView.findViewById(R.id.tv_title);
        btn_done = (Button) contentView.findViewById(R.id.btn_done);

        selected_photos_container_frame = (FrameLayout) contentView.findViewById(R.id.selected_photos_container_frame);
        hsv_selected_photos = (HorizontalScrollView) contentView.findViewById(R.id.hsv_selected_photos);
        selected_photos_container = (LinearLayout) contentView.findViewById(R.id.selected_photos_container);
        selected_photos_empty = (TextView) contentView.findViewById(R.id.selected_photos_empty);
    }

    private void setRecyclerView() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rc_gallery.setLayoutManager(gridLayoutManager);
        rc_gallery.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), builder.spacing, builder.includeEdgeSpacing));
        updateAdapter();
    }

    private void updateAdapter() {

        imageGalleryAdapter = new GalleryAdapter(
                getActivity()
                , builder);
        rc_gallery.setAdapter(imageGalleryAdapter);
        imageGalleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                GalleryAdapter.PickerTile pickerTile = imageGalleryAdapter.getItem(position);

                switch (pickerTile.getTileType()) {
                    case GalleryAdapter.PickerTile.CAMERA:
                        startCameraIntent();
                        break;
                    case GalleryAdapter.PickerTile.GALLERY:
                        startGalleryIntent();
                        break;
                    case GalleryAdapter.PickerTile.IMAGE:
                        complete(pickerTile.getImageUri());

                        break;

                    default:
                        errorMessage();
                }

            }
        });
    }

    private void complete(final Uri uri) {
        Log.d(TAG, "selected uri: " + uri.toString());
        //uri = Uri.parse(uri.toString());
        if (isMultiSelect()) {


            if (selectedUriList.contains(uri)) {
                removeImage(uri);
            } else {
                addUri(uri);
            }


        } else {
            builder.onImageSelectedListener.onImageSelected(uri);
            dismissAllowingStateLoss();
        }

    }

    private boolean addUri(final Uri uri) {


        if (selectedUriList.size() == builder.selectMaxCount) {
            String message;
            if (builder.selectMaxCountErrorText != null) {
                message = builder.selectMaxCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_max_count), builder.selectMaxCount);
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return false;
        }


        selectedUriList.add(uri);

        final View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.tedbottompicker_selected_item, null);
        ImageView thumbnail = (ImageView) rootView.findViewById(R.id.selected_photo);
        ImageView iv_close = (ImageView) rootView.findViewById(R.id.iv_close);
        rootView.setTag(uri);

        selected_photos_container.addView(rootView, 0);


        int px = (int) getResources().getDimension(R.dimen.tedbottompicker_selected_image_height);
        thumbnail.setLayoutParams(new FrameLayout.LayoutParams(px, px));

        if (builder.imageProvider == null) {
            Glide.with(getActivity())
                    .load(uri)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_gallery)
                            .error(R.drawable.img_error))
                    .into(thumbnail);
        } else {
            builder.imageProvider.onProvideImage(thumbnail, uri);
        }


        if (builder.deSelectIconDrawable != null) {
            iv_close.setImageDrawable(builder.deSelectIconDrawable);
        }

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeImage(uri);

            }
        });


        updateSelectedView();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri);
        return true;

    }

    private void removeImage(Uri uri) {

        selectedUriList.remove(uri);


        for (int i = 0; i < selected_photos_container.getChildCount(); i++) {
            View childView = selected_photos_container.getChildAt(i);


            if (childView.getTag().equals(uri)) {
                selected_photos_container.removeViewAt(i);
                break;
            }
        }

        updateSelectedView();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri);
    }

    private void updateSelectedView() {

        if (selectedUriList == null || selectedUriList.size() == 0) {
            selected_photos_empty.setVisibility(View.VISIBLE);
            selected_photos_container.setVisibility(View.GONE);
        } else {
            selected_photos_empty.setVisibility(View.GONE);
            selected_photos_container.setVisibility(View.VISIBLE);
        }

    }

    private void startCameraIntent() {
        Intent cameraInent;
        File mediaFile;

        if (builder.mediaType == Builder.MediaType.IMAGE) {
            cameraInent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mediaFile = getImageFile();
        } else {
            cameraInent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            mediaFile = getVideoFile();
        }

        if (cameraInent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Application do not have Camera Application");
            return;
        }


        Uri photoURI = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", mediaFile);

        List<ResolveInfo> resolvedIntentActivities = getContext().getPackageManager().queryIntentActivities(cameraInent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            getContext().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        cameraInent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        TedOnActivityResult.with(getActivity())
                .setIntent(cameraInent)
                .setListener(new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                            onActivityResultCamera(cameraImageUri);
                        }
                    }
                })
                .startActivityForResult();
    }

    private File getImageFile() {
        // Create an image file name
        File imageFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            if (!storageDir.exists())
                storageDir.mkdirs();

            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );


            // Save a file: path for use with ACTION_VIEW intents
            cameraImageUri = Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage("Could not create imageFile for camera");
        }


        return imageFile;
    }

    private File getVideoFile() {
        // Create an image file name
        File videoFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "VIDEO_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

            if (!storageDir.exists())
                storageDir.mkdirs();

            videoFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".mp4",         /* suffix */
                    storageDir      /* directory */
            );


            // Save a file: path for use with ACTION_VIEW intents
            cameraImageUri = Uri.fromFile(videoFile);
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage("Could not create imageFile for camera");
        }


        return videoFile;
    }

    private void errorMessage(String message) {
        String errorMessage = message == null ? "Something wrong." : message;

        if (builder.onErrorListener == null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            builder.onErrorListener.onError(errorMessage);
        }
    }

    private void startGalleryIntent() {
        Intent galleryIntent;
        Uri uri;
        if (builder.mediaType == Builder.MediaType.IMAGE) {
            galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
        } else {
            galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("video/*");

        }

        if (galleryIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Application do not have Gallery Application");
            return;
        }

        TedOnActivityResult.with(getActivity())
                .setIntent(galleryIntent)
                .setListener(new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                            onActivityResultGallery(data);
                        }
                    }
                })
                .startActivityForResult();
    }

    private void errorMessage() {
        errorMessage(null);
    }

    private void setTitle() {

        if (!builder.showTitle) {
            tv_title.setVisibility(View.GONE);

            if (!isMultiSelect()) {
                view_title_container.setVisibility(View.GONE);
            }

            return;
        }

        if (!TextUtils.isEmpty(builder.title)) {
            tv_title.setText(builder.title);
        }

        if (builder.titleBackgroundResId > 0) {
            tv_title.setBackgroundResource(builder.titleBackgroundResId);
        }

    }

    private boolean isMultiSelect() {
        return builder.onMultiImageSelectedListener != null;
    }

    private void onActivityResultCamera(final Uri cameraImageUri) {

        MediaScannerConnection.scanFile(getContext(), new String[]{cameraImageUri.getPath()}, new String[]{"image/jpeg"}, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {

            }

            @Override
            public void onScanCompleted(String s, Uri uri) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter();
                        complete(cameraImageUri);
                    }
                });

            }
        });
    }


    private void onActivityResultGallery(Intent data) {
        Uri temp = data.getData();

        if (temp == null) {
            errorMessage();
        }

        String realPath = RealPathUtil.getRealPath(getActivity(), temp);

        Uri selectedImageUri = null;
        try {
            selectedImageUri = Uri.fromFile(new File(realPath));
        } catch (Exception ex) {
            selectedImageUri = Uri.parse(realPath);
        }

        complete(selectedImageUri);

    }


    public interface OnMultiImageSelectedListener {
        void onImagesSelected(ArrayList<Uri> uriList);
    }

    public interface OnImageSelectedListener {
        void onImageSelected(Uri uri);
    }

    public interface OnErrorListener {
        void onError(String message);
    }

    public interface ImageProvider {
        void onProvideImage(ImageView imageView, Uri imageUri);
    }

    public static class Builder {

        public Context context;
        public int previewMaxCount = 25;
        public Drawable cameraTileDrawable;
        public Drawable galleryTileDrawable;

        public Drawable deSelectIconDrawable;
        public Drawable selectedForegroundDrawable;

        public int spacing = 1;
        public boolean includeEdgeSpacing = false;
        public OnImageSelectedListener onImageSelectedListener;
        public OnMultiImageSelectedListener onMultiImageSelectedListener;
        public OnErrorListener onErrorListener;
        public ImageProvider imageProvider;
        public boolean showCamera = true;
        public boolean showGallery = true;
        public int peekHeight = -1;
        public int cameraTileBackgroundResId = R.color.tedbottompicker_camera;
        public int galleryTileBackgroundResId = R.color.tedbottompicker_gallery;

        public String title;
        public boolean showTitle = true;
        public int titleBackgroundResId;

        public int selectMaxCount = Integer.MAX_VALUE;
        public int selectMinCount = 0;


        public String completeButtonText;
        public String emptySelectionText;
        public String selectMaxCountErrorText;
        public String selectMinCountErrorText;
        public @MediaType
        int mediaType = MediaType.IMAGE;
        ArrayList<Uri> selectedUriList;
        Uri selectedUri;

        public Builder(@NonNull Context context) {

            this.context = context;

            setCameraTile(R.drawable.ic_camera);
            setGalleryTile(R.drawable.ic_gallery);
            setSpacingResId(R.dimen.tedbottompicker_grid_layout_margin);
        }

        public Builder setCameraTile(@DrawableRes int cameraTileResId) {
            setCameraTile(ContextCompat.getDrawable(context, cameraTileResId));
            return this;
        }

        public Builder setGalleryTile(@DrawableRes int galleryTileResId) {
            setGalleryTile(ContextCompat.getDrawable(context, galleryTileResId));
            return this;
        }

        public Builder setSpacingResId(@DimenRes int dimenResId) {
            this.spacing = context.getResources().getDimensionPixelSize(dimenResId);
            return this;
        }

        public Builder setCameraTile(Drawable cameraTileDrawable) {
            this.cameraTileDrawable = cameraTileDrawable;
            return this;
        }

        public Builder setGalleryTile(Drawable galleryTileDrawable) {
            this.galleryTileDrawable = galleryTileDrawable;
            return this;
        }

        public Builder setDeSelectIcon(@DrawableRes int deSelectIconResId) {
            setDeSelectIcon(ContextCompat.getDrawable(context, deSelectIconResId));
            return this;
        }

        public Builder setDeSelectIcon(Drawable deSelectIconDrawable) {
            this.deSelectIconDrawable = deSelectIconDrawable;
            return this;
        }

        public Builder setSelectedForeground(@DrawableRes int selectedForegroundResId) {
            setSelectedForeground(ContextCompat.getDrawable(context, selectedForegroundResId));
            return this;
        }

        public Builder setSelectedForeground(Drawable selectedForegroundDrawable) {
            this.selectedForegroundDrawable = selectedForegroundDrawable;
            return this;
        }

        public Builder setPreviewMaxCount(int previewMaxCount) {
            this.previewMaxCount = previewMaxCount;
            return this;
        }

        public Builder setSelectMaxCount(int selectMaxCount) {
            this.selectMaxCount = selectMaxCount;
            return this;
        }

        public Builder setSelectMinCount(int selectMinCount) {
            this.selectMinCount = selectMinCount;
            return this;
        }

        public Builder setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
            return this;
        }

        public Builder setOnMultiImageSelectedListener(OnMultiImageSelectedListener onMultiImageSelectedListener) {
            this.onMultiImageSelectedListener = onMultiImageSelectedListener;
            return this;
        }

        public Builder setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
            return this;
        }

        public Builder showCameraTile(boolean showCamera) {
            this.showCamera = showCamera;
            return this;
        }

        public Builder showGalleryTile(boolean showGallery) {
            this.showGallery = showGallery;
            return this;
        }

        public Builder setSpacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder setIncludeEdgeSpacing(boolean includeEdgeSpacing){
            this.includeEdgeSpacing = includeEdgeSpacing;
            return this;
        }

        public Builder setPeekHeight(int peekHeight) {
            this.peekHeight = peekHeight;
            return this;
        }

        public Builder setPeekHeightResId(@DimenRes int dimenResId) {
            this.peekHeight = context.getResources().getDimensionPixelSize(dimenResId);
            return this;
        }

        public Builder setCameraTileBackgroundResId(@ColorRes int colorResId) {
            this.cameraTileBackgroundResId = colorResId;
            return this;
        }

        public Builder setGalleryTileBackgroundResId(@ColorRes int colorResId) {
            this.galleryTileBackgroundResId = colorResId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(@StringRes int stringResId) {
            this.title = context.getResources().getString(stringResId);
            return this;
        }

        public Builder showTitle(boolean showTitle) {
            this.showTitle = showTitle;
            return this;
        }

        public Builder setCompleteButtonText(String completeButtonText) {
            this.completeButtonText = completeButtonText;
            return this;
        }

        public Builder setCompleteButtonText(@StringRes int completeButtonResId) {
            this.completeButtonText = context.getResources().getString(completeButtonResId);
            return this;
        }

        public Builder setEmptySelectionText(String emptySelectionText) {
            this.emptySelectionText = emptySelectionText;
            return this;
        }

        public Builder setEmptySelectionText(@StringRes int emptySelectionResId) {
            this.emptySelectionText = context.getResources().getString(emptySelectionResId);
            return this;
        }

        public Builder setSelectMaxCountErrorText(String selectMaxCountErrorText) {
            this.selectMaxCountErrorText = selectMaxCountErrorText;
            return this;
        }

        public Builder setSelectMaxCountErrorText(@StringRes int selectMaxCountErrorResId) {
            this.selectMaxCountErrorText = context.getResources().getString(selectMaxCountErrorResId);
            return this;
        }

        public Builder setSelectMinCountErrorText(String selectMinCountErrorText) {
            this.selectMinCountErrorText = selectMinCountErrorText;
            return this;
        }

        public Builder setSelectMinCountErrorText(@StringRes int selectMinCountErrorResId) {
            this.selectMinCountErrorText = context.getResources().getString(selectMinCountErrorResId);
            return this;
        }

        public Builder setTitleBackgroundResId(@ColorRes int colorResId) {
            this.titleBackgroundResId = colorResId;
            return this;
        }

        public Builder setImageProvider(ImageProvider imageProvider) {
            this.imageProvider = imageProvider;
            return this;
        }

        public Builder setSelectedUriList(ArrayList<Uri> selectedUriList) {
            this.selectedUriList = selectedUriList;
            return this;
        }

        public Builder setSelectedUri(Uri selectedUri) {
            this.selectedUri = selectedUri;
            return this;
        }

        public Builder showVideoMedia() {
            this.mediaType = MediaType.VIDEO;
            return this;
        }

        public TedBottomPicker create() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?");
            }

            if (onImageSelectedListener == null && onMultiImageSelectedListener == null) {
                throw new RuntimeException("You have to use setOnImageSelectedListener() or setOnMultiImageSelectedListener() for receive selected Uri");
            }

            TedBottomPicker customBottomSheetDialogFragment = new TedBottomPicker();

            customBottomSheetDialogFragment.builder = this;
            return customBottomSheetDialogFragment;
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({MediaType.IMAGE, MediaType.VIDEO})
        public @interface MediaType {
            int IMAGE = 1;
            int VIDEO = 2;
        }


    }


}
