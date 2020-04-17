package cat.bcn.vincles.mobile.UI.Home;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.makeramen.roundedimageview.RoundedImageView;

public class SquareRoundedImageView extends RoundedImageView {


    public SquareRoundedImageView(Context context) {
        super(context);
    }

    public SquareRoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareRoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*@Override
    public void requestLayout() {
        forceLayout();
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMeasure(final int widthSpec, final int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;
        super.onLayout(changed, left, top, right, bottom);
        Log.d("qwe","onLayout, l:"+left+" r:"+right);

        // If textview below forces the layout to not be square, we add padding to correct for that
        if (h < w) {
            int sidePadding = (w-h)/2;
            setPadding(sidePadding, 0, sidePadding, 0);
        }
    }

}
