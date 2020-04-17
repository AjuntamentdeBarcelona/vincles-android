package cat.bcn.vincles.mobile.UI.Compound;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cat.bcn.vincles.mobile.R;

public class DeleteNotificationCompoundView extends LinearLayout {

    ImageView imageView;
    TextView textView;

    public DeleteNotificationCompoundView(Context context) {
        super(context);
        initializeViews(context);
    }

    public DeleteNotificationCompoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public DeleteNotificationCompoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.delete_notification_layout, this);

        imageView = root.findViewById(R.id.imageview);
        textView = root.findViewById(R.id.textview);
    }

    public void setText(String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }


}
