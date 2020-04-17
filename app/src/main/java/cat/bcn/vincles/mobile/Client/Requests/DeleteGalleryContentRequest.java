package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.GalleryService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteGalleryContentRequest extends BaseRequest implements Callback<ResponseBody> {

    private int contentID;
    private GalleryService galleryService;
    List<OnResponse> onResponses = new ArrayList<>();

    public DeleteGalleryContentRequest(RenewTokenFailed listener, int contentID) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        //super(BaseRequest.AUTHENTICATED_REQUEST, accesToken);
        this.contentID = contentID;
        //galleryService = retrofit.create(GalleryService.class);
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        galleryService = retrofit.create(GalleryService.class);
        Call<ResponseBody> call = galleryService.deleteContent(String.valueOf(contentID));

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
                    if (response.body() != null) {
                        r.onResponseDeleteGalleryContentRequest(contentID);
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureDeleteGalleryContentRequest(errorCode, contentID);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureDeleteGalleryContentRequest(new Exception(t), contentID);
        }
    }

    public interface OnResponse {
        void onResponseDeleteGalleryContentRequest(int contentID);
        void onFailureDeleteGalleryContentRequest(Object error, int contentID);
    }
}

