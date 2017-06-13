/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MessageService {
    @GET("")
    public Call<JsonArray> getMessageList(@Query("from") String from, @Query("to") String to, @Query("idUserSender") Long idUserSender);

    @GET("")
    public Call<JsonObject> getMessage(@Path("id") Long id);

    @POST("")
    public Call<JsonObject> sendMessage(@Body JsonObject message);

    @PUT("")
    public Call<ResponseBody> markMessage(@Path("id") Long id);

    @DELETE("")
    public Call<ResponseBody> deleteMessage(@Path("id") Long id);

    @GET("")
    public Call<JsonObject> getNotification(@Path("id") Long id);

    @GET("")
    public Call<JsonArray> getAllNotification(@Query("from") Long timestampFrom, @Query("to") Long timestampTo);
}