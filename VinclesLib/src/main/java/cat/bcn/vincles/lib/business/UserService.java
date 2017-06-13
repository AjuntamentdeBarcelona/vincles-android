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
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserService {
    @FormUrlEncoded
    @POST("")
    public Call<JsonObject> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("")
    public Call<JsonObject> logout(@Field("token") String token);

    @POST("")
    public Call<JsonObject> register(@Body JsonObject user);

    @POST("")
    public Call<JsonObject> validateUser(@Body JsonObject validateBody);

    @POST("")
    public Call<ResponseBody> recoverPassword(@Body JsonObject recoverUser);

    @POST("")
    public Call<JsonObject> associateAnonymous(@Body JsonObject user);

    @POST("")
    public Call<JsonObject> associateRegistered(@Body JsonObject association);

    @POST("")
    public Call<JsonObject> generateCode(@Body JsonObject empty);

    @GET("")
    public Call<JsonArray> getUserVinclesNetworkList();

    @GET("")
    public Call<JsonArray> getUserMobileNetworkList();

    @GET("")
    public Call<JsonObject> getMyUserInfo();

    @DELETE("")
    public Call<ResponseBody> deleteUser(@Path("id") Long id);

    @PUT("")
    public Call<ResponseBody> updateUser(@Body JsonObject user);

    @GET("")
    public Call<JsonObject> getFullUserInfo(@Path("id") Long id);

    @GET("")
    public Call<JsonObject> getBasicUserInfo(@Path("id") Long id);

    @GET("")
    public Call<ResponseBody> getUserPhoto(@Path("id") Long id);

    @Multipart
    @POST("")
    public Call<JsonObject> updateUserPhoto(@Part MultipartBody.Part file);

    @POST("")
    public Call<JsonObject> changePassword(@Body JsonObject passwordBody);

    @PUT("")
    public Call<JsonObject> resetPassword();

    // MIGRATE OLD USERS:

    @POST("")
    public Call<ResponseBody> migrateUser(@Body JsonObject migrateBody);

    @POST("")
    public Call<ResponseBody> migrateValidateUser(@Body JsonObject validateBody);
}