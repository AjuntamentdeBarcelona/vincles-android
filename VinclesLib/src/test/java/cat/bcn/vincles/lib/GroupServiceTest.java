/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.business.GroupService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.lib.vo.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class GroupServiceTest {
    String accessToken;
    private String error;

    /*private String username = "demo2@vincles-bcn.cat";
    private String password = "12345";*/
    private String username = "demo3@vincles-bcn.cat"; // idCalendar=4 id=11
    private String password = "123456";
    /*private String username = "m9on8p3ni6cc@vincles-bcn.cat"; // alias=demo id=162
    private String password = "1gfgcd0u961ub";*/

    private Long userId;
    private User user;

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
    public void testGetUserGroupList() throws IOException {
        GroupService client = ServiceGenerator.createService(GroupService.class, accessToken);
        Call<JsonArray> call = client.getUserGroupList();
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            List<VinclesGroup> items = new ArrayList<VinclesGroup>();
            for (JsonElement it : jsonArray) {
                JsonObject jsonGroup = it.getAsJsonObject().get("group").getAsJsonObject();
                VinclesGroup item = VinclesGroup.fromJSON(jsonGroup);

                // Add dynamizer chat associated
                item.idDynamizerChat = it.getAsJsonObject().get("idDynamizerChat").getAsLong();

                items.add(item);
            }

            assertNotNull(jsonArray);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetGroupUserList() throws IOException {
        Long groupId = 6l;
        GroupService client = ServiceGenerator.createService(GroupService.class, accessToken);
        Call<JsonArray> call = client.getGroupUserList(groupId);
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            List<User> items = new ArrayList<User>();
            for (JsonElement it : jsonArray) {
                JsonObject json = it.getAsJsonObject();
                User item = User.fromJSON(json);
                items.add(item);
            }
            assertNotNull(jsonArray);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetInvitation() throws IOException {
        Long invitationId = 2l;
        GroupService client = ServiceGenerator.createService(GroupService.class, accessToken);
        Call<JsonObject> call = client.getInvitation(invitationId);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            JsonObject jsonGroup = json.get("group").getAsJsonObject();
            VinclesGroup vinclesGroup = VinclesGroup.fromJSON(jsonGroup);

            assertEquals(vinclesGroup.getId(), (Long)jsonGroup.get("id").getAsLong());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetGroupPhoto() throws IOException {
        Long groupId = 2l;
        GroupService client = ServiceGenerator.createService(GroupService.class, accessToken);
        Call<ResponseBody> call = client.getGroupPhoto(groupId);
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

    @Test
    public void testSendInvitation() throws IOException {
        Long idGroup = 1l;
        JsonObject jsonUser = new JsonObject();
        jsonUser.addProperty("idUser", 196);
        GroupService client = ServiceGenerator.createService(GroupService.class, accessToken);
        Call<JsonObject> call = client.sendInvitation(idGroup, jsonUser);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            Long idInvitation = json.get("idInvitation").getAsLong();
            assertNotNull(idInvitation);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testAcceptInvitation() throws IOException {
        Long invitationId = 4l;
        boolean isOK = false;
        GroupService client = ServiceGenerator.createService(GroupService.class, accessToken);
        Call<ResponseBody> call = client.acceptInvitation(invitationId, isOK);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testAddUserToGroup() throws IOException {
        Long idGroup = 21l;
        User user = new User();
        user.setId(4l);
        GroupService client = ServiceGenerator.createService(GroupService.class, accessToken);
        Call<JsonObject> call = client.addUserToGroup(idGroup, user.toJSON());
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            assertNotNull(json);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    private void writeImage(byte[]data) {
        File file = new File("/opt/vincles/" + new Date().getTime() + "_group.jpg");
        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}