package cat.bcn.vincles.mobile.Client.Business;

import java.util.HashMap;

public interface MediaCallbacksGallery
{
    void onSuccess(HashMap<Integer, Object> response);
    void onFailure(HashMap<Integer, Object> responsee);
}
