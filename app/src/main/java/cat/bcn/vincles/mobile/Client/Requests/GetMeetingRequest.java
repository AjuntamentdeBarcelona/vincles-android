package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Services.MeetingsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMeetingRequest extends BaseRequest implements Callback<MeetingRest> {

    MeetingsService meetingsService;
    List<OnResponse> onResponses = new ArrayList<>();
    int meetingId;

    public GetMeetingRequest(RenewTokenFailed listener, int meetingId) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.meetingId = meetingId;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        meetingsService = retrofit.create(MeetingsService.class);
        Call<MeetingRest> call = meetingsService.getMeeting(meetingId);

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
    public void onResponse(Call<MeetingRest> call, Response<MeetingRest> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetMeetingRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetMeetingRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<MeetingRest> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetMeetingRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetMeetingRequest(MeetingRest meetingRest);
        void onFailureGetMeetingRequest(Object error);
    }
}
