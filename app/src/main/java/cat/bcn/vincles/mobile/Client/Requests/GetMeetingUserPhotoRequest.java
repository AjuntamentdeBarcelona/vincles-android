package cat.bcn.vincles.mobile.Client.Requests;


import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.MeetingsService;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMeetingUserPhotoRequest extends BaseRequest implements Callback<ResponseBody> {

    MeetingsService meetingsService;
    List<OnResponse> onResponses = new ArrayList<>();
    private String userID;
    private int meetingId;
    private int viewID = 0;
    int contactType = -1;

    public GetMeetingUserPhotoRequest(RenewTokenFailed listener, int meetingId, String userID) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.userID = userID;
        this.meetingId = meetingId;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        meetingsService = retrofit.create(MeetingsService.class);
        Call<ResponseBody> call = meetingsService.getPhoto(meetingId, userID);

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
    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
        if (!shouldRenewToken(this, response)) {
            for (final OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                r.onResponseGetMeetingUserPhotoRequest(ImageUtils.saveFile(
                                        response.body().byteStream()), userID);
                            /*InputStream is = response.body().byteStream();
                            Bitmap bm = BitmapFactory.decodeStream(is);
                            r.onResponseGetUserPhotoRequest(bm, userID, viewID);*/
                            }
                        }).start();
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetMeetingUserPhotoRequest(errorCode, userID);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetMeetingUserPhotoRequest(new Exception(t), userID);
        }
    }

    public interface OnResponse {
        void onResponseGetMeetingUserPhotoRequest(Uri photo, String userID);
        void onFailureGetMeetingUserPhotoRequest(Object error, String userID);
    }

    public void setContactType(int contactType) {
        this.contactType = contactType;
    }
}