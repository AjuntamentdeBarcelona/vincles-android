/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import cat.bcn.vincles.lib.business.CallService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class CallServiceTest {
    String accessToken;
    private String error;

    /*private String username = "user1_vincles@vincles-bcn.cat";
    private String password = "123456";*/
    private String username = "demo@vincles-bcn.cat";// idCalendar=4 id=11
    private String password = "123456";
    /*private String username = "410i877dadomn@vincles-bcn.cat"; // alias=acm41 id=80
    private String password = "55kmdag780ud6";*/

    @Before
    public void setUp() throws IOException {
        login(username, password);
    }

    private Response<JsonObject> login(String username, String password) throws IOException {
        UserService client = ServiceGenerator.createLoginService(UserService.class);
        Call<JsonObject> call = client.login(username, password);
        Response<JsonObject> result = call.execute();

        if (result.isSuccessful()) {
            // Set authToken globally for further request
            JsonObject json = result.body();
            accessToken = json.get("access_token").getAsString();
        }

        return result;
    }

    @Test
    public void testStartVideoConference() throws IOException {
        JsonObject item = new JsonObject();
        item.addProperty("idUser", 80);
        item.addProperty("idRoom", "ID_ROOM");

        CallService client = ServiceGenerator.createService(CallService.class, accessToken);
        Call<ResponseBody> call = client.startVideoConference(item);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }
}