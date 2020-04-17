package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;

public class ContentDetailAugmentedActivity extends BaseActivity implements ContentDetailAugmentedFragment.ContentDetailAugmentedListener, ContentDetailAugmentedFragment.OnFragmentInteractionnAugmentedListener{

    public String filePath;
    public String filterKind;
    public int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    |  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        decorView.setSystemUiVisibility(uiOptions);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle b = new Bundle();
        b = getIntent().getExtras();

        filePath =  b.getString("filePath", "");
        filterKind =  b.getString("filterKind", "");
        index =  b.getInt("index", 0);

        setContentView(R.layout.activity_content_detail_augmented);




    }

    @Override
    public void onGalleryShareButtonClicked(int idContent, String path, String metadata) {

    }

    @Override
    public void onFragmentInteractionAugmented(Uri uri) {

    }
}
