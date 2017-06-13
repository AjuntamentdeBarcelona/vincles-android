/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonObject;
import java.io.IOException;
import cat.bcn.vincles.lib.business.CallService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoModel {
    private static final String TAG = "VideoModel";
    protected MainModel mainModel = MainModel.getInstance();
    private boolean initialized;
    private static VideoModel instance;
    
    public VideoModel() {
    }

    public static VideoModel getInstance() {
        if (instance == null) {
            instance = new VideoModel();
            instance.initialize();
        }
        return instance;
    }

    public void initialize() {
        if (!initialized) {
            initialized = true;
        }
    }

    public void startVideoConference(final AsyncResponse response, long idUser, String idRoom) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("idUser", idUser);
        json.addProperty("idRoom", idRoom);

        CallService client = ServiceGenerator.createService(CallService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.startVideoConference(json);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                // Update local 'Task'
                if (result.isSuccessful()) {
                    response.onSuccess(true);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "startVideoConference() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }
}