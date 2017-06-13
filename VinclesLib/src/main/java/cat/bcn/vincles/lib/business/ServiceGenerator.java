/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.business;

import java.io.IOException;

import cat.bcn.vincles.lib.VinclesApp;
import cat.bcn.vincles.lib.util.VinclesHttpClient;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    private static final String TAG = "ServiceGenerator";

    // PRE-Eurecat
    public static final String API_BASE_PRE_EURECAT = "https://XXX.XXX.XXX.XXX";
    public static final String BASIC_AUTH_PRE_EURECAT = "";
    public static final String MODULES_VERSION_URL_PRE_EURECAT = "";

    // PRE-Azure
    public static final String API_BASE_PRE_AZURE = "https://YYY.YYY.YYY.YYY";
    public static final String BASIC_AUTH_PRE_AZURE = "";
    public static final String MODULES_VERSION_URL_PRE_AZURE = "";

    // PRO-Azure
    public static final String API_BASE_PRO_AZURE = "https://ZZZ.ZZZ.ZZZ.ZZZ";
    public static final String BASIC_AUTH_PRO_AZURE = "";
    public static final String MODULES_VERSION_URL_PRO_AZURE = "";

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(getApiBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create());

    public static String getApiBaseUrl() {
        switch (VinclesApp.getVinclesApp().getAppFlavour()) {
            case VinclesApp.FLAVOUR_PRE_EURECAT:
                return API_BASE_PRE_EURECAT;
            case VinclesApp.FLAVOUR_PRE_AZURE:
                return API_BASE_PRE_AZURE;
            case VinclesApp.FLAVOUR_PRO_AZURE:
                return API_BASE_PRO_AZURE;
            case VinclesApp.FLAVOUR_PRODUCTION:
            default:
                return API_BASE_PRE_AZURE;
        }
    }

    public static String getApiBasicAuth() {
        switch (VinclesApp.getVinclesApp().getAppFlavour()) {
            case VinclesApp.FLAVOUR_PRE_EURECAT:
                return BASIC_AUTH_PRE_EURECAT;
            case VinclesApp.FLAVOUR_PRE_AZURE:
                return BASIC_AUTH_PRE_AZURE;
            case VinclesApp.FLAVOUR_PRO_AZURE:
                return BASIC_AUTH_PRO_AZURE;
            case VinclesApp.FLAVOUR_PRODUCTION:
            default:
                return BASIC_AUTH_PRE_AZURE;
        }
    }

    public static String getModulesVersionUrl() {
        switch (VinclesApp.getVinclesApp().getAppFlavour()) {
            case VinclesApp.FLAVOUR_PRE_EURECAT:
                return MODULES_VERSION_URL_PRE_EURECAT;
            case VinclesApp.FLAVOUR_PRE_AZURE:
                return MODULES_VERSION_URL_PRE_AZURE;
            case VinclesApp.FLAVOUR_PRO_AZURE:
                return MODULES_VERSION_URL_PRO_AZURE;
            case VinclesApp.FLAVOUR_PRODUCTION:
            default:
                return MODULES_VERSION_URL_PRE_AZURE;
        }
    }

    public static <S> S createLoginService(Class<S> serviceClass) {
        VinclesHttpClient.Builder clientBuilder = new VinclesHttpClient().newBuilder();

        final String basic = "Basic " + getApiBasicAuth();

        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("grant_type", "password")
                        .build();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Accept", "application/json")
                        .url(url)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = clientBuilder.build();

        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass) {
        OkHttpClient client = createOkHttpClient();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String accessToken) {
        OkHttpClient client = createOkHttpClient(accessToken);
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static OkHttpClient createOkHttpClient() {
        VinclesHttpClient.Builder clientBuilder = new VinclesHttpClient().newBuilder();

        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        //.header("Content-Type", "multipart/form-data")
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        return clientBuilder.build();
    }

    public static OkHttpClient createOkHttpClient(String accessToken) {
        VinclesHttpClient.Builder clientBuilder = new VinclesHttpClient().newBuilder();

        final String basic = "Bearer " + accessToken;

        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        return clientBuilder.build();
    }
}