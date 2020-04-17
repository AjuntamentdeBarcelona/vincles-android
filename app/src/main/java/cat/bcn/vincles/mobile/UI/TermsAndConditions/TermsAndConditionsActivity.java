package cat.bcn.vincles.mobile.UI.TermsAndConditions;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class TermsAndConditionsActivity extends BaseActivity implements View.OnClickListener {

    Button acceptBtn, cancelBtn;


    @Override
    protected void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(this,
                getResources().getString(R.string.tracking_terms_conditions));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean firstboot = getSharedPreferences("BOOT_PREF", MODE_PRIVATE).getBoolean("firstboot", true);

        super.onCreate(savedInstanceState);

        if (firstboot==true){

            UserPreferences userPreferences = new UserPreferences(this);
            int userID = userPreferences.getUserID();
            Log.d("TermsAnd userID ", String.valueOf(userID));
            if (userID != 0) {
                Intent intent = new Intent(this, MainFragmentManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            setContentView(R.layout.activity_terms_and_conditions);

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);

            acceptBtn = findViewById(R.id.accept);
            cancelBtn = findViewById(R.id.cancel);

            acceptBtn.setOnClickListener(this);
            cancelBtn.setOnClickListener(this);

            String htmlAsString = getString(R.string.termsandconditions);
            WebView webView = (WebView) findViewById(R.id.webView);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setHorizontalScrollBarEnabled(false);
            WebSettings webSettings = webView.getSettings();
            webSettings.setDefaultFontSize(12);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.loadDataWithBaseURL(null, htmlAsString, "text/html", "utf-8", null);

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.accept) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            getSharedPreferences("BOOT_PREF", MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstboot", false)
                    .commit();
        } else if (view.getId() == R.id.cancel) {
            finishAffinity();
            System.exit(0);
        }
    }
}
