package cat.bcn.vincles.mobile.Client.Requests;

import android.util.Log;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Services.GalleryService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGalleryContentsRequest extends BaseRequest implements Callback<ArrayList<GalleryContentRealm>> {

    private static final String MIMETYPES = "image/jpeg,image/jpg,video/mp4";
    private String token;

    GalleryService galleryService;
    List<OnResponse> onResponses = new ArrayList<>();
    long to;

    public GetGalleryContentsRequest(RenewTokenFailed listener, long to) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        //super(BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        //galleryService = retrofit.create(GalleryService.class);
        this.to = to;
    }

    @Override
    public void doRequest(String accessToken) {
        token = BEARER_AUTH + accessToken;
        authenticatedRequest(accessToken);
        galleryService = retrofit.create(GalleryService.class);
        Call<ArrayList<GalleryContentRealm>> call = galleryService.getMineContents(to, MIMETYPES);
        String currentToken = BEARER_AUTH + new UserPreferences().getAccessToken();

        if(token != null && currentToken.equals(BEARER_AUTH) || !token.equals(currentToken)){
            return;
        }

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
    public void onResponse(Call<ArrayList<GalleryContentRealm>> call, Response<ArrayList<GalleryContentRealm>> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    String currentToken = BEARER_AUTH + new UserPreferences().getAccessToken();

                    if(token != null && currentToken.equals(BEARER_AUTH) ||!token.equals(currentToken)){

                    }
                    else{
                        r.onResponseGetGalleryContentsRequest(response.body());
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    Log.d("ErrorgetGal", "errorCode: " + errorCode);

                    r.onFailureGetGalleryContentsRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ArrayList<GalleryContentRealm>> call, Throwable t) {

       Exception e = new Exception(t);
       if (t instanceof SocketTimeoutException){
            e = new SocketTimeoutException();
        }
        if (t instanceof ConnectException){
            e = new ConnectException();
        }

        for (OnResponse r : onResponses) {
            r.onFailureGetGalleryContentsRequest(e);
        }
    }


    public interface OnResponse {
        void onResponseGetGalleryContentsRequest(ArrayList<GalleryContentRealm> galleryContent);
        void onFailureGetGalleryContentsRequest(Object error);
    }
}
