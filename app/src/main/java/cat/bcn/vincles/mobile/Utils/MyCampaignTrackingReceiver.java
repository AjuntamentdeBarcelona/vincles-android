package cat.bcn.vincles.mobile.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import cat.bcn.vincles.mobile.R;

public class MyCampaignTrackingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        MyApplication app = (MyApplication) context.getApplicationContext();
        if (app != null) {
            // Get the Tracker shared instance
            Tracker t = app.getDefaultTracker();
            String campaignData = app.getResources().getString(R.string.tracking_campaign_url, bundle.get("referrer"));
            Log.d("GAv4", "campaignData=" + campaignData);
            t.send(new HitBuilders.ScreenViewBuilder()
                    .setCampaignParamsFromUrl(campaignData)
                    .build());
        }
    }
}
