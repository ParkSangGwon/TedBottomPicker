package gun0912.tedbottompicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import gun0912.tedbottompicker.adapter.GalleryAdapter;
import gun0912.tedbottompicker.entity.MediaPickerEntity;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TedBottomPicker extends BottomSheetDialogFragment {

  public static final String TAG = "TedBottomPicker";
  static final String EXTRA_CAMERA_IMAGE_URI = "camera_image_uri";
  static final String EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri";
  static final String VIDEO_CAPTURE_STRING = "io.memfis19.annca.camera_video_file_path";
  public static Builder builder;
  GalleryAdapter imageGalleryAdapter;
  View view_title_container;
  TextView tv_title;
  AppCompatTextView btn_done;

  FrameLayout selected_photos_container_frame;
  HorizontalScrollView hsv_selected_photos;
  LinearLayout selected_photos_container;

  TextView selected_photos_empty;
  View contentView;
  ArrayList<MediaPickerEntity> selectedUriList;
  ArrayList<MediaPickerEntity> tempUriList;
  private Uri cameraImageUri;
  private RecyclerView rc_gallery;
  private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback =
      new BottomSheetBehavior.BottomSheetCallback() {

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

  private Uri photoURI;

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
    if (builder == null) {
      dismissAllowingStateLoss();
      return;
    }
    initView(contentView);

    setTitle();
    setRecyclerView();
    setSelectionView();

    selectedUriList = new ArrayList<>();

    if (builder.onImageSelectedListener != null && cameraImageUri != null) {
      addUri(new MediaPickerEntity(cameraImageUri).setType(MediaPickerEntity.MEDIA_TYPE.CAMERA));
    } else if (builder.onMultiImageSelectedListener != null && tempUriList != null) {
      for (MediaPickerEntity mediaPickerEntity : tempUriList) {
        addUri(mediaPickerEntity);
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
        message = String.format(getResources().getString(R.string.select_min_count),
            builder.selectMinCount);
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
    rc_gallery = contentView.findViewById(R.id.rc_gallery);
    tv_title = contentView.findViewById(R.id.tv_title);
    btn_done = contentView.findViewById(R.id.btn_done);

    selected_photos_container_frame =
        contentView.findViewById(R.id.selected_photos_container_frame);
    hsv_selected_photos = contentView.findViewById(R.id.hsv_selected_photos);
    selected_photos_container =
        contentView.findViewById(R.id.selected_photos_container);
    selected_photos_empty = contentView.findViewById(R.id.selected_photos_empty);
  }

  private void setRecyclerView() {

    GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), builder.spanCount);
    rc_gallery.setLayoutManager(gridLayoutManager);
    rc_gallery.addItemDecoration(
        new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), builder.spacing,
            builder.includeEdgeSpacing));
    updateAdapter();
  }

  private void updateAdapter() {

    imageGalleryAdapter = new GalleryAdapter(getActivity(), builder);
    rc_gallery.setAdapter(imageGalleryAdapter);
    imageGalleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(View view, int position) {

        GalleryAdapter.PickerTile pickerTile = imageGalleryAdapter.getItem(position);

        switch (pickerTile.getTileType()) {
          case GalleryAdapter.PickerTile.CAMERA:
            startCameraIntent(Builder.MediaType.IMAGE);
            break;
          case GalleryAdapter.PickerTile.VIDEO_CAPTURE:
            startCameraIntent(Builder.MediaType.VIDEO);
            break;
          case GalleryAdapter.PickerTile.GALLERY:
            startGalleryIntent();
            break;
          case GalleryAdapter.PickerTile.PDF:
            startPDFIntent();
            break;
          case GalleryAdapter.PickerTile.IMAGE:
            complete(new MediaPickerEntity(pickerTile.getImageUri()).setType(
                MediaPickerEntity.MEDIA_TYPE.PICKER));
            break;
          default:
            errorMessage();
        }
      }
    });
  }

  private void complete(final MediaPickerEntity mediaPickerEntity) {
    if (isMultiSelect()) {

      if (!selectedUriList.contains(mediaPickerEntity)) {
        addUri(mediaPickerEntity);
      }

    } else {
      builder.onImageSelectedListener.onImageSelected(mediaPickerEntity.getUri());
      dismissAllowingStateLoss();
    }
  }

  private boolean addUri(final MediaPickerEntity mediaPickerEntity) {

    if (selectedUriList.size() == builder.selectMaxCount) {
      String message;
      if (builder.selectMaxCountErrorText != null) {
        message = builder.selectMaxCountErrorText;
      } else {
        message = String.format(getResources().getString(R.string.select_max_count),
            builder.selectMaxCount);
      }

      Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
      return false;
    }

    selectedUriList.add(mediaPickerEntity);

    final View rootView =
        LayoutInflater.from(getActivity()).inflate(R.layout.tedbottompicker_selected_item, null);
    ImageView thumbnail = rootView.findViewById(R.id.selected_photo);
    ImageView iv_close = rootView.findViewById(R.id.iv_close);
    rootView.setTag(mediaPickerEntity.getUri());

    selected_photos_container.addView(rootView, 0);

    int px = (int) getResources().getDimension(R.dimen.tedbottompicker_selected_image_height);
    thumbnail.setLayoutParams(new FrameLayout.LayoutParams(px, px));
    if (mediaPickerEntity.getType() == MediaPickerEntity.MEDIA_TYPE.PDF) {
      thumbnail.setBackgroundResource(R.drawable.ic_pdf);
    } else if (builder.imageProvider == null) {
      Glide.with(getActivity())
          .load(mediaPickerEntity.getUri())
          .thumbnail(0.1f)
          .apply(new RequestOptions()
              .centerCrop()
              .placeholder(R.drawable.ic_gallery)
              .error(R.drawable.img_error))
          .into(thumbnail);
    } else {
      builder.imageProvider.onProvideImage(thumbnail, mediaPickerEntity.getUri());
    }

    if (builder.deSelectIconDrawable != null) {
      iv_close.setImageDrawable(builder.deSelectIconDrawable);
    }

    iv_close.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        removeImage(mediaPickerEntity);
      }
    });

    updateSelectedView();
    imageGalleryAdapter.setSelectedUriList(selectedUriList, mediaPickerEntity.getUri());
    return true;
  }

  private void removeImage(MediaPickerEntity mediaPickerEntity) {

    selectedUriList.remove(mediaPickerEntity);

    for (int i = 0; i < selected_photos_container.getChildCount(); i++) {
      View childView = selected_photos_container.getChildAt(i);

      if (childView.getTag().equals(mediaPickerEntity.getUri())) {
        selected_photos_container.removeViewAt(i);
        break;
      }
    }

    updateSelectedView();

    imageGalleryAdapter.setSelectedUriList(selectedUriList, mediaPickerEntity.getUri());
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

  @SuppressLint("MissingPermission") private void startCameraIntent(int mediaType) {
    Intent cameraInent;
    File mediaFile;

    if (mediaType == Builder.MediaType.IMAGE) {
      cameraInent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      mediaFile = getImageFile();
    } else {
      cameraInent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
      mediaFile = getVideoFile();
    }

    if (cameraInent.resolveActivity(getActivity().getPackageManager()) == null) {
      errorMessage(getString(R.string.no_camera_permission));
      return;
    }

    final Uri photoURI = FileProvider.getUriForFile(getContext(),
        getContext().getApplicationContext().getPackageName() + ".provider", mediaFile);

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
      String timeStamp =
          new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
      String imageFileName = "JPEG_" + timeStamp + "_";
      File storageDir = new File(getContext().getExternalCacheDirs()[0], "/swipestoximages");

      if (!storageDir.exists()) {
        storageDir.mkdirs();
      }

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
      String timeStamp =
          new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
      String imageFileName = "VIDEO_" + timeStamp + "_";
      File storageDir = new File(getContext().getExternalCacheDirs()[0], "/swipestoximages");

      if (!storageDir.exists()) {
        storageDir.mkdirs();
      }

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

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 102) {
      if (data != null) {
        Bundle bundle = data.getExtras();
        if (bundle != null && bundle.getString(VIDEO_CAPTURE_STRING) != null) {
          onActivityResultCamera(Uri.fromFile(
              new File(bundle.getString(VIDEO_CAPTURE_STRING))));
        }
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
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
      galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      galleryIntent.setType("image/*");
    } else {
      galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
      galleryIntent.setType("video/*");
    }
    galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    if (galleryIntent.resolveActivity(getActivity().getPackageManager()) == null) {
      errorMessage(getString(R.string.no_gallery_permission));
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

  private void startPDFIntent() {
    Intent pdfIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    pdfIntent.setType("application/pdf");
    pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    if (pdfIntent.resolveActivity(getActivity().getPackageManager()) == null) {
      errorMessage(getString(R.string.no_pdf_permission));
      return;
    }

    TedOnActivityResult.with(getActivity())
        .setIntent(pdfIntent)
        .setListener(new OnActivityResultListener() {
          @Override
          public void onActivityResult(int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
              onActivityResultPDF(data);
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
    }

    if (builder.titleColor > 0) {
      tv_title.setTextColor(getResources().getColor(builder.titleColor));
    }

    if (!TextUtils.isEmpty(builder.title)) {
      tv_title.setText(builder.title);
    }

    if (builder.titleBackgroundResId > 0) {
      tv_title.setBackgroundColor(builder.titleBackgroundResId);
    }

    if (builder.topBarBackgroundResId > 0) {
      view_title_container.setBackgroundResource(builder.topBarBackgroundResId);
    }

    if (builder.buttonColor > 0) {
      btn_done.setTextColor(getResources().getColor(builder.buttonColor));
    }

    if (builder.multipleMediaBarColor > 0) {
      hsv_selected_photos.setBackgroundResource(builder.multipleMediaBarColor);
    }

    if (builder.multipleMediaBarTextColor > 0) {
      selected_photos_empty.setTextColor(
          getResources().getColor(builder.multipleMediaBarTextColor));
    }
  }

  private boolean isMultiSelect() {
    return builder.onMultiImageSelectedListener != null;
  }

  private void onActivityResultCamera(final Uri cameraImageUri) {
    updateAdapter();
    complete(new MediaPickerEntity(cameraImageUri).setType(MediaPickerEntity.MEDIA_TYPE.CAMERA));
  }

  private void onActivityResultGallery(Intent data) {
    Uri uri = data.getData();

    if (uri == null) {
      errorMessage();
    }

    final ContentResolver resolver = Objects.requireNonNull(getContext()).getContentResolver();
    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

    complete(new MediaPickerEntity(uri).setType(MediaPickerEntity.MEDIA_TYPE.GALLERY));
  }

  private void onActivityResultPDF(Intent data) {
    Uri uri = data.getData();

    if (uri == null) {
      errorMessage();
    }

    final ContentResolver resolver = Objects.requireNonNull(getContext()).getContentResolver();
    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

    complete(new MediaPickerEntity(uri).setType(MediaPickerEntity.MEDIA_TYPE.PDF));
  }

  public interface OnMultiImageSelectedListener {
    void onImagesSelected(ArrayList<MediaPickerEntity> selectedUris);
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
    public int spanCount = 3;
    public int previewMaxCount = 25;
    public Drawable cameraTileDrawable;
    public Drawable captureVideoTileDrawable;
    public Drawable galleryTileDrawable;
    public Drawable pdfTileDrawable;

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
    public boolean showVideoCapture = true;
    public boolean showVideoMedia = true;
    public boolean showPDFPicker = false;
    public int peekHeight = -1;
    public int cameraTileBackgroundResId = R.color.tedbottompicker_camera;
    public int captureVideoTileBackgroundResId = R.color.tedbottompicker_camera;
    public int galleryTileBackgroundResId = R.color.tedbottompicker_gallery;
    public int pdfTileBackgroundResId = R.color.tedbottompicker_gallery;

    public String title;
    public boolean showTitle = true;
    public int titleBackgroundResId;
    public int topBarBackgroundResId;
    private int multipleMediaBarColor;
    private @ColorRes int multipleMediaBarTextColor;
    private @ColorRes int buttonColor;
    private @ColorRes int titleColor;

    public int selectMaxCount = Integer.MAX_VALUE;
    public int selectMinCount = 0;

    public String completeButtonText;
    public String emptySelectionText;
    public String selectMaxCountErrorText;
    public String selectMinCountErrorText;
    public @MediaType
    int mediaType = MediaType.IMAGE;
    ArrayList<MediaPickerEntity> selectedUriList;
    Uri selectedUri;

    public Builder(@NonNull Context context) {

      this.context = context;

      setCameraTile(R.drawable.baseline_photo_camera_white_36);
      setGalleryTile(R.drawable.baseline_collections_white_36);
      setCaptureVideoTile(R.drawable.baseline_videocam_white_48);
      setPDFTile(R.drawable.ic_pdf);
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

    public Builder setPDFTile(@DrawableRes int pdfTileResId) {
      setPDFTile(ContextCompat.getDrawable(context, pdfTileResId));
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

    public Builder setCaptureVideoTile(Drawable captureVideoTileDrawable) {
      this.captureVideoTileDrawable = captureVideoTileDrawable;
      return this;
    }

    public void setCaptureVideoTile(@DrawableRes int captureVideoResId) {
      setCaptureVideoTile(ContextCompat.getDrawable(context, captureVideoResId));
    }

    public boolean isShowVideoMedia() {
      return showVideoMedia;
    }

    public Builder setShowVideoMedia(boolean showVideoMedia) {
      this.showVideoMedia = showVideoMedia;
      return this;
    }

    public Builder setShowVideoCapture(boolean showVideoCapture) {
      this.showVideoCapture = showVideoCapture;
      return this;
    }

    public Builder setShowPDFPicker(boolean showPDFPicker) {
      this.showPDFPicker = showPDFPicker;
      return this;
    }

    public Builder setGalleryTile(Drawable galleryTileDrawable) {
      this.galleryTileDrawable = galleryTileDrawable;
      return this;
    }

    public Builder setPDFTile(Drawable pdfTileDrawable) {
      this.pdfTileDrawable = pdfTileDrawable;
      return this;
    }

    public Builder setDeSelectIcon(@DrawableRes int deSelectIconResId) {
      setDeSelectIcon(ContextCompat.getDrawable(context, deSelectIconResId));
      return this;
    }

    public Builder setSpanCount(int spanCount) {
      this.spanCount = spanCount;
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

    public Builder setOnMultiImageSelectedListener(
        OnMultiImageSelectedListener onMultiImageSelectedListener) {
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

    public Builder setIncludeEdgeSpacing(boolean includeEdgeSpacing) {
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

    public Builder setCaptureVideoTileBackgroundResId(int captureVideoTileBackgroundResId) {
      this.captureVideoTileBackgroundResId = captureVideoTileBackgroundResId;
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

    public Builder setTopBarBackgroundResId(int topBarBackgroundResId) {
      this.topBarBackgroundResId = topBarBackgroundResId;
      return this;
    }

    public Builder setButtonTextColor(int buttonColor) {
      this.buttonColor = buttonColor;
      return this;
    }

    public Builder setMultipleMediaBarColor(int multipleMediaBarColor) {
      this.multipleMediaBarColor = multipleMediaBarColor;
      return this;
    }

    public Builder setMultipleMediaBarTextColor(int multipleMediaBarTextColor) {
      this.multipleMediaBarTextColor = multipleMediaBarTextColor;
      return this;
    }

    public Builder setTitleColor(int titleColor) {
      this.titleColor = titleColor;
      return this;
    }

    public Builder setImageProvider(ImageProvider imageProvider) {
      this.imageProvider = imageProvider;
      return this;
    }

    public Builder setSelectedUriList(ArrayList<MediaPickerEntity> selectedUriList) {
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
          && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        throw new RuntimeException(
            "Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?");
      }

      if (onImageSelectedListener == null && onMultiImageSelectedListener == null) {
        throw new RuntimeException(
            "You have to use setOnImageSelectedListener() or setOnMultiImageSelectedListener() for receive selected Uri");
      }

      TedBottomPicker customBottomSheetDialogFragment = new TedBottomPicker();

      customBottomSheetDialogFragment.builder = this;
      return customBottomSheetDialogFragment;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MediaType.IMAGE, MediaType.VIDEO, MediaType.PDF})
    public @interface MediaType {
      int IMAGE = 1;
      int VIDEO = 2;
      int PDF = 3;
    }
  }
}
