package gun0912.tedbottompicker;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gun0912.tedbottompicker.adapter.ImageGalleryAdapter;

import static gun0912.tedbottompicker.TedBottomPicker.REQ_CODE_CAMERA;
import static gun0912.tedbottompicker.TedBottomPicker.REQ_CODE_GALLERY;

/**
 * Created by broto on 9/28/16.
 */

public class GallerySheetView extends NestedScrollView {

    private static final int DEFAULT_CAMERA_BACKGROUND = Color.GRAY;
    private static final int DEFAULT_GALLERY_BACKGROUND = Color.BLACK;
    private static final int DEFAULT_MAX_COUNT = 25;
    private static final float DEFAULT_PEEK_HEIGHT = -1;
    private static final float DEFAULT_SPACING = 1;
    private static final boolean DEFAULT_MULTI_SELECTION = false;
    private static final boolean DEFAULT_SHOW_GALLERY = true;
    private static final boolean DEFAULT_SHOW_CAMERA = true;

    // Camera Tile
    public boolean showCamera;
    public Drawable iconCamera;
    public int backgroundCamera = DEFAULT_CAMERA_BACKGROUND;

    // Gallery Tile
    public boolean showGallery;
    public Drawable iconGallery;
    public int backgroundGallery = DEFAULT_GALLERY_BACKGROUND;

    // Settings
    public float peekHeight;
    public float spacing;

    public int maxCount;
    public boolean multiSelection;

    private List<String> remoteImages;

    //
    private Context context;
    private RecyclerView recyclerView;
    private ImageGalleryAdapter imageGalleryAdapter;

    public ImageProvider imageProvider;

    public GallerySheetView(Context context) {
        this(context, null);
    }

    public GallerySheetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GallerySheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;
        initStyleableValues(attrs);
    }

    private void initStyleableValues(AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TedBottomPickerSquareView,
                0, 0);

        showGallery = typedArray.getBoolean(R.styleable.GallerySheetView_show_gallery, DEFAULT_SHOW_GALLERY);
        showCamera = typedArray.getBoolean(R.styleable.GallerySheetView_show_camera, DEFAULT_SHOW_CAMERA);

        iconGallery = typedArray.getDrawable(R.styleable.GallerySheetView_show_camera);
        iconCamera = typedArray.getDrawable(R.styleable.GallerySheetView_show_camera);

        backgroundGallery = typedArray.getColor(R.styleable.GallerySheetView_background_gallery,
                DEFAULT_GALLERY_BACKGROUND);
        backgroundCamera = typedArray.getColor(R.styleable.GallerySheetView_background_camera,
                DEFAULT_CAMERA_BACKGROUND);

        peekHeight = typedArray.getDimension(R.styleable.GallerySheetView_peek_height,
                DEFAULT_PEEK_HEIGHT);
        spacing = typedArray.getDimension(R.styleable.GallerySheetView_peek_height,
                DEFAULT_SPACING);
        maxCount = typedArray.getInt(R.styleable.GallerySheetView_peek_height,
                DEFAULT_MAX_COUNT);
        multiSelection = typedArray.getBoolean(R.styleable.GallerySheetView_peek_height,
                DEFAULT_MULTI_SELECTION);

        typedArray.recycle();
    }

    private void init() {
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) this.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);

        imageGalleryAdapter = new ImageGalleryAdapter(context, this);

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), (int)spacing, false));
        recyclerView.setAdapter(imageGalleryAdapter);

        imageGalleryAdapter.setOnItemClickListener(new ImageGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                PickerTile pickerTile = imageGalleryAdapter.getItem(position);

                switch (pickerTile.getTileType()) {
                    case CAMERA:
                        startCameraIntent();
                        break;
                    case GALLERY:
                        startGalleryIntent();
                        break;
                    case IMAGE:
                        complete(pickerTile.getImageUri());
                        break;
                    case REMOTE:
                        complete(pickerTile.getImageUri());
                        break;
                    default:
                        errorMessage();
                }
            }
        });
    }

    public void setRemoteImages(List<String> remoteImages) {
        this.remoteImages = remoteImages;
    }

    public List<String> getRemoteImages() {
        return remoteImages;
    }

    private void startCameraIntent() {
        Intent cameraInent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraInent.resolveActivity(context.getPackageManager()) == null) {
            errorMessage("This Application do not have Camera Application");
            return;
        }

        File imageFile = getImageFile();
        cameraInent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        context.startActivityForResult(cameraInent, REQ_CODE_CAMERA);
    }

    private void startGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(context.getPackageManager()) == null) {
            errorMessage("This Application do not have Gallery Application");
            return;
        }

        startActivityForResult(galleryIntent, REQ_CODE_GALLERY);
    }

    private File getImageFile() {
        // Create an image file name
        File imageFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

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
}
