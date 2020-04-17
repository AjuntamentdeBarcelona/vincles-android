package cat.bcn.vincles.mobile.UI.Home;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ContactCompoundView extends LinearLayout {

    TextView nameTV;
    TextView notificationsTV;
    ImageView avatarIV;
    ProgressBar progressBar;

    private int notificationsNumber = 0;
    private int notificationsNumberSize = 0;

    public ContactCompoundView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ContactCompoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ContactCompoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.contact_compound_view_layout, this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        nameTV = root.findViewById(R.id.contact_name);
        avatarIV = root.findViewById(R.id.contact_avatar);
        progressBar = root.findViewById(R.id.progressbar);
        notificationsTV = root.findViewById(R.id.notifications_number);

        notificationsTV.setWidth(notificationsNumberSize/4);
        notificationsTV.setHeight(notificationsNumberSize/4);
        notificationsTV.setVisibility(notificationsNumber == 0 ? GONE : VISIBLE);

        ViewTreeObserver vto = avatarIV.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (avatarIV.getWidth() != 0) {
                    avatarIV.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int avatarTopMargin = ((RelativeLayout.LayoutParams) (findViewById(R.id.contact_avatar_frame)).getLayoutParams()).topMargin;

                    notificationsNumberSize = Math.min(avatarIV.getWidth(), avatarIV.getHeight());
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) notificationsTV.getLayoutParams();
                    params.width = (int) (notificationsNumberSize*0.3);
                    params.height = (int) (notificationsNumberSize*0.3);
                    Log.d("ert","par wid:"+params.width);
                    notificationsTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            OtherUtils.getTextSizeNumberBullet(getResources(), params.width));

                    //make sure its square
                    if (avatarIV.getTop() > avatarTopMargin) {
                        int extraPadding = (avatarIV.getTop() -  avatarTopMargin) /2;
                        nameTV.setPadding(nameTV.getPaddingLeft(), nameTV.getPaddingTop(),
                                nameTV.getPaddingRight(), nameTV.getPaddingBottom()
                                        + extraPadding);
                    }
                }
            }
        });
    }

    public TextView getNameTV() {
        return nameTV;
    }

    public ImageView getAvatarIV() {
        return avatarIV;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setNotificationsNumber(int notificationsNumber) {
        this.notificationsNumber = notificationsNumber;
        notificationsTV.setVisibility(notificationsNumber == 0 ? GONE : VISIBLE);
        notificationsTV.setText(String.valueOf(notificationsNumber));
        avatarIV.setBackground(notificationsNumber == 0 ? null :
                getResources().getDrawable(R.drawable.red_circle_contact));
    }

    public void clearView() {
        nameTV.setText("");
        Glide.with(getContext()).clear(avatarIV);
        progressBar.setVisibility(View.GONE);
        setNotificationsNumber(0);
    }
}
