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

public interface ResourceService {
    @GET("")
    public Call<ResponseBody> getResource(@Path("id") Long id);

    @GET("")
    public Call<JsonObject> getResourceInfo(@Path("id") Long id);

    @Multipart
    @POST("")
    public Call<JsonObject> sendResource(@Part MultipartBody.Part file);

    @GET("")
    public Call<JsonArray> getResourceListFromLibrary(@Query("from") String from, @Query("to") String to);

    @DELETE("")
    public Call<ResponseBody> deleteResource(@Path("id") Long id);

    @PUT("")
    public Call<ResponseBody> addResource(@Path("id") Long id);
}