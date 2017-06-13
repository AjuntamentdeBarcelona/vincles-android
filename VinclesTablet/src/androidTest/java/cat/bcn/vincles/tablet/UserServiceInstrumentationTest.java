/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet;

import android.app.Instrumentation;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.vo.User;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(AndroidJUnit4.class)
public class UserServiceInstrumentationTest extends InstrumentationTestCase {
    private static final String TAG = "UserServiceInstrumentationTest";
    private Instrumentation instrumentation;
    private String accessToken;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instrumentation = getInstrumentation();
        accessToken = "f321497f5be58e7a8111c2edd7ebe087";
        TokenAuthenticator.username = "user1_vincles@vincles-bcn.cat";
//        TokenAuthenticator.password = "123456";
    }

    @Test
    public void testJSONtoUser() {
        String j = "{\n" +
                "      \"id\": 11,\n" +
                "      \"idInstallation\": -1,\n" +
                "      \"idCircle\": -1,\n" +
                "      \"idLibray\": -1,\n" +
                "      \"idCalendar\": -1,\n" +
                "      \"username\": \"1ds5r3bns3815\",\n" +
                "      \"name\": \"Luis\",\n" +
                "      \"lastname\": \"Jimenez\",\n" +
                "      \"photoMimeType\": \"image/png\",\n" +
                "      \"photo\": \"cGhvdG8=\",\n" +
                "      \"birthdate\": 1461838781310,\n" +
                "      \"email\": \"lj@vincles.org\",\n" +
                "      \"phone\": \"123456789\",\n" +
                "      \"gender\": \"MALE\",\n" +
                "      \"liveInBarcelona\": true\n" +
                "    }";

        JsonParser parser = new JsonParser();
        JsonObject jsonUser = parser.parse(j).getAsJsonObject();
        User user = User.fromJSON(jsonUser);
        assertEquals(11l, (long) user.getId());
    }

    @Test
    public void testGetNetworkList() throws IOException {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonArray> call = client.getUserVinclesNetworkList();
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            Gson gson = new Gson();

            List<User> items = new ArrayList<User>();
            for (JsonElement item : jsonArray) {
                JsonObject user = item.getAsJsonObject().get("username").getAsJsonObject();
                User it = User.fromJSON(user);// gson.fromJson(username, User.class);
                it.relationship = item.getAsJsonObject().get("relationship").getAsString();
                items.add(it);
            }
            assertTrue(items.size() > 0);
        } else {
            fail(result.message());
        }
    }
}
