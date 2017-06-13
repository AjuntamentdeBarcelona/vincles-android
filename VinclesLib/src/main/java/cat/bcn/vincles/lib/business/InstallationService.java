/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.business;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface InstallationService {

    @POST("")
    public Call<ResponseBody> addInstallation(@Body JsonObject installation);

    @PUT("")
    public Call<ResponseBody> updateInstallation(@Body JsonObject installation);

    @GET("")
    public Call<JsonObject> getInstallation();

}
