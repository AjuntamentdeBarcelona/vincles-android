package cat.bcn.vincles.mobile.Client.Requests;


import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.Media;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.GalleryService;
import cat.bcn.vincles.mobile.Client.Services.GroupsService;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupMessageFileRequest extends BaseRequest implements Callback<ResponseBody> {

    GroupsService groupsService;
    List<OnResponse> onResponses = new ArrayList<>();
    int chatId, messageId;
    String contentID;
    ImageUtils imageUtils;
    Calendar c = Calendar.getInstance();
    Context context;

    public GetGroupMessageFileRequest(RenewTokenFailed listener, Context context, String contentID,
                                      int chatId, int messageId) {
        //super(BaseRequest.AUTHENTICATED_REQUEST, accesToken);
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.chatId = chatId;
        this.messageId = messageId;
        this.contentID = contentID;
        //galleryService = retrofit.create(GalleryService.class);
        imageUtils = new ImageUtils();
        this.context = context;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        groupsService = retrofit.create(GroupsService.class);
        Call<ResponseBody> call = groupsService.getMessageFile(chatId, messageId);

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
                        InputStream is = response.body().byteStream();

                        c = Calendar.getInstance();
                        long now = c.getTimeInMillis();
                        String imagePath = "";
                        String fileName = "";
                        String mimeType = response.headers().get("Content-Type");
                        switch (mimeType) {
                            case "image/jpeg":
                                fileName = contentID + ".jpeg";
                                break;
                            case "image/png":
                                fileName = contentID + ".png";
                                break;
                            case "video/mp4" :
                                fileName = contentID + ".mp4";
                                break;
                            case "audio/aac":
                                fileName = contentID + ".aac";
                                break;

                            case "audio/mp3":
                                fileName = contentID + ".mp3";
                                break;
                            case "audio/mpeg":
                                fileName = contentID + ".mp3";
                                break;
                            default:
                                fileName = contentID + ".unknown";
                                break;

                        }
                        imagePath = Media.saveFileImage(context,is,fileName);
                        Log.e("FILENAME: ", fileName);
                        r.onGetGroupMessageFileRequestResponse(messageId, imagePath, contentID, mimeType);
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onGetGroupMessageFileRequestFailure(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {

    }


    public interface OnResponse {
        void onGetGroupMessageFileRequestResponse(int messageId, String filePath, String contentID, String mimeType);
        void onGetGroupMessageFileRequestFailure(Object error);
    }


}
