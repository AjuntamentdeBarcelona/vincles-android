package cat.bcn.vincles.mobile.UI.StartGuide;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class StartGuideActivity extends BaseActivity implements View.OnClickListener {

    ViewPager pager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_guide);

        pager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tablayout);
        pager.setAdapter(new GuidePagerAdapter(this));
        tabLayout.setupWithViewPager(pager, true);

        findViewById(R.id.close_button).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(this,
                getResources().getString(R.string.tracking_initial_guide));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.close_button) {
            finish();
        }
    }
}
