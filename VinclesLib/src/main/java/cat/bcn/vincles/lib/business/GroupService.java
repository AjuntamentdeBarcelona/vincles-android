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

public interface GroupService {

    @GET("")
    public Call<JsonArray> getUserGroupList();

    @GET("")
    public Call<JsonArray> getGroupUserList(@Path("id") Long id);

    @GET("")
    public Call<JsonObject> getInvitation(@Path("id") Long id);

    @GET("")
    public Call<ResponseBody> getGroupPhoto(@Path("id") Long id);

    @POST("")
    public Call<JsonObject> sendInvitation(@Path("idGroup") Long idGroup, @Body JsonObject user);

    @PUT("")
    public Call<ResponseBody> acceptInvitation(@Path("id") Long id, @Path("ok") boolean ok);

    @POST("")
    public Call<JsonObject> addUserToGroup(@Path("idGroup") Long idGroup, @Body JsonObject user);
}