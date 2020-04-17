package cat.bcn.vincles.mobile.Client.Requests;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.TokenFromLogin;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static cat.bcn.vincles.mobile.Utils.MyApplication.getAppContext;

public class RenewTokenRequest implements Callback<TokenFromLogin> {

    UserService userService;
    private static final String USER_PREFIX = "@vincles-bcn.cat";
    List<BaseRequest> pendingRequests = new ArrayList<>();
    Boolean isOnRequest = false;
    BaseRequest.RenewTokenFailed listener;
    Retrofit retrofit;

    private static volatile RenewTokenRequest sSoleInstance = new RenewTokenRequest();

    //private constructor.
    private RenewTokenRequest(){

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            clientBuilder.addInterceptor(logging);
        }
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
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


        userService = retrofit.create(UserService.class);

        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public synchronized static RenewTokenRequest getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new RenewTokenRequest();
        }

        return sSoleInstance;
    }

    public void doRequest(String refreshToken) {
        isOnRequest = true;

        Log.d("requesttoken","doRequest");

        Call<TokenFromLogin> call = userService.renewToken("refresh_token",refreshToken);
        call.enqueue(this);
    }



    @Override
    public void onResponse(Call<TokenFromLogin> call, Response<TokenFromLogin> response) {
        UserPreferences userPreferences = new UserPreferences();

        if (response.isSuccessful()) {
            Log.d("requesttoken","onResponse");

            TokenFromLogin tokenFromLogin = response.body();
            userPreferences.setAccessToken(tokenFromLogin.getAccessToken());
            userPreferences.setExpiresIn(tokenFromLogin.getExpiresIn());
            userPreferences.setTokenType(tokenFromLogin.getTokenType());
            userPreferences.setRefreshToken(tokenFromLogin.getRefreshToken());

            for(Iterator<BaseRequest> it = pendingRequests.iterator(); it.hasNext();) {
                BaseRequest request = it.next();

                request.doRequest(tokenFromLogin.getAccessToken());
                it.remove();
            }

            userPreferences.setRenewTokenFailed(false);
        } else {
            pendingRequests.clear();
           if(listener!=null)listener.onRenewTokenFailed();
            OtherUtils.cancelProcessingNotifications(getAppContext());
            if (!((MyApplication)getAppContext()).isBackground){
                BaseActivity currentActivity = ((MyApplication)getAppContext()).getCurrentActivity();
                if (currentActivity != null){
                    currentActivity.renewTokenFailure();
                }
            }


            userPreferences.setLoginDataDownloaded(false);
            userPreferences.setRenewTokenFailed(true);
        }
        isOnRequest = false;

    }

    @Override
    public void onFailure(Call<TokenFromLogin> call, Throwable t) {
        isOnRequest = false;
        pendingRequests.clear();
        if(listener!=null)listener.onRenewTokenFailed();

        OtherUtils.cancelProcessingNotifications(getAppContext());


        BaseActivity currentActivity = ((MyApplication)getAppContext()).getCurrentActivity();
        if (currentActivity != null){
            currentActivity.renewTokenFailure();
        }
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setLoginDataDownloaded(false);
        userPreferences.setRenewTokenFailed(true);

    }




}
