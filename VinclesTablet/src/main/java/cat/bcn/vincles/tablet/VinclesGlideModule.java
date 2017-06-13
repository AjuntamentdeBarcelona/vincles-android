/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;
import java.io.InputStream;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import okhttp3.OkHttpClient;

public class VinclesGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) { }

    @Override
    public void registerComponents(Context context, Glide glide) {
        OkHttpClient client = ServiceGenerator.createOkHttpClient();
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        glide.register(GlideUrl.class, InputStream.class, factory);
    }
}