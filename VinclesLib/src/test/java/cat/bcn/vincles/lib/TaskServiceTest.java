/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.business.TaskService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Task;
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
public class TaskServiceTest {
    private String accessToken;
    /*private String username = "user1_vincles@vincles-bcn.cat";
    private String password = "123456";*/
    /*private String username = "demo@vincles-bcn.cat";// idCalendar=4 id=11
    private String password = "123456";*/
    private String username = "dc6hs632or7tf@vincles-bcn.cat"; // alias=acm281 id=52
    private String password = "1pmb0c32conjt";
    private Long calendarId = 4l;
    private Long eventId = 77l;

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
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
        return result;
    }

    @Test
    public void testGetTaskList() throws IOException {
        login(username, password);
        Calendar calFrom = Calendar.getInstance();
        Calendar calTo = Calendar.getInstance();
        calTo.add(Calendar.DATE, 1);
        String dateFrom = String.valueOf(calFrom.getTime().getTime()+1);
        String dateTo = String.valueOf(calTo.getTime().getTime());

        TaskService client = ServiceGenerator.createService(TaskService.class, accessToken);
        Call<JsonArray> call = client.getEventList(calendarId, dateFrom, dateTo);
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            List<Task> items = new ArrayList<Task>();
            for (JsonElement it : jsonArray) {
                Task item = Task.fromJSON(it.getAsJsonObject());
                User userCreator = User.fromJSON(it.getAsJsonObject().get("userCreator").getAsJsonObject());
                item.owner = userCreator;
                items.add(item);
            }
            assertNotNull(jsonArray);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testCreateTask() throws IOException {
        Task item = new Task();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        item.setDate(cal.getTime());
        item.duration=1;
        item.description="test " + new Date().getTime();
        //item.owner = new User();
        item.calendarId = calendarId;

        TaskService client = ServiceGenerator.createService(TaskService.class, accessToken);
        Call<JsonObject> call = client.createTask(item.calendarId, item.toJSON());
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            Long id = result.body().get("id").getAsLong();
            assertNotNull(id);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testUpdateTask() throws IOException {
        Task item = new Task();
        item.setId(67l);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        item.setDate(cal.getTime());
        item.duration=1;
        item.description="test updated " +  new Date().getTime();
        //item.owner = new User();
        item.calendarId = calendarId;

        TaskService client = ServiceGenerator.createService(TaskService.class, accessToken);
        Call<ResponseBody> call = client.updateTask(item.calendarId, item.getId(), item.toJSON());
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testRememberTask() throws IOException {
        TaskService client = ServiceGenerator.createService(TaskService.class, accessToken);
        Call<ResponseBody> call = client.rememberTask(calendarId, eventId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testDeleteTask() throws IOException {
        TaskService client = ServiceGenerator.createService(TaskService.class, accessToken);
        Call<ResponseBody> call = client.deleteTask(calendarId, eventId);
        Response<ResponseBody> result = call.execute();
        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testGetTask() throws IOException {
        TaskService client = ServiceGenerator.createService(TaskService.class, accessToken);
        Call<JsonObject> call = client.getTask(calendarId, eventId);
        Response<JsonObject> result = call.execute();

        if (result.isSuccessful()) {
            JsonObject json = result.body();
            Task task = Task.fromJSON(json);
            assertNotNull(task);
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }

    @Test
    public void testAcceptTask() throws IOException {
        TaskService client = ServiceGenerator.createService(TaskService.class, accessToken);
        Call<ResponseBody> call = client.acceptTask(calendarId, eventId, false);
        Response<ResponseBody> result = call.execute();

        if (result.isSuccessful()) {
            assertNotNull(result.body());
        } else {
            VinclesError error = ErrorHandler.parseError(result);
            fail(error.getMessage());
        }
    }
}