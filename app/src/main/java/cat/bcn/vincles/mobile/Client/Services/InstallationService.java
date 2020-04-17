package cat.bcn.vincles.mobile.Client.Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface InstallationService {

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/device-installations")
    public Call<JsonObject> addInstallation(@Body JsonObject installation);

    @PUT("/t/vincles-bcn.cat/vincles-services/1.0/device-installations/{id}")
    public Call<ResponseBody> updateInstallation(@Path("id") Long id, @Body JsonObject installation);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/device-installations/{id}")
    public Call<JsonObject> getInstallation(@Path("id") Long id);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/device-installations/mine")
    public Call<JsonArray> getAllInstallations(@Query("installationId") String installationId);

}
