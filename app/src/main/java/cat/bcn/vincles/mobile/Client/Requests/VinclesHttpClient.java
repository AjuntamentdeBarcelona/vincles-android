package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.squareup.okhttp.MediaType;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.NetworkUsage.DataUsageUtils;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static cat.bcn.vincles.mobile.Client.Requests.BaseRequest.BEARER_AUTH;

public class VinclesHttpClient {
    static Dispatcher dispatcher;

    public static OkHttpClient getOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (!BuildConfig.FLAVOR.equals("proazure")) {
                //TODO: CAUTION, insecure, only for development
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
                builder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            builder.addInterceptor(REQUEST_INTERCEPTOR);
            builder.addInterceptor(RESPONSE_INTERCEPTOR);



            return builder
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Interceptor REQUEST_INTERCEPTOR = new Interceptor() {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();

           /* Log.d("interceptor", "Entra Request");
            Log.d("interceptor", "URL -->" + String.valueOf(originalRequest.url()));
            Log.d("interceptor", "METHOD -->" + String.valueOf(originalRequest.method()));*/


            String tag = "undefined";
            if(originalRequest.tag() instanceof String[]){
                tag = ((String[])originalRequest.tag())[0];
                //Log.d("interceptor", "TAG -->" + tag);
            }

            String method = originalRequest.method();


            if(originalRequest.body() != null){
               // Log.d("interceptor", "BODY length: " + String.valueOf(originalRequest.body().contentLength()));
                long data = originalRequest.body().contentLength();
                if (data != -1){
                    DataUsageUtils dataUsageUtils = new DataUsageUtils();
                    dataUsageUtils.addDataUsage(tag,data, "request");
                }

            }


            return chain.proceed(originalRequest);
        }
    };

    private static final Interceptor RESPONSE_INTERCEPTOR = new Interceptor() {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());

          /*  Log.d("interceptor", "Entra Response");
            Log.d("interceptor", "URL -->" + String.valueOf(originalResponse.request().url()));
            Log.d("interceptor", "METHOD -->" + String.valueOf(originalResponse.request().method()));
            Log.d("interceptor", "RESPONSE -->" + String.valueOf(originalResponse.body()));*/

            String tag = "undefined";
            if(originalResponse.request().tag() instanceof String[]){
                tag = ((String[])originalResponse.request().tag())[0];
              //  Log.d("interceptor", "TAG -->" + tag);
            }
            String method = originalResponse.request().method();

            if(originalResponse.body() != null){
                long data = originalResponse.body().contentLength();
                if (data != -1){
                 //   Log.d("interceptor", "BODY length: " + String.valueOf(originalResponse.body().contentLength()));
                    DataUsageUtils dataUsageUtils = new DataUsageUtils();
                    dataUsageUtils.addDataUsage(tag,data, "response");
                }

            }


            return originalResponse.newBuilder()
                    .build();


        }
    };

    private static final Interceptor RESPONSE_CANCEL_INTERCEPTOR = new Interceptor() {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            Request originalRequest = chain.request();

            String auth = originalRequest.header("Authorization");
            Log.d("cancel_interceptor", "Response Header: " + auth);
            String currentToken = BEARER_AUTH + new UserPreferences().getAccessToken();

            Log.d("cancel_interceptor", "Response Prefs: " + currentToken);
            if(!auth.contains("Basic")){
                if(currentToken.equals(BEARER_AUTH) || !auth.equals(currentToken)){
                    Log.d("cancel_interceptor", "response bad");
               //     okhttp3.MediaType contentType = originalResponse.body().contentType();


                }
            }


            return originalResponse.newBuilder()
                    .build();
        }
    };

    private static final Interceptor REQUEST_CANCEL_INTERCEPTOR = new Interceptor() {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            String auth = originalRequest.header("Authorization");

            Log.d("cancel_interceptor", "Request Header: " + auth);
            String currentToken = BEARER_AUTH + new UserPreferences().getAccessToken();
            Log.d("cancel_interceptor", "Request Prefs: " + currentToken);
            if(!auth.contains("Basic")){
                if(currentToken.equals(BEARER_AUTH) ||!auth.equals(currentToken)){
                    Log.d("cancel_interceptor", "request bad");
                   // okhttp3.MediaType contentType = originalRequest.body().contentType();


                }
            }

            return chain.proceed(originalRequest);
        }
    };

    public static OkHttpClient getOkHttpClient(OkHttpClient.Builder builder) {

        builder.addInterceptor(REQUEST_INTERCEPTOR);
        builder.addInterceptor(RESPONSE_INTERCEPTOR);


        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            if (!BuildConfig.FLAVOR.equals("proazure")) {
                // TODO: CAUTION, insecure, only for development
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
                builder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            dispatcher=new Dispatcher();
            dispatcher.setMaxRequests(10);
            builder.dispatcher(dispatcher);


            return builder
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void cancellAllRequests(){
        if(dispatcher != null){
            dispatcher.cancelAll();
        }
    }
}
