package gun0912.tedbottompicker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import java.util.List;

/**
 * Created by broto on 9/28/16.
 */

public class Builder {

    public Context context;
    public int maxCount = 25;
    public Drawable cameraTileDrawable;
    public Drawable galleryTileDrawable;

    public int spacing = 1;
    public TedBottomPicker.OnImageSelectedListener onImageSelectedListener;
    public TedBottomPicker.OnErrorListener onErrorListener;
    public TedBottomPicker.ImageProvider imageProvider;
    public boolean showCamera = true;
    public boolean showGallery = true;
    public int peekHeight = -1;
    public int cameraTileBackgroundResId = R.color.tedbottompicker_camera;
    public int galleryTileBackgroundResId = R.color.tedbottompicker_gallery;

    public String title;
    public boolean showTitle = true;
    public int titleBackgroundResId;

    public List<String> remoteImages;

    public Builder(@NonNull Context context) {
        this.context = context;

        setCameraTile(R.drawable.ic_camera);
        setGalleryTile(R.drawable.ic_gallery);
        setSpacingResId(R.dimen.tedbottompicker_grid_layout_margin);
    }

    public Builder setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public Builder setRemoteImages(List<String> remoteImages) {
        this.remoteImages = remoteImages;
        return this;
    }

    public Builder setOnImageSelectedListener(TedBottomPicker.OnImageSelectedListener onImageSelectedListener) {
        this.onImageSelectedListener = onImageSelectedListener;
        return this;
    }

    public Builder setOnErrorListener(TedBottomPicker.OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public Builder showCameraTile(boolean showCamera) {
        this.showCamera = showCamera;
        return this;
    }

    public Builder setCameraTile(@DrawableRes int cameraTileResId) {
        setCameraTile(ContextCompat.getDrawable(context, cameraTileResId));
        return this;
    }

    public Builder setCameraTile(Drawable cameraTileDrawable) {
        this.cameraTileDrawable = cameraTileDrawable;
        return this;
    }

    public Builder showGalleryTile(boolean showGallery) {
        this.showGallery = showGallery;
        return this;
    }

    public Builder setGalleryTile(@DrawableRes int galleryTileResId) {
        setGalleryTile(ContextCompat.getDrawable(context, galleryTileResId));
        return this;
    }

    public Builder setGalleryTile(Drawable galleryTileDrawable) {
        this.galleryTileDrawable = galleryTileDrawable;
        return this;
    }

    public Builder setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public Builder setSpacingResId(@DimenRes int dimenResId) {
        this.spacing = context.getResources().getDimensionPixelSize(dimenResId);
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

    public Builder setTitleBackgroundResId(@ColorRes int colorResId) {
        this.titleBackgroundResId = colorResId;
        return this;
    }

    public Builder setImageProvider(TedBottomPicker.ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
        return this;
    }

    public TedBottomPicker create() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?");
        }

        if (onImageSelectedListener == null) {
            throw new RuntimeException("You have to setOnImageSelectedListener() for receive selected Uri");
        }

        TedBottomPicker customBottomSheetDialogFragment = new TedBottomPicker();

        customBottomSheetDialogFragment.builder = this;
        return customBottomSheetDialogFragment;
    }
}