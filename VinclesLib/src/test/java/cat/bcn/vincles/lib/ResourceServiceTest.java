/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.business.ResourceService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ResourceServiceTest {
    private String username = "demo11@vincles-bcn.cat";// idCalendar=4 id=11
    private String password = "123456";
    private String accessToken;
    private Long userFromId = 12l;
    private Long userToId = 59l;
    private Long messageId;

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

    @Before
    public void setUp() throws IOException {
        login(username, password);
    }

    @Test
    public void testGetResource() throws IOException {
        Long resourceId = 42l;

        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);
        Call<ResponseBody> call = client.getResource(resourceId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            byte[] data = IOUtils.toByteArray(result.body().byteStream());
            assertNotNull(data);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetResourceInfo() throws IOException {
        Long resourceId = 67l;

        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);
        Call<JsonObject> call = client.getResourceInfo(resourceId);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            String mimeType = json.get("mimeType").getAsString();
            assertEquals("image/jpeg", mimeType);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetResourceListFromLibrary() throws IOException {
        Calendar calFrom = Calendar.getInstance();
        calFrom.add(Calendar.DATE, -1);
        Calendar calTo = Calendar.getInstance();
        calTo.add(Calendar.DATE, 1);
        String from = "";//String.valueOf(calFrom.getTime().getTime());
        String to = "";//String.valueOf(calTo.getTime().getTime());

        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);
        Call<JsonArray> call = client.getResourceListFromLibrary("", "");
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            List<Resource> items = new ArrayList<Resource>();
            for (JsonElement it : jsonArray) {
                Resource item = Resource.fromJSON(it.getAsJsonObject());
                items.add(item);
            }
            assertNotNull(jsonArray);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testDeleteResource() throws IOException {
        Long resourceId = 317l;

        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);
        Call<ResponseBody> call = client.deleteResource(resourceId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testAddResource() throws IOException {
        Long resourceId = 67l;

        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);
        Call<ResponseBody> call = client.addResource(resourceId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testSendResource() throws IOException {
        String nameFile = "user";
        File imageFile = new File("/opt/vincles/" + nameFile + VinclesConstants.IMAGE_EXTENSION);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part data = MultipartBody.Part.createFormData("file", nameFile, file);

        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);
        Call<JsonObject> call = client.sendResource(data);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            assertNotNull(json);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }
}