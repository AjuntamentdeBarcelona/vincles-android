package cat.bcn.vincles.mobile.UI.Profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.json.JSONObject;

import cat.bcn.vincles.mobile.Client.Model.ChangePasswordResponseModel;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.ChangePasswordRequest;
import cat.bcn.vincles.mobile.Client.Requests.UpdateUserRequest;

public class ProfileEditRepository extends Fragment implements UpdateUserRequest.OnResponse, ChangePasswordRequest.OnResponse {

    public static final int STAGE_NOTHING = 0;
    public static final int STAGE_DOING_REQUEST = 1;
    public static final int STAGE_RESULT = 2;
    public static final int STAGE_ERROR = 3;

    protected Callback listener;
    BaseRequest.RenewTokenFailed renewTokenListener;

    String oldPassword, newPassword;
    String accessToken;

    Object error;
    JSONObject userRegister;
    boolean passwordResultReceived;
    int stage = STAGE_NOTHING;

    public ProfileEditRepository(){}

    public static ProfileEditRepository newInstance(Callback listener,
                                                    BaseRequest.RenewTokenFailed renewTokenListener) {
        ProfileEditRepository profileEditRepository = new ProfileEditRepository();
        profileEditRepository.setListeners(listener, renewTokenListener);
        return profileEditRepository;
    }

    public void setListeners(Callback listener, BaseRequest.RenewTokenFailed renewTokenListener) {
        this.listener = listener;
        this.renewTokenListener = renewTokenListener;
    }

    public void getPendingResults() {
        if (userRegister != null) {
            listener.onUserDataUpdated(userRegister);
            userRegister = null;
        }
        if (passwordResultReceived) {
            passwordResultReceived = false;
            listener.onPasswordUpdated();
        }

        if (stage == STAGE_ERROR) {
            listener.onError(error);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void updateUserData(String accessToken, String name, String lastname, String phone,
                               boolean livesInBarcelona, Long birthDate, String gender) {
        UserRegister userRegister = new UserRegister(name, lastname, phone,
                livesInBarcelona, birthDate, gender);

        stage = STAGE_DOING_REQUEST;
        UpdateUserRequest updateUserRequest = new UpdateUserRequest((BaseRequest.RenewTokenFailed) getActivity(), userRegister);
        updateUserRequest.addOnOnResponse(this);
        updateUserRequest.doRequest(accessToken);
    }

    public void updatePassword(String accessToken, String oldPassword, String newPassword) {
        stage = STAGE_DOING_REQUEST;
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                (BaseRequest.RenewTokenFailed) getActivity(), newPassword, oldPassword);
        changePasswordRequest.addOnOnResponse(this);
        changePasswordRequest.doRequest(accessToken);
    }

    public void updateAll(String accessToken, String name, String lastname, String phone,
                          boolean livesInBarcelona, Long birthDate, String gender,
                          String oldPassword, String newPassword) {
        this.accessToken = accessToken;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;

        updateUserData(accessToken, name, lastname, phone, livesInBarcelona, birthDate, gender);
    }

    public void onResultAcknowledged() {
        stage = STAGE_NOTHING;
        userRegister = null;
        error = null;
        passwordResultReceived = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResponseUpdateUserRequest(JSONObject userRegister) {
        if (oldPassword == null) {
            stage = STAGE_RESULT;
            if (listener != null) {
                this.userRegister = null;
                listener.onUserDataUpdated(userRegister);
            } else {
                this.userRegister = userRegister;
            }
        } else {
            if (listener != null) {
                this.userRegister = null;
                listener.onUserDataUpdated(userRegister);
            } else this.userRegister = userRegister;
            updatePassword(accessToken, oldPassword, newPassword);
        }

    }

    @Override
    public void onResponseChangePasswordRequest(ChangePasswordResponseModel responseBody) {
        stage = STAGE_RESULT;
        if (listener != null) {
            this.passwordResultReceived = false;
            if (userRegister != null) {
                listener.onUserDataUpdated(userRegister);
                userRegister = null;
            }
            listener.onPasswordUpdated();
        }
        else {
            this.passwordResultReceived = true;
        }
    }

    @Override
    public void onFailureUpdateUserRequest(Object error) {
        onError(error);
    }

    @Override
    public void onFailureChangePasswordRequest(Object error) {
        onError(error);
    }

    private void onError(Object error) {
        stage = STAGE_ERROR;
        if (listener != null) {
            if (userRegister != null) {
                listener.onUserDataUpdated(userRegister);
                userRegister = null;
            }
            listener.onError(error);
        }
        this.error = error;
    }

    public int getStage() {
        return stage;
    }

    public interface Callback {
        void onError(Object error);
        void onUserDataUpdated(JSONObject userRegister);
        void onPasswordUpdated();
    }
}
