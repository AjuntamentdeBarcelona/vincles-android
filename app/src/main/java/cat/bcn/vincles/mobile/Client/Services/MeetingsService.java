package cat.bcn.vincles.mobile.Client.Services;

import java.util.ArrayList;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRestSendModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


public interface MeetingsService {

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/schedule/meeting")
    public Call<ArrayList<MeetingRest>> getMeetings(@QueryMap Map<String, String> params);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/schedule/meeting/{idMeeting}")
    public Call<MeetingRest> getMeeting(@Path("idMeeting") int idMeeting);

    @PUT("/t/vincles-bcn.cat/vincles-services/1.0/schedule/meeting/{idMeeting}")
    public Call<ResponseBody> updateMeeting(@Path("idMeeting") int idMeeting,
                                            @Body MeetingRestSendModel meeting);

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/schedule/meeting")
    public Call<ResponseBody> createMeeting(@Body MeetingRestSendModel meeting);

    @PUT("/t/vincles-bcn.cat/vincles-services/1.0/schedule/meeting/{idMeeting}/accept/{attendance}")
    public Call<ResponseBody> acceptDeclineMeeting(@Path("idMeeting") int idMeeting,
                                                   @Path("attendance") boolean attendance);

    @DELETE("/t/vincles-bcn.cat/vincles-services/1.0/schedule/meeting/{idMeeting}")
    public Call<ResponseBody> deleteMeeting(@Path("idMeeting") int idMeeting);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/schedule/meeting/{idMeeting}/guest/{userID}/photo")
    public Call<ResponseBody> getPhoto(@Path("idMeeting") int idMeeting,
                                       @Path("userID") String userID);
}
