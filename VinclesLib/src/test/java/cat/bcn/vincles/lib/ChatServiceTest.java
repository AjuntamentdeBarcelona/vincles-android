/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.lib.business.ChatService;
import cat.bcn.vincles.lib.business.MessageService;
import cat.bcn.vincles.lib.business.ResourceService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ChatServiceTest {
    String accessToken;
    private String error;

    /*private String username = "demo2@vincles-bcn.cat";
    private String password = "123456";*/
    /*private String username = "demo10@vincles-bcn.cat"; // idCalendar=6 id=11
    private String password = "123456";*/
    /*private String username = "demo9@vincles-bcn.cat";
    private String password = "123456";*/
    /*private String username = "m9on8p3ni6cc@vincles-bcn.cat"; // alias=demo id=162
    private String password = "1gfgcd0u961ub";*/
    private String username = "demo3@vincles-bcn.cat";
    private String password = "123456";

    private Long chatId = 10l;
    private Long messageId = 1l;
    private Long idContent;

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
    public void testGetChat() throws IOException {
        ChatService client = ServiceGenerator.createService(ChatService.class, accessToken);
        Call<JsonObject> call = client.getChat(chatId, messageId);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            Message chat = Message.fromJSON(json);
            assertNotNull(json);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetChatList() throws IOException {
        ChatService client = ServiceGenerator.createService(ChatService.class, accessToken);
        Call<JsonArray> call = client.getChatList(chatId, "", "");
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            List<Chat> items = new ArrayList<Chat>();
            for (JsonElement it : jsonArray) {
                Chat item = Chat.fromJSON(it.getAsJsonObject());
                items.add(item);
            }
            assertNotNull(jsonArray);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testSendResource() throws IOException {
        String nameFile = "resource";
        File imageFile = new File("/opt/vincles/" + nameFile + VinclesConstants.IMAGE_EXTENSION);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part data = MultipartBody.Part.createFormData("file", nameFile, file);

        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);
        Call<JsonObject> call = client.sendResource(data);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            idContent = json.get("id").getAsLong();
            assertNotNull(json);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testSendChat() throws IOException {
        Message chat = new Message();
        chat.text = "Test " + VinclesConstants.getDateString(new Date(), "dd/MM/yyyy 'a las' HH:mm 'h.'", new Locale("es","ES"));

        // Text
        //chat.metadataTipus = VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE.toString();

        // Image
        testSendResource();
        chat.metadataTipus = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE.toString();
        chat.idContent = idContent;

        ChatService client = ServiceGenerator.createService(ChatService.class, accessToken);
        Call<JsonObject> call = client.sendChat(chatId, chat.toJSON());
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            Long id = json.get("id").getAsLong();
            messageId = id;
            assertNotNull(id);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetChatResource() throws IOException {
        Long messageId = 464l;
        ChatService client = ServiceGenerator.createService(ChatService.class, accessToken);
        Call<ResponseBody> call = client.getChatResource(chatId, messageId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            byte[] data = IOUtils.toByteArray(result.body().byteStream());
            assertNotNull(data);
            writeImage(data);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testSendChatResourceToLibrary() throws IOException {
        testSendChat();

        ChatService client = ServiceGenerator.createService(ChatService.class, accessToken);
        Call<ResponseBody> call = client.sendChatResourceToLibrary(chatId, messageId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    private void writeImage(byte[]data) {
        File file = new File("/opt/vincles/" + new Date().getTime() + "_chat.jpg");
        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}