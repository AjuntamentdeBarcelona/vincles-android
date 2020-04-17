package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.MeetingsService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteMeetingRequest extends BaseRequest implements Callback<ResponseBody> {

    MeetingsService meetingsService;
    List<OnResponse> onResponses = new ArrayList<>();
    private int idMeeting;

    public DeleteMeetingRequest(RenewTokenFailed listener, int idMeeting) {

        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idMeeting = idMeeting;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        meetingsService = retrofit.create(MeetingsService.class);
        Call<ResponseBody> call = meetingsService.deleteMeeting(idMeeting);

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
                    r.onResponseDeleteMeeting(idMeeting);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureDeleteMeeting(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureDeleteMeeting(new Exception(t));
        }
    }



    public interface OnResponse {
        void onResponseDeleteMeeting(int idMeeting);
        void onFailureDeleteMeeting(Object error);
    }
}
