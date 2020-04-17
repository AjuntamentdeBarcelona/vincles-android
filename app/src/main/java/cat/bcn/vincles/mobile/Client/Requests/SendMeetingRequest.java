package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.MeetingRestSendModel;
import cat.bcn.vincles.mobile.Client.Services.MeetingsService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMeetingRequest extends BaseRequest implements Callback<ResponseBody> {

    public static final int CREATE_MEETING = -1;

    MeetingsService meetingsService;
    List<OnResponse> onResponses = new ArrayList<>();
    private int idMeeting;
    private MeetingRestSendModel meeting;
    boolean isCreate;

    public SendMeetingRequest(RenewTokenFailed listener, int idMeeting,
                              MeetingRestSendModel meeting) {

        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idMeeting = idMeeting;
        this.meeting = meeting;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        meetingsService = retrofit.create(MeetingsService.class);
        isCreate = idMeeting == CREATE_MEETING;
        Call<ResponseBody> call = !isCreate ?
                meetingsService.updateMeeting(idMeeting, meeting)
                : meetingsService.createMeeting(meeting);

        try{
            ((String[])call.request().tag())[0] = this.getClass().getSimpleName();
        }catch (Exception e){
            Log.e("TAG", this.getClass().getSimpleName() + " Put request Tag error");
        }

        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    if (isCreate) r.onResponseCreateMeeting(response);
                    else r.onResponseUpdateMeeting();
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureSendMeeting(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureSendMeeting(new Exception(t));
        }
    }



    public interface OnResponse {
        void onResponseCreateMeeting(Response<ResponseBody> response);
        void onResponseUpdateMeeting();
        void onFailureSendMeeting(Object error);
    }
}
