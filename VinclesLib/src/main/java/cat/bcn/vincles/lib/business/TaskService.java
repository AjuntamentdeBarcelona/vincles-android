/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TaskService {
    @GET("")
    public Call<JsonArray> getEventList(@Path("calendarId") Long id, @Query("from") String from, @Query("to") String to);

    @POST("")
    public Call<JsonObject> createTask(@Path("id") Long id, @Body JsonObject task);

    @PUT("")
    public Call<ResponseBody> updateTask(@Path("calendarId") Long calendarId, @Path("eventId") Long eventId, @Body JsonObject task);

    @GET("")
    public Call<ResponseBody> rememberTask(@Path("calendarId") Long calendarId, @Path("eventId") Long eventId);

    @DELETE("")
    public Call<ResponseBody> deleteTask(@Path("calendarId") Long calendarId, @Path("eventId") Long eventId);

    @GET("")
    public Call<JsonObject> getTask(@Path("calendarId") Long calendarId, @Path("eventId") Long eventId);

    @PUT("")
    public Call<ResponseBody> acceptTask(@Path("calendarId") Long calendarId, @Path("eventId") Long eventId, @Path("ok") boolean ok);
}