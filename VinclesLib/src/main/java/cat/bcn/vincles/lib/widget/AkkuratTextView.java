/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import cat.bcn.vincles.lib.util.FontManager;
import cat.bcn.vincles.lib.util.VinclesConstants;

public class AkkuratTextView extends TextView {

    public AkkuratTextView(Context context) {
        super(context);
        init();
    }

    public AkkuratTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AkkuratTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        int style = Typeface.NORMAL;
        if (this.getTypeface() != null) style = this.getTypeface().getStyle();
        FontManager.setCustomFont(this, getContext(), style == Typeface.BOLD ? VinclesConstants.TYPEFACE.BOLD : VinclesConstants.TYPEFACE.REGULAR);
    }


}
