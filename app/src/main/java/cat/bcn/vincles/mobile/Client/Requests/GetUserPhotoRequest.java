package cat.bcn.vincles.mobile.Client.Requests;


import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserPhotoRequest extends BaseRequest implements Callback<ResponseBody> {

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    private String userID;
    private int viewID = 0;
    int contactType = -1;

    public GetUserPhotoRequest(RenewTokenFailed listener, String userID) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.userID = userID;
    }
    public GetUserPhotoRequest(RenewTokenFailed listener, String userID, int viewID) {

        this(listener, userID);
        this.viewID = viewID;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<ResponseBody> call = userService.getPhoto(userID);

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
                                long size = response.body().contentLength();
                                r.onResponseGetUserPhotoRequest(ImageUtils.saveFile(
                                        response.body().byteStream()), userID, viewID, contactType);
                            /*InputStream is = response.body().byteStream();
                            Bitmap bm = BitmapFactory.decodeStream(is);
                            r.onResponseGetUserPhotoRequest(bm, userID, viewID);*/
                            }
                        }).start();
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetUserPhotoRequest(errorCode, userID, viewID, contactType);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetUserPhotoRequest(new Exception(t), userID, viewID, contactType);
        }
    }

    public interface OnResponse {
        void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType);
        void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType);
    }

    public void setContactType(int contactType) {
        this.contactType = contactType;
    }
}