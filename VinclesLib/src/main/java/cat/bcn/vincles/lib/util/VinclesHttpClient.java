/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import cat.bcn.vincles.lib.VinclesApp;
import okhttp3.OkHttpClient;

public class VinclesHttpClient extends OkHttpClient {
    private static final String TAG = "VinclesHttpClient";
    @Override
    public Builder newBuilder() {
        Builder builder = super.newBuilder();

        switch(VinclesApp.getVinclesApp().getAppFlavour()) {
            case VinclesApp.FLAVOUR_PRE_EURECAT:
            case VinclesApp.FLAVOUR_PRE_AZURE:
                // TODO: CAUTION, insecure, only for development
                builder.sslSocketFactory(UnsafeOkHttpClient.getSSLSocketFactory());
                builder.hostnameVerifier(UnsafeOkHttpClient.getHostnameVerifier());
                break;
            case VinclesApp.FLAVOUR_PRO_AZURE:
                break;
        }
        
        builder.authenticator(new TokenAuthenticator());
        return builder;
    }
}
