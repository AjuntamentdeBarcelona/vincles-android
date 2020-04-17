package cat.bcn.vincles.mobile.Client.Services;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface CallService {

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/videoconference/start")
    Call<ResponseBody> startVideoconference(@Body JsonObject callInfo);

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/videoconference/error")
    Call<ResponseBody> errorVideoconference(@Body JsonObject callInfo);

}
