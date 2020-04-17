package cat.bcn.vincles.mobile.Client.Requests;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import cat.bcn.vincles.mobile.Client.Enviroment.Environment;

public abstract class BaseRequest {

    static final int UNAUTHENTICATED_REQUEST = 1;
    static final int LOGIN_LOGOUT_REQUEST = 2;
    static final int AUTHENTICATED_REQUEST = 3;

    static final String BEARER_AUTH = "Bearer ";
    Retrofit retrofit;
    String refreshToken;
    RenewTokenFailed listener;
    OkHttpClient okHttpClient;

    //singleton retrofit for authenticated requests
    private static Retrofit authenticatedRetrofit = null;
    private static String authenticatedToken = null;

    public BaseRequest (RenewTokenFailed listener, int typeRequest){
        this.listener = listener;
        switch (typeRequest) {
            case UNAUTHENTICATED_REQUEST:
                unauthnticatedRequest();
                return;
            case LOGIN_LOGOUT_REQUEST:
                loginRequest();
                return;
        }
    }

    public BaseRequest (RenewTokenFailed listener, int typeRequest, String accessToken){
        this.listener = listener;
        switch (typeRequest) {
            case AUTHENTICATED_REQUEST:
                authenticatedRequest(accessToken);
                return;
        }
    }

    public abstract void doRequest(String token);

    public void onRenewTokenFailed() {
        if (listener!=null) listener.onRenewTokenFailed();
    }

    public void unauthnticatedRequest() {

        final OkHttpClient okHttpClient = VinclesHttpClient.getOkHttpClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(Environment.getApiBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    private static Gson getGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public void loginRequest () {

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(logging);
        }
        clientBuilder.addInterceptor(new Interceptor() {
                                      @Override
                                      public Response intercept(Interceptor.Chain chain) throws IOException {
                                          Request original = chain.request();

                                          Request request = original.newBuilder()
                                                  .header("Authorization", "Basic " + Environment.getApiBasicAuth())
                                                  .header("Content-Type", "application/x-www-form-urlencoded")
                                                  .method(original.method(), original.body())
                                                  .build();

                                          return chain.proceed(request);
                                      }
                                  });


        OkHttpClient okHttpClient = VinclesHttpClient.getOkHttpClient(clientBuilder);
        GsonConverterFactory x = GsonConverterFactory.create();
        retrofit = new Retrofit.Builder().baseUrl(Environment.getApiBaseUrl()) .addConverterFactory(x) .client(okHttpClient) .build();
    }

    public void authenticatedRequest (final String accessToken) {
        retrofit = getAuthenticatedRetrofitInstance(accessToken);
    }

    public boolean shouldRenewToken(BaseRequest request, retrofit2.Response response) {
        Log.d("shouldRenewToken", "response.code()" + response.code());

        if (response.code() == 401) {
            UserPreferences userPreferences = new UserPreferences();

            RenewTokenRequest renewTokenRequest = RenewTokenRequest.getInstance();
            Log.d("shouldRenewToken", "getLoginDataDownloaded" + userPreferences.getLoginDataDownloaded());
            if(userPreferences.getLoginDataDownloaded() && !userPreferences.getRenewTokenFailed()){
                renewTokenRequest.pendingRequests.add(request);
                Log.d("shouldRenewToken", "renewTokenRequest.isOnRequest" + renewTokenRequest.isOnRequest);
                if(!renewTokenRequest.isOnRequest){
                    renewTokenRequest.listener = listener;
                    renewTokenRequest.doRequest(new UserPreferences().getRefreshToken());
                }
            }
            return true;
        }
        return false;
    }

    public interface RenewTokenFailed {
        public void onRenewTokenFailed();
    }

    private Retrofit getAuthenticatedRetrofitInstance(final String accessToken) {
        if (authenticatedToken == null || !authenticatedToken.equals(accessToken)) {

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                // set your desired log level
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                clientBuilder.addInterceptor(logging);
            }
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request request = original.newBuilder()
                            .header("Authorization", BEARER_AUTH + accessToken)
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                }
            });


            final OkHttpClient okHttpClient = VinclesHttpClient.getOkHttpClient(clientBuilder);
            authenticatedRetrofit = new Retrofit.Builder()
                    .baseUrl(Environment.getApiBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .callFactory(new Call.Factory() {
                        @Override
                        public Call newCall(Request request) {

                            request = request.newBuilder().tag(new String[]{null}).build();

                            Call call = okHttpClient.newCall(request);

                            return call;
                        }})
                    .build();

            authenticatedToken = accessToken;
        }

        return authenticatedRetrofit;
    }


}
