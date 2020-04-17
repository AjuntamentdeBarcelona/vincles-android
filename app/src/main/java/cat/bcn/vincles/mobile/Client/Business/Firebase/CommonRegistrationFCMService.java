package cat.bcn.vincles.mobile.Client.Business.Firebase;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.IOException;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.VinclesHttpClient;
import cat.bcn.vincles.mobile.Client.Services.InstallationService;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommonRegistrationFCMService {

    private static final String TAG = "RegistrationIntentSvc";
    private static final String[] TOPICS = {"global"};

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     */
    public static void sendFCMRegistrationToServer(String imei) {
        Log.i(TAG, "Firebase: Sending Installation to server.");
        new BackgroundInstallation( imei, 0).execute();
    }

    public static void saveSuccessfullInstallResponse(Installation installation, InstallationService installationService) {
        Log.i("getInst", "Firebase: no longer using get Installation");
        installation.updateFromJSON(installation, null);
        /*Call<JsonObject> call = installationService.getInstallation(
                new UserPreferences(MyApplication.getAppContext()).getIdInstallation());
        try {
            Response<JsonObject> json = call.execute();
            installation.updateFromJSON(installation, json.body());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    // Non-blocking methods. No need to use AsyncTask or background thread.
    private static void subscribeTopics() throws IOException {
        Log.i(TAG, "Subscribing to receive asynchornous messages from Firebase.");
        for (String topic : TOPICS) {
            FirebaseMessaging.getInstance().subscribeToTopic("mytopic");
        }
    }


    static class BackgroundInstallation extends AsyncTask<Void, Void, Boolean> {
        private String imei = "";
        private int retries = 0;

        BackgroundInstallation(String pImei, int pRetry) {
            imei = pImei;
            retries = pRetry;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final UserPreferences userPreferences = new UserPreferences(MyApplication.getAppContext());
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(logging);
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + userPreferences.getAccessToken())
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                }
            });
            OkHttpClient okHttpClient = VinclesHttpClient.getOkHttpClient(clientBuilder);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Environment.getApiBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            Log.i(TAG, "Firebase: Async installation process started");
            String token = FirebaseInstanceId.getInstance().getToken();
            String deviceId = FirebaseInstanceId.getInstance().getId();
            Log.d("firebaseToken", "Token: "+token);
            Log.d("firebaseId", "DeviceId: "+deviceId);
            InstallationService installationService = retrofit.create(InstallationService.class);

            Installation installation = Installation.getInstallation();

            if (installation == null) installation = new Installation();
            installation.setIdUser((long) userPreferences.getUserID());
            installation.setOperatingSystem(Installation.OS_ANDROID);
            installation.setImei(imei);
            installation.setDeviceId(deviceId);
            installation.setPushToken(token);
            installation.setAppVersion(BuildConfig.VERSION_NAME);
            installation.setIdSession(userPreferences.getIdSession());
            installation.setId(userPreferences.getIdInstallation());

            Log.v(TAG, "Firebase: Sending installation");
            Log.v(TAG, "Firebase: Installation Json:"+installation.toJSON());

            try {
                Response<JsonObject> response = installationService.addInstallation(installation.toJSON()).execute();
                if (response.isSuccessful()) {
                    Log.d("instll","Save new installation success");
                    Log.i(TAG, "Firebase: Installation sent successfully to server: " + response.code() + " " + response.message() + ": " + response.body().toString());
                    subscribeTopics();
                    if (!response.body().get("id").equals(JsonNull.INSTANCE)) {
                        installation.setId(response.body().get("id").getAsInt());
                        Log.d("instll","Save new installation success id:"+installation.getId());
                    }
                    saveSuccessfullInstallResponse(installation, installationService);
                } else {
                    Log.d("instll","Save new installation fail");
                    Log.e(TAG, "Firebase: Failure Adding installation to server: " + response.code() + " " + response.message() + ": " + response.errorBody().string() + "\nRetry with update");

                    // FAIL 409 ADDING MEANS EXIST SO TRY UPDATE IT:
                    if (response.code() == 409 && installation.getId() == -1) {
                        //we do not have the id of the installation, so we need to request it
                        Log.d("instll","getAllInstallations, id:"+installation.getId()+" deviceId:"+installation.getDeviceId());
                        Response<JsonArray> response2 = installationService.getAllInstallations(
                                installation.getDeviceId()+installation.getIdUser()).execute();
                        if (response2.isSuccessful()) {
                            //get id, save it and update the installation now that we have the id
                            Log.d("instll","Get installations OK, body:"+response2.body());

                            if(response2.body().size()!=0) {
                              JsonObject firstObject = ((JsonObject) response2.body().getAsJsonArray().get(0));

                              if (firstObject != null) {
                                int id = firstObject.get("id").getAsInt();
                                installation.setId(id);
                                userPreferences.setIdInstallation(id);
                                updateInstallation(installationService, installation);
                                Log.d("instll","Get installations OK, id:"+id);
                              }
                            }
                        } else {
                            Log.e(TAG, "Firebase: Failure getting the list of installations: " + response2.code() + " " + response2.message() + ": " + response2.errorBody().string());
                        }

                    } else if (response.code() == 409) {
                        Log.d("instll","Update installation");
                        updateInstallation(installationService, installation);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Firebase: Error sending installation.", e);
                e.printStackTrace();
                return false;
            }

            return true;
        }

        private void updateInstallation(InstallationService installationService,
                                        Installation installation) {
            try {
                Response response2 = installationService.updateInstallation(
                        installation.getId(), installation.toJSON()).execute();
                if (response2.isSuccessful()) {
                    Log.i(TAG, "Firebase: Update Installation sent successfully to server: " + response2.code() + " " + response2.message() + ": " + response2.body().toString());
                    saveSuccessfullInstallResponse(installation, installationService);
                    subscribeTopics();
                } else {
                    Log.e(TAG, "Firebase: Failure sending installation to server: " + response2.code() + " " + response2.message() + ": " + response2.errorBody().string());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        protected void onPostExecute(Boolean ret) {
            // RETRY 3 TIMES IF IT IS NOT WORKING
            if (!ret && retries < 3) {
                retries++;
                new BackgroundInstallation(imei, retries).execute();
            }
        }
    }

}
