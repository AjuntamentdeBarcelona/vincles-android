/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class UserServiceTest {
    String accessToken;
    private String error;

    /*private String username = "user1_vincles@vincles-bcn.cat";
    private String password = "123456";*/
    private String username = "demo11@vincles-bcn.cat";// idCalendar=4 id=11
    private String password = "123456";
    /*private String username = "67fihk4lk08jt"; // alias=acm121 id=25
    private String password = "cs1mn0a4ju8u2";*/

    private Long userId;
    private User user;
    private String code;

    @Before
    public void setUp() throws IOException {
        login(username, password);
    }

    @Test
    public void testGetLogin() throws IOException {
        Response<JsonObject> result = login(username, password);
        JsonObject json = result.body();
        accessToken = json.get("access_token").getAsString();
        assertNotNull(accessToken);
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
    public void testLogin() throws IOException {
        // User OK
        Response<JsonObject> result = login(username, password);

        if (result.isSuccessful()) {
            // Set authToken globally for further request
            JsonObject json = result.body();
            assertNotNull(json);
            String accessToken = json.get("access_token").getAsString();
            assertNotNull(accessToken);
        } else {
            fail(result.message());
        }

        // User fail
        result = login(username, "xxx");

        if (result.isSuccessful()) {
            fail("Must not be logged!");
        }
    }

    @Test
    public void testAssociateAnonymous() throws IOException {
        testGenerateCode();

        User user = new User();
        user.name = "test";
        user.lastname = "test";
        user.birthdate = new Date();
        user.email = "test@test.es";
        user.phone = "123456789";
        user.gender = false;
        user.liveInBarcelona = true;
        user.registerCode = code;
        user.relationship = "OTHER";

        UserService client = ServiceGenerator.createService(UserService.class);
        Call<JsonObject> call = client.associateAnonymous(user.toJSON());
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            assertNotNull(json);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testAssociateRegistered() throws IOException {
        testGenerateCode();

        JsonObject association = new JsonObject();
        association.addProperty("registerCode", code);
        association.addProperty("relationship", "PARTNER");

        login(username, password);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.associateRegistered(association);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            assertNotNull(json);
            fail("User is already in circle!");
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            assertEquals("1321", error.getCode());
        }
    }

    @Test
    public void testGenerateCode() throws IOException {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.generateCode(new JsonObject());
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            code = json.get("registerCode").getAsString();
            assertNotNull(code);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testGetUserNetworkList() throws IOException {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonArray> call = client.getUserVinclesNetworkList();
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            assertNotNull(jsonArray);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testErrorHandler() throws IOException {
        //{"errors":[{"code":1321,"message":"User is already in circle"}]}
        JsonObject json = new JsonObject();
        json.addProperty("code", 1321);
        json.addProperty("message", "User is already in circle");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(json);
        JsonObject errors = new JsonObject();
        errors.add("errors", jsonArray);

        JsonElement element = errors.get("errors");
        VinclesError[] result = new GsonBuilder().create().fromJson(element, VinclesError[].class);

        assertNotNull(result);
    }

    @Test
    public void testGetUserMobileNetworkList() throws IOException {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonArray> call = client.getUserMobileNetworkList();
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            assertNotNull(jsonArray);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testGetMyUserInfo() throws IOException {
        login(username, password);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getMyUserInfo();
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            userId = result.body().get("id").getAsLong();
            assertNotNull(userId);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testDeleteUser() throws IOException {
        Long userId = 42l;
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.deleteUser(userId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testUpdateUser() throws IOException {
        login(username, password);
        User user = new User();

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call1 = client.getMyUserInfo();
        Response<JsonObject> result1 = call1.execute();
        if (result1.isSuccessful()) {
            JsonObject json = result1.body();
            user = User.fromJSON(json);
            assertNotNull(user);
        } else {
            VinclesError error = ErrorHandler.parseError(result1);
            fail(error.getMessage());
        }

        user.name = "Admin - test";
        //CAUTION: Photo not in json >>> user.photo = "";

        UserService client2 = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call2 = client2.updateUser(user.toJSON());
        Response<ResponseBody> result2 = call2.execute();
        if (result2.isSuccessful()) {
            assertNotNull(result2.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result2);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetFullUserInfo() throws IOException {
        //testGetMyUserInfo(); // Login & set userId
        Long userId = 10l;
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getFullUserInfo(userId);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            user = User.fromJSON(json);
            assertNotNull(user);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetUserPhoto() throws IOException {
        testGetFullUserInfo(); // Login & set userId

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.getUserPhoto(userId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            byte[] data = IOUtils.toByteArray(result.body().byteStream());
            writeImage(data);
            assertNotNull(data);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    private void writeImage(byte[]data) {
        File file = new File("/opt/vincles/" + new Date().getTime() + "_user.jpg");
        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAndSetUserID() throws IOException {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getMyUserInfo();
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            User user = User.fromJSON(json);
            assertNotNull(json);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testUpdateUserPhoto() throws IOException {
        String nameFile = "user";
        File imageFile = new File("/opt/vincles/" + nameFile + VinclesConstants.IMAGE_EXTENSION);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part data = MultipartBody.Part.createFormData("file", nameFile, file);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.updateUserPhoto(data);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            assertNotNull(json);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testResetPassword() throws IOException {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.resetPassword();
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            String newPassword = json.get("newPassword").getAsString();
            assertNotEquals(newPassword, password);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }
}