/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import android.util.Log;

import com.google.gson.JsonObject;

import java.io.IOException;

import cat.bcn.vincles.lib.BuildConfig;
import cat.bcn.vincles.lib.VinclesApp;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.vo.User;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {
    public static String key = "";
    public static String username;
    public static byte[] password;
    public static Model model;

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        UserService userService = ServiceGenerator.createLoginService(UserService.class);
        String plainPass = "";
        try {
            Security sec = new Security();

            // LOGS TO CATCH ERRORS HERE
            try {
                if (VinclesApp.isDebugVersion()) {
                    Log.i(null, "---------- PREPROCESS ----------");
                    Log.i(null, "key = " + key);
                    Log.i(null, "username = " + username);
                    String passTemp = "";
                    for (byte b : password) {
                        passTemp += String.format("0x%2x, ", b);
                    }
                    Log.i(null, "Pass: " + passTemp);
                    Log.i(null, "model = " + model);
                    Log.i(null, "Model User Id = " + model.getCurrentUserId());
                    User temp = new UserDAOImpl().get(model.getCurrentUserId());
                    Log.i(null, "Model User Name = " + temp.username);
                    byte[] password = temp.cipher;
                    passTemp = "";
                    for (byte b : password) {
                        passTemp += String.format("0x%2x, ", b);
                    }
                    Log.i(null, "Pass: " + passTemp);
                    Log.i(null, "------------------------------");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Remove suffix if exist
            if (username != null)
                username = username.replace(VinclesConstants.LOGIN_SUFFIX, "");


            if (model.getCurrentUserId() != null) {
                key = sec.md5(model.getCurrentUserId().toString());

                try {
                    User temp = new UserDAOImpl().get(model.getCurrentUserId());
                    password = temp.cipher;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            sec.loadPlainAESKey(key);
            plainPass = sec.AESdecrypt(password);
        } catch (Exception e)  { e.printStackTrace(); }

        // LOGS TO CATCH ERRORS HERE
        try {
            if (VinclesApp.isDebugVersion()) {
                Log.i(null, "---------- PREPROCESS ----------");
                Log.i(null, "key = " + key);
                Log.i(null, "username = " + username);
                String passTemp = "";
                for (byte b : password) {
                    passTemp += String.format("0x%2x, ", b);
                }
                Log.i(null, "Pass: " + passTemp);
                Log.i(null, "model = " + model);
                Log.i(null, "Model User Id = " + model.getCurrentUserId());
                User temp = new UserDAOImpl().get(model.getCurrentUserId());
                Log.i(null, "Model User Name = " + temp.username);
                byte[] password = temp.cipher;
                passTemp = "";
                for (byte b : password) {
                    passTemp += String.format("0x%2x, ", b);
                }
                Log.i(null, "Pass: " + passTemp);
                Log.i(null, "------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Call<JsonObject> call = userService.login(username + VinclesConstants.LOGIN_SUFFIX, plainPass);
        // This call is made correctly, as it shows up on the back-end.
        retrofit2.Response<JsonObject> result = call.execute();
        plainPass = "-------";

        if (result.isSuccessful()) {
            // Set authToken globally for further request
            JsonObject json = result.body();
            String accessToken = json.get("access_token").getAsString();

            model.updateAccessToken(accessToken);

            // Repeat previous request with renewed token!
            String basic = "Bearer " + accessToken;
            return response.request().newBuilder()
                    .header("Authorization", basic)
                    .build();
        } else {
            throw new IOException(VinclesError.ERROR_LOGIN);
        }
    }
}
