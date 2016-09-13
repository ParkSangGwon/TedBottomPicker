package gun0912.tedbottompicker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import gun0912.tedbottompicker.R;


/**
 * Created by Gil on 09/06/2014.
 */
public class TedSquareImageView extends ImageView {

    String fit_mode;
    private Drawable foreground;

    public TedSquareImageView(Context context) {
        super(context);
    }

    public TedSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TedBottomPickerImageView,
                0, 0);

        Drawable foreground = a.getDrawable(R.styleable.TedBottomPickerImageView_foreground);
        if (foreground != null) {
            setForeground(foreground);
        }


        try {
            fit_mode = a.getString(R.styleable.TedBottomPickerImageView_fit_mode);

        } finally {
            a.recycle();
        }
    }


    //Squares the thumbnail
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        if ("height".equals(fit_mode)) {
            setMeasuredDimension(heightMeasureSpec, heightMeasureSpec);

        } else {
            setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);

        }


        if (foreground != null) {
            foreground.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            invalidate();
        }


    }


    /**
     * Supply a Drawable that is to be rendered on top of all of the child views
     * in the frame layout.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    public void setForeground(Drawable drawable) {
        if (foreground == drawable) {
            return;
        }
        if (foreground != null) {
            foreground.setCallback(null);
            unscheduleDrawable(foreground);
        }

        foreground = drawable;

        if (drawable != null) {
            drawable.setCallback(this);
            if (drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
        }
        requestLayout();
        invalidate();
    }


    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == foreground;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (foreground != null)
            foreground.jumpToCurrentState();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (foreground != null && foreground.isStateful()) {
            foreground.setState(getDrawableState());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (foreground != null) {
            foreground.setBounds(0, 0, w, h);
            invalidate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (foreground != null) {
            foreground.draw(canvas);
        }
    }


}
