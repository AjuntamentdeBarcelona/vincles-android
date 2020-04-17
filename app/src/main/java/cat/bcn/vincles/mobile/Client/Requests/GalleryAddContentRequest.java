package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.GalleryService;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryAddContentRequest extends BaseRequest implements Callback<JsonObject> {

    GalleryService galleryService;
    List<OnResponse> onResponses = new ArrayList<>();
    MultipartBody.Part multipartBody;

    public GalleryAddContentRequest(RenewTokenFailed listener, MultipartBody.Part multipartBody) {
        //super(BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        //galleryService = retrofit.create(GalleryService.class);
        this.multipartBody = multipartBody;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        galleryService = retrofit.create(GalleryService.class);
        Call<JsonObject> call = galleryService.addContent(multipartBody);

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
    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGalleryAddContentRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGalleryAddContentRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<JsonObject> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGalleryAddContentRequest(new Exception(t));
        }
    }


    public interface OnResponse {
        void onResponseGalleryAddContentRequest(JsonObject galleryAddedContent);
        void onFailureGalleryAddContentRequest(Object error);
    }
}
