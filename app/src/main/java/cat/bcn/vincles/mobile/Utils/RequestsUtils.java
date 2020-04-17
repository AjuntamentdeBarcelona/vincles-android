package cat.bcn.vincles.mobile.Utils;

import android.util.Log;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class RequestsUtils {

    private static RequestsUtils instance;
    public ArrayList<Call> mediaRequests = new ArrayList<retrofit2.Call>();

    public static RequestsUtils getInstance()
    {
        return initInstance();
    }

    private RequestsUtils() { }


    public static RequestsUtils initInstance()
    {
        if (instance == null)
        {
            instance = new RequestsUtils();
        }
        return instance;
    }

    public void removeCall(Call<ResponseBody> call) {
       try{
           mediaRequests.remove(call);
       }catch (Exception e){
           Log.d("startVidConf", "Error removeCall");
       }
    }


    public void addGalleryRequest(Call call) {
        mediaRequests.add(call);
    }

    public void cancelGalleryCalls() {

        for (Call call : mediaRequests){
            if (call!=null){
                call.cancel();
            }
        }
        mediaRequests.clear();
    }
}
