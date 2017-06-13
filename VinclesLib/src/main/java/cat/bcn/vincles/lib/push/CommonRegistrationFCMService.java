/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.push;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import java.io.IOException;

import cat.bcn.vincles.lib.business.InstallationService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.vo.Installation;
import retrofit2.Call;
import retrofit2.Response;

public class CommonRegistrationFCMService {

    private static final String TAG = "RegistrationIntentSvc";
    private static final String[] TOPICS = {"global"};

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param imei The phone imei.
     */
    public static void sendFCMRegistrationToServer(String imei) {
        Log.i(TAG, "Firebase: Sending Installation to server.");
        new BackgroundInstallation(imei, 0).execute();
    }

    public static void saveSuccessfullInstallResponse(Installation installation, InstallationService installationService) {
        Call<JsonObject> call = installationService.getInstallation();
        try {
            Response<JsonObject> json = call.execute();
            installation.updateFromJSON(installation, json.body());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            Log.i(TAG, "Firebase: Async installation process started");
            String token = FirebaseInstanceId.getInstance().getToken();
            InstallationService installationService = ServiceGenerator.createService(
                    InstallationService.class, TokenAuthenticator.model.getAccessToken());

            Installation installation = Installation.findById(Installation.class, 1);

            if (installation == null) installation = new Installation();
            installation.setIdUser(TokenAuthenticator.model.getCurrentUserId());
            installation.setOperatingSystem(Installation.OS_ANDROID);
            installation.setImei(imei);
            installation.setPushToken(token);

            Log.v(TAG, "Firebase: Sending installation");

            try {
                Response response = installationService.addInstallation(installation.toJSON()).execute();
                if (response.isSuccessful()) {
                    Log.i(TAG, "Firebase: Installation sent successfully to server: " + response.code() + " " + response.message() + ": " + response.body().toString());
                    subscribeTopics();
                    saveSuccessfullInstallResponse(installation, installationService);
                } else {
                    Log.e(TAG, "Firebase: Failure Adding installation to server: " + response.code() + " " + response.message() + ": " + response.errorBody().string() + "\nRetry with update");

                    // FAIL 409 ADDING MEANS EXIST SO TRY UPDATE IT:
                    if (response.code() == 409) {
                        Response response2 = installationService.updateInstallation(installation.toJSON()).execute();
                        if (response2.isSuccessful()) {
                            Log.i(TAG, "Firebase: Update Installation sent successfully to server: " + response2.code() + " " + response2.message() + ": " + response2.body().toString());
                            saveSuccessfullInstallResponse(installation, installationService);
                            subscribeTopics();
                        } else {
                            Log.e(TAG, "Firebase: Failure sending installation to server: " + response2.code() + " " + response2.message() + ": " + response2.errorBody().string());
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Firebase: Error sending installation.", e);
                e.printStackTrace();
                return false;
            }

            return true;
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
