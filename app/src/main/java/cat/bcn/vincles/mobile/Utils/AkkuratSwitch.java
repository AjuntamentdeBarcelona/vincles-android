package cat.bcn.vincles.mobile.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Switch;

//import cat.bcn.vincles.lib.util.FontCache;
//import cat.bcn.vincles.lib.util.FontManager;
//import cat.bcn.vincles.lib.util.VinclesConstants;

public class AkkuratSwitch extends Switch {
    private final String TAG = this.getClass().getSimpleName();

    public AkkuratSwitch(Context context) {
        super(context);
        init(context);
    }

    public AkkuratSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AkkuratSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public void setSwitchTypeface(Typeface tf, int style) {
//        Typeface tf1 = FontCache.get(VinclesConstants.TYPEFACE.REGULAR, this.getContext());
//        if (tf1!= null) {
//            super.setSwitchTypeface(tf1, style);
//        }
    }

    public void init(Context context) {
        int style = Typeface.NORMAL;
        if (this.getTypeface() != null) style = this.getTypeface().getStyle();
        //FontManager.setCustomFont(this, getContext(), style == Typeface.BOLD ? VinclesConstants.TYPEFACE.BOLD : VinclesConstants.TYPEFACE.REGULAR);
    }
}
