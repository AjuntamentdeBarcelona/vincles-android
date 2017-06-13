/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import cat.bcn.vincles.tablet.R;

public class MainButton extends FrameLayout {

    TextView textView_caption;
    Button button_circle;

    public MainButton(Context context) {
        super(context);
    }

    public MainButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.component_main_button, this);

        textView_caption = (TextView) findViewById(R.id.texMainComp);
        button_circle = (Button) findViewById(R.id.button_circle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MainButton);
        String caption = a.getString(R.styleable.MainButton_caption);
        String num = a.getString(R.styleable.MainButton_num);
        a.recycle();

        setCaption(caption);
        setNum(num);
    }

    public void setNum(String num) {
        button_circle.setText(num);
    }

    public void setCaption(String caption) {
        textView_caption.setText(caption);
    }
}