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
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatService {
    @GET("")
    public Call<JsonArray> getChatList(@Path("idChat") Long idChat, @Query("from") String from, @Query("to") String to);

    @GET("")
    public Call<JsonObject> getChat(@Path("idChat") Long idChat, @Path("idMessage") Long idMessage);

    @POST("")
    public Call<JsonObject> sendChat(@Path("idChat") Long idChat, @Body JsonObject chat);

    @GET("")
    public Call<ResponseBody> getChatResource(@Path("idChat") Long idChat, @Path("idMessage") Long idMessage);

    @PUT("")
    public Call<ResponseBody> sendChatResourceToLibrary(@Path("idChat") Long idChat, @Path("idMessage") Long idMessage);
}