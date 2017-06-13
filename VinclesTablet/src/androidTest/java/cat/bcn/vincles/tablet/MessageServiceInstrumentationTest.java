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
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.business.MessageService;
import cat.bcn.vincles.lib.business.ResourceService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.tablet.model.MainModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(AndroidJUnit4.class)
public class MessageServiceInstrumentationTest extends InstrumentationTestCase {
    private static final String TAG = "MessageServiceInstrumentationTest";
    private Instrumentation instrumentation;
    private UserDAO userDAO;
    String accessToken = "1a01e5e9fb6e3f504b9694f9afda2b06";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instrumentation = getInstrumentation();
        userDAO = new UserDAOImpl();

        TokenAuthenticator.username = "5q8eh9c6ucdgm@vincles-bcn.cat";//"user1_vincles@vincles-bcn.cat";
//        TokenAuthenticator.password = "k9i9ejpabklb";//"123456";
        TokenAuthenticator.model = MainModel.getInstance();
    }

    @Test
    public void testGetMessageList() throws IOException {
        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<JsonArray> call = client.getMessageList("", "", 21l);
        Response<JsonArray> result = call.execute();
        if (result.isSuccessful()) {
            JsonArray jsonArray = result.body();
            List<Message> items = new ArrayList<Message>();
            for (JsonElement it : jsonArray) {
                Message item = Message.fromJSON(it.getAsJsonObject());
                item.userFrom = userDAO.get(item.idUserFrom);
                items.add(item);
            }
            assertNotNull(jsonArray);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testGetMessage() throws IOException {
        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<JsonObject> call = client.getMessage(6l);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            Gson gson = new Gson();
            Message item = Message.fromJSON(json);
            item.userFrom = userDAO.get(item.idUserFrom);
            assertNotNull(item);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testSendResource() throws IOException {
        final Thread thread = Thread.currentThread();

        String image = "iVBORw0KGgoAAAANSUhEUgAAADQAAAA0CAYAAADFeBvrAAAABGdBTU" +
                "EAALGPC/xhBQAAB0pJREFUaAXdWntsU1UY/9bu0a7r3ls3dYEFcESNTHTTSDQhIgsJiAa2ZIlGVNyAQKIYn" +
                "8DIZgxBEtD4ADbkHyXo5nsGwtNEI2REEKZGFliQEN3Yw0G37kHX1t/vcnfpum67bcfWcZL2ntc95/c75zvn" +
                "fOf7boTchLBz585UNDvT7XZPj4iIsCIep3bT5fF4Og0Gw3mkz5aWlrap+WP2iBiLlqqrq40dHR2PAmghAC/" +
                "BL8FsNvfExcUZIxGMRmM0+3G5XNf6Ebq6ulw9PT1m1Lcj+0sQr0lKSvqpqKjIFSqekAjt2rXrPoDfCECPx8" +
                "bGOtPS0uLwM1osFl24HA6HtLa2uvDr6u7ujgLBQ5jR8uXLl/+mqwE/lYIitH379qkY+K1or2DatGmmlJQUQ" +
                "0xMjJ/m9Wf19fVJe3u7u7GxsRdvHcBErl25cuXf+lu4XjMgQlwbEJ9yiM7zU6ZMMWZlZUUhHWifI9ZH23Lp" +
                "0iXnxYsXXWh7N9IbA1lruglBvO6BaB3OyMhIyM7ONkVFRY0ILNRCp9MpFy5c6G1ubr4KUZwHMfxDT5u6CO3" +
                "YsWMRRmtvTk5ObHp6uq539HSup05LS4unoaGhGzNVvGLFitrR3jGMVqGysnItFv73s2bNsow3GWJjn+wba/" +
                "ZzYFk/Gt4RRxtrZgOme0N+fn5UqIt+NCCjlXPTOHHihBOD+0ZJSQk3JL9hWEIUM45KXl5e7ESTGUBOUseOH" +
                "RNs7U8MJ35+CWFq78ZLdbm5uRarlQd9+AS73S5nzpxxAN9D/jaKIWuIWzOm9TA3gHAjw2GNj48XYuOOq6pY" +
                "g0Z7CCGeM5mZmYkTsQEMQjZCgth4fBCrb7VBhKgB8NDkOeNbMdzSxEisxOyNbRAhqjPUAG72oekNINg4MRK" +
                "rqoJpzWiEqGgit4DqjFYa5hEVa4GKXUGrEUKqjIom5DLMadyAR6zETI1/IFchxPsM5HE+teaBgsnyJGZeX8" +
                "iBmBUCUNsfMZlM/eFygAYymMTMuxgvmBohLKwim82m71YWSG/jVJcXS6hohRohRJbyphlw/27cmPmb4KBiX" +
                "0oYBp62kMF4vddmb+y3r1sjGZve8s6akDixkwO5cEHlQAZ57Z3UgUYZEJgZib8ZYBjy7mb9cb+Y609Jf2KS" +
                "JH73hbgtcdK6+nVxR8dI+gebxODokitPFktH8QvqwHkktXKbWI/sE6jP0v7cakn8eo80rd8szqzsgAeXFiY" +
                "YXaZHQmu1YlMgsZCC8eoVSfihRhx5c6SpbIskfrNXMiteFaftNvnvmVIxdDvE9m6Z2OcvEldKuiR/Winx+7" +
                "+VtpKXxYPuU6vek6imf8Rw7VpQOMhB4aISUuxmQbXk9VIE7ABNG7aIxxyrzFD80f3S9uJL0jl3gVIr+bNKs" +
                "fx6XOwFiyV57yfSvmyV2Bc8pZTx3cwQ1iMOWXKIM+CUZfCCFXy0P82mkGEL/clpSkPduXlag26LVQyddols" +
                "bRYjno4HHtbKuvPnaPFQIlw7XdASgptnn55dWDe+wYM15BsMPd1Klis+QSvqT0iSUIZV5dDFGeqkeVZreRw" +
                "izsw7xI0T3nTuL603U2OD+L0+azVGjoAQ7Q2d3AzOYXdwj1x9bEs9UdFydVGhpL//jrTCTuAxmyXt4y0hdd" +
                "LZ2emGtnA+En8NsCuP+4WuZdVrkg7BsG2rEE+MSTqWPC3pH24Wd5AGTBr/MSJnlVmuqqpqmT17dlow2kKww" +
                "2r55aj03Hu/uK3X11E0RG7qssVyfl+dlqe3bRr9T5482QLzlk05UCF7NW1tbeOqlCXvqVLUphgQMf1+CjP1" +
                "tjjmzA2YDEnTg4Hj5yvGFUJYUDWwITuYMV6hGecVN4fM8lfEtrVc+u68S/4t3xZU93THQIWr4cuKhoBL0s+" +
                "4T0TSkDdedyKSaV3zprQGReHGS8RM3xIdZsxVZoieM5y0B+mfuVF1csSIGRvboQHvn0JIhV5BZxPEb3IwAU" +
                "piJWasH80+pxFS3YAH6GyaLIxUrAdU7ApsjRBTdAPSc0ZnU7gHYiRWYvbGOogQfZpYS7vpOfOuFI5xYiRWX" +
                "z/sIEIEDrncSDcgPWfhSISYiI0YidUX4xBCqoP2MboBoR/51p/wNN0pxIadbZ4/Z/IQQkQMFeJPsC8+ffp0" +
                "L/f5cAnEUl9fr/hb/fmGiNMvIRbQQwaVaB3dgOFAihiIBRrBpuG8d8Q9oi2utrb2+MKFC6WpqelBnMQT5me" +
                "l6NfV1dEV+Syk5yMCHy7oulNNJre+LkIcDfpd8Tgy3h9eQDquYGbmcV0PNyve+boJ8SVaJrH33xqfxniPwi" +
                "3z8ZI3KcZVz1kZtvj5dMdAHC2pqakBf152+fJlR29vL7+rO4hmK7x1M98+R0sHJHLDNUZnE31MAFQIeS+k4" +
                "Zz2cpqYadFEfjTyqSsqHwDSKEM7Bg5H5QNA5FfzTjZwBRiuHz35Y0LItyPVo5GD/BkgonyiiWcEzUz8If8c" +
                "jTP+TnrftgJN/w9aMGN9+MHhSgAAAABJRU5ErkJggg==";

        byte[] resource = VinclesConstants.getByteFromBase64(image);
        RequestBody file = RequestBody.create(MediaType.parse("image/png"), resource);
        MultipartBody.Part data =
                MultipartBody.Part.createFormData("file", "myImage.png", file);
        ResourceService client = ServiceGenerator.createService(ResourceService.class, accessToken);

        Call<JsonObject> call = client.sendResource(data);
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            String resourceId = json.get("id").getAsString();
            assertNotNull(resourceId);
        } else {
            fail(result.message());
        }
    }

    @Test
    public void testSendMessage() throws IOException {
        Message message = new Message();
        message.idUserFrom = 8l;
        message.idUserTo = 3l;
        message.text = "Test " + new Date().getTime();
        message.metadataTipus = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE.toString();
        message.resourceTempList = new ArrayList<Resource>();
        Resource resource = new Resource();
        resource.setId(4l);
        message.resourceTempList.add(resource);

        MessageService client = ServiceGenerator.createService(MessageService.class, accessToken);
        Call<JsonObject> call = client.sendMessage(message.toJSON());
        Response<JsonObject> result = call.execute();
        if (result.isSuccessful()) {
            JsonObject json = result.body();
            String messageId = json.get("id").getAsString();
            assertNotNull(messageId);
        } else {
            fail(result.message());
        }
    }
}