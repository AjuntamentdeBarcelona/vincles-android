package cat.bcn.vincles.mobile.Client.Requests;


import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.GroupsService;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupPhotoRequest extends BaseRequest implements Callback<ResponseBody> {

    GroupsService groupsService;
    List<OnResponse> onResponses = new ArrayList<>();
    private String groupId;
    private int viewID = 0;

    public GetGroupPhotoRequest(RenewTokenFailed listener, String groupId) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.groupId = groupId;
    }
    public GetGroupPhotoRequest(RenewTokenFailed listener, String userID, int viewID) {
        this(listener, userID);
        this.viewID = viewID;
    }

    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        groupsService = retrofit.create(GroupsService.class);
        Call<ResponseBody> call = groupsService.getGroupPhoto(groupId);

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
                                r.onResponseGetGroupPhotoRequest(ImageUtils.saveFile(response.body().byteStream()), groupId, viewID);
                            /*InputStream is = response.body().byteStream();
                            Bitmap bm = BitmapFactory.decodeStream(is);
                            r.onResponseGetUserPhotoRequest(bm, userID, viewID);*/
                            }
                        }).start();
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGroupPhotoRequest(errorCode, groupId);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetGroupPhotoRequest(new Exception(t), groupId);
        }
    }

    public interface OnResponse {
        void onResponseGetGroupPhotoRequest(Uri photo, String userID, int viewID);
        void onFailureGetGroupPhotoRequest(Object error, String userID);
    }

}