package gun0912.tedbottompicker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import gun0912.tedbottompicker.R;

/**
 * Created by Gil on 09/06/2014.
 */
public class TedSquareFrameLayout extends FrameLayout {


    private static boolean mMatchHeightToWidth;
    private static boolean mMatchWidthToHeight;

    public TedSquareFrameLayout(Context context) {
        super(context);
    }

    public TedSquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TedBottomPickerSquareView,
                0, 0);

        try {
            mMatchHeightToWidth = a.getBoolean(R.styleable.TedBottomPickerSquareView_matchHeightToWidth, false);
            mMatchWidthToHeight = a.getBoolean(R.styleable.TedBottomPickerSquareView_matchWidthToHeight, false);
        } finally {
            a.recycle();
        }
    }



    //Squares the thumbnail
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      //  Dlog.w("start: "+widthMeasureSpec+"x"+heightMeasureSpec);
        if(mMatchHeightToWidth){
            setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
        } else if(mMatchWidthToHeight){
            setMeasuredDimension(heightMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if(mMatchHeightToWidth){
            super.onSizeChanged(w, w,oldw,oldh);
        } else if(mMatchWidthToHeight){
            super.onSizeChanged(h, h,oldw,oldh);
        }

    }
}
