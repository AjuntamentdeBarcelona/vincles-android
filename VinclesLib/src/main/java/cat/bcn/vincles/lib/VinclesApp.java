/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VinclesApp extends com.orm.SugarApp {
    private static final String TAG = "VinclessApp";

    public static VinclesApp getVinclesApp() {
        return vinclesApp;
    }

    private static VinclesApp vinclesApp;

    public static final int FLAVOUR_PRE_EURECAT = 1;
    public static final int FLAVOUR_PRE_AZURE = 2;
    public static final int FLAVOUR_PRODUCTION = 3;
    public static final int FLAVOUR_PRO_AZURE = 4;

    public VinclesApp () {
        super();
        vinclesApp = this;
    }

    public int getAppFlavour() {
        // WILL BE OVERRIDED IN BOTH APP APPLICATIONS
        // There is no way to get the App flavour (versionName should be in number format)
        return FLAVOUR_PRODUCTION;
    }

    public static boolean isDebugVersion () {
        PackageInfo pi = null;
        try {
            if (vinclesApp == null) {
                return true;
            }
            pi = vinclesApp.getPackageManager().getPackageInfo(vinclesApp.getPackageName(), 0);
            if (pi.packageName.contains(".debug")) return true;
            else return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}