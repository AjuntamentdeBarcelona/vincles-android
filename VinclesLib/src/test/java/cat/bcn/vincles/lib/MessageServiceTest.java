/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.business.MessageService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MessageServiceTest {
    String accessToken;
    private String error;

    private String username = "demo11@vincles-bcn.cat";
    private String password = "123456";
    /*private String username = "demo@vincles-bcn.cat"; // idCalendar=6 id=11
    private String password = "123456";*/
    /*private String username = "67fihk4lk08jt"; // alias=acm121 id=25
    private String password = "cs1mn0a4ju8u2";*/

    private Long userFromId = 166l;
    private Long userToId = 226l;
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
    public void testGetMessage() throws IOException {
        Long messageId = 72l;

        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<JsonObject> call = client.getMessage(messageId);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            Message message = Message.fromJSON(json);
            assertNotNull(json);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testGetMessageList() throws IOException {
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<JsonArray> call = client.getMessageList(null, null, 0l);
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            List<Message> items = new ArrayList<Message>();
            for (JsonElement it : jsonArray) {
                Message item = Message.fromJSON(it.getAsJsonObject());
                items.add(item);
            }
            assertNotNull(jsonArray);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testSendMessage() throws IOException {
        Message message = new Message();
        message.idUserFrom = userFromId;
        message.idUserTo = userToId;
        message.text = "Test " + new Date().getTime();
        message.metadataTipus = VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE.toString();
        message.resourceTempList = new ArrayList<Resource>();

        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<JsonObject> call = client.sendMessage(message.toJSON());
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            String messageId = json.get("id").getAsString();
            assertNotNull(messageId);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testMarkMessage() throws IOException {
        testSendMessage();

        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<ResponseBody> call = client.markMessage(messageId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.message());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }

        testSendMessage();

        client = ServiceGenerator.createService(MessageService.class, accessToken);
        call = client.markMessage(messageId);
        result = call.execute();
        if (result.isSuccessful()) {
            Assert.fail("Not allowed!");
        } else {
            String errorCode = ErrorHandler.parseError(result).getCode();
            assertEquals("1606", errorCode);
        }
    }

    @Test
    public void testDeleteMessage() throws IOException {
        testSendMessage();

        login("ao6n2g3a2e6lo@vincles-bcn.cat", "3td9dlnm5f6as");// 3/21
        // login("9lm3a8ef3g3i7@vincles-bcn.cat", "a9bjpj8anf41v");// 3/17
        // login("9lm3a8ef3g3i7@vincles-bcn.cat", "a9bjpj8anf41v");//

        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<ResponseBody> call = client.deleteMessage(messageId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.message());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }
}