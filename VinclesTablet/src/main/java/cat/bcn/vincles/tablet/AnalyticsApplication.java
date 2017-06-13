/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import cat.bcn.vincles.lib.VinclesApp;
import io.fabric.sdk.android.Fabric;

public class AnalyticsApplication extends VinclesApp {
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        // Enable automatic activity tracking for your app
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public int getAppFlavour() {
        if (BuildConfig.FLAVOR.contains("preeurecat")) return FLAVOUR_PRE_EURECAT;
        else if (BuildConfig.FLAVOR.contains("preazure")) return FLAVOUR_PRE_AZURE;
        else if (BuildConfig.FLAVOR.contains("proazure")) return FLAVOUR_PRO_AZURE;
        else return FLAVOUR_PRODUCTION;
    }
}
