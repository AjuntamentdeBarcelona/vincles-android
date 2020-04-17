package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.GalleryService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetContentInMyGallery extends BaseRequest implements Callback<GalleryContentRealm> {

    GalleryService galleryService;
    List<OnResponse> onResponses = new ArrayList<>();
    int idContent;

    public GetContentInMyGallery(RenewTokenFailed listener, int idContent) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idContent = idContent;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        galleryService = retrofit.create(GalleryService.class);
        Call<GalleryContentRealm> call = galleryService.getContentInMyGallery(idContent);

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
    public void onResponse(Call<GalleryContentRealm> call, Response<GalleryContentRealm> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetContentInMyGallery(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    Log.d("rntk","onResponse error code:"+errorCode);
                    r.onFailureGetContentInMyGallery(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<GalleryContentRealm> call, Throwable t) {
        Log.d("rntk","on failure throwable:"+t.toString());
        for (OnResponse r : onResponses) {
            r.onFailureGetContentInMyGallery(new Exception(t));
        }
    }



    public interface OnResponse {
        void onResponseGetContentInMyGallery(GalleryContentRealm galleryContent);
        void onFailureGetContentInMyGallery(Object error);
    }
}
