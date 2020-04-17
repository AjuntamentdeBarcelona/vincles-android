package cat.bcn.vincles.mobile.UI.Profile;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONObject;

import cat.bcn.vincles.mobile.Client.Business.ValidateFields;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ProfileEditFragment extends BaseFragment implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener, AlertMessage.AlertMessageInterface,
        ProfileEditRepository.Callback {

    public static final String REPOSITORY_FRAGMENT_TAG = "repository_fragment_tag";

    private OnFragmentInteractionListener mListener;

    AlertNonDismissable alertNonDismissable;
    AlertMessage alertErrorMessage;

    UserPreferences userPreferences;
    EditText nameET, lastNameET, phoneET, newPasswordET, verifyPasswordET, oldPasswordET;
    String newName, newLastname, newPhone, newPassword, verifyNewPassword, oldPassword;
    boolean livesInBarcelona;
    RadioGroup liveInBarcelonaRdG;
    String errorString;

    boolean updateUserData, updatePassword;

    ProfileEditRepository repository;

    public ProfileEditFragment() {
        // Required empty public constructor
    }

    public static ProfileEditFragment newInstance(FragmentResumed listener) {
        ProfileEditFragment fragment = new ProfileEditFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONFIGURATION);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("newName", newName);
        outState.putString("newLastname", newLastname);
        outState.putString("newPhone", newPhone);
        outState.putString("newPassword", newPassword);
        outState.putString("verifyNewPassword", verifyNewPassword);
        outState.putString("oldPassword", oldPassword);
        outState.putBoolean("livesInBarcelona", livesInBarcelona);
        outState.putString("errorString", errorString);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPreferences = new UserPreferences(getContext());

        Fragment repo = getFragmentManager().findFragmentByTag(REPOSITORY_FRAGMENT_TAG);
        if (repo instanceof ProfileEditRepository) {
            repository = (ProfileEditRepository) repo;
        } else {
            repository = null;
            getFragmentManager().beginTransaction().remove(repo);
        }

        if (repository != null && savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(repository);
            repository = null;
        }
        if (repository == null) {
            repository = ProfileEditRepository.newInstance(this,
                    (BaseRequest.RenewTokenFailed) getActivity());
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(repository, REPOSITORY_FRAGMENT_TAG).commit();
        } else {
            repository.setListeners(this, (BaseRequest.RenewTokenFailed) getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_config_personal_data));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        nameET = v.findViewById(R.id.name);
        lastNameET = v.findViewById(R.id.lastName);
        phoneET = v.findViewById(R.id.phone);
        newPasswordET = v.findViewById(R.id.new_password_et);
        verifyPasswordET = v.findViewById(R.id.new_password_verify_et);
        oldPasswordET = v.findViewById(R.id.old_password_verify_et);
        liveInBarcelonaRdG = v.findViewById(R.id.liveInBarcelona);
        v.findViewById(R.id.save_button).setOnClickListener(this);
        v.findViewById(R.id.back).setOnClickListener(this);

        nameET.setText(userPreferences.getName());
        lastNameET.setText(userPreferences.getLastName());
        phoneET.setText(userPreferences.getPhone());

        ((TextView)v.findViewById(R.id.username)).setText(userPreferences.getUsername());
        if (userPreferences.getEmail() == null || userPreferences.getEmail().length() == 0) {
            ((TextView)v.findViewById(R.id.email)).setText("");
        } else {
            ((TextView)v.findViewById(R.id.email)).setText(userPreferences.getEmail());
        }

        livesInBarcelona = userPreferences.livesInBarcelona();

        alertNonDismissable = new AlertNonDismissable(getResources().getString(
                R.string.login_sending_data),true);

        if (savedInstanceState != null) {
            newName = savedInstanceState.getString("newName");
            newLastname = savedInstanceState.getString("newLastname");
            newPhone = savedInstanceState.getString("newPhone");
            newPassword = savedInstanceState.getString("newPassword");
            verifyNewPassword = savedInstanceState.getString("verifyNewPassword");
            oldPassword = savedInstanceState.getString("oldPassword");
            livesInBarcelona = savedInstanceState.getBoolean("livesInBarcelona");
            errorString = savedInstanceState.getString("errorString");
        }
        if (livesInBarcelona) {
            liveInBarcelonaRdG.check(R.id.yes);
        } else {
            liveInBarcelonaRdG.check(R.id.no);
        }
        liveInBarcelonaRdG.setOnCheckedChangeListener(this);
        Log.d("editorch","onCreateActivity done");

        if (errorString != null) {
            alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
            alertErrorMessage.showMessage(getActivity(), errorString, "");
        }

        if (repository.stage == ProfileEditRepository.STAGE_DOING_REQUEST) {
            alertNonDismissable.showMessage(getActivity());
        }

        repository.getPendingResults();

        return v;
    }


    @Override
    public void onClick(View v) {
        OtherUtils.hideKeyboard(getActivity());

        if (v.getId() == R.id.save_button) {
            if (isValidData(nameET.getText().toString(), lastNameET.getText().toString(),
                    phoneET.getText().toString(), newPasswordET.getText().toString(),
                    oldPasswordET.getText().toString(), verifyPasswordET.getText().toString())) {

                newName = nameET.getText().toString().equals(userPreferences.getName()) ? null :
                        nameET.getText().toString();
                newLastname = lastNameET.getText().toString().equals(userPreferences.getLastName()) ? null :
                        lastNameET.getText().toString();
                newPhone = phoneET.getText().toString().equals(userPreferences.getPhone()) ? null :
                        phoneET.getText().toString();

                if (newName == null && newLastname == null && newPhone == null
                        && livesInBarcelona == userPreferences.livesInBarcelona()) {
                    updateUserData = false;
                } else {
                    updateUserData = true;
                }

                newPassword = newPasswordET.getText().toString();
                oldPassword = oldPasswordET.getText().toString();
                if (newPassword.length() > 0) {
                    updatePassword = true;
                }

                //update data or go back if nothing to update
                if (updatePassword || updateUserData) {
                    updateDataOnServer();
                } else {
                    //no change, go to previous screen
                    if (getFragmentManager() != null) {
                        getFragmentManager().popBackStack();
                    }
                }

            }
        } else if (v.getId() == R.id.back) {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        }
    }

    public void updateDataOnServer() {
        String name = newName == null ? userPreferences.getName() : newName;
        String lastname = newLastname == null ? userPreferences.getLastName() :
                newLastname;
        String phone = newPhone == null ? userPreferences.getPhone() : newPhone;

        String accessToken = userPreferences.getAccessToken();

        String oldPassword = oldPasswordET.getText().toString();
        String newPassword = newPasswordET.getText().toString();

        Log.d("pwderr", "updateDataOnServer: userData?"+(updateUserData)+ ", updPwd:"+updatePassword);
        if (updateUserData && updatePassword) {
            repository.updateAll(accessToken, name, lastname, phone,
                    livesInBarcelona, userPreferences.getBirthdate(), userPreferences.getGender(),
                    oldPassword, newPassword);
        } else if (updateUserData) {
            repository.updateUserData(accessToken, name, lastname, phone,
                    livesInBarcelona, userPreferences.getBirthdate(), userPreferences.getGender());
        } else {
            repository.updatePassword(accessToken, oldPassword, newPassword);
        }

        alertNonDismissable.showMessage(getActivity());

    }

    public boolean isValidData(String name, String lastName, String phone, String newPassword,
                               String oldPassword, String verifyNewPassword) {
        boolean isValid = true;
        ValidateFields validateFields = new ValidateFields();
        if (!validateFields.isValididName(name)) {
            nameET.setError(getString(R.string.not_valid_name));
            isValid = false;
        } else if (!validateFields.isValididLastName(lastName)) {
            lastNameET.setError(getString(R.string.not_valid_last_name));
            isValid = false;
        } else if (!validateFields.isValidPhone(phone) ) {
            phoneET.setError(getString(R.string.error_1113));
            isValid = false;
        } else if (newPassword.length() > 0 && !newPassword.equals(verifyNewPassword)) {
            newPasswordET.setError(getString(R.string.configuration_error_different_passwords));
            isValid = false;
        } else if (newPassword.length() > 0
                && !validateFields.isValididPassword(newPassword)) {
            newPasswordET.requestFocus();
            newPasswordET.setError(getString(R.string.configuration_error_password_format_wrong));
            isValid = false;
        } else if (newPassword.length() > 0  && oldPassword.length() == 0) {
            newPasswordET.setError(null);
            oldPasswordET.requestFocus();
            oldPasswordET.setError(getString(R.string.configuration_error_missing_old_password));
            isValid = false;
        }
        return isValid;
    }

    private void processUpdateUserResponse(JSONObject userRegister) {
        if ((newName!=null && !newName.equals(userPreferences.getName()))
                || (newLastname!=null && !newLastname.equals(userPreferences.getLastName()))) {
            new UsersDb(MyApplication.getAppContext()).updateUserName(userPreferences.getUserID(),
                    newName != null ? newName : userPreferences.getName(),
                    newLastname != null ? newLastname : userPreferences.getLastName());
        }
        if (newName != null) {
            userPreferences.setName(newName);
            newName = null;
        }
        if (newLastname != null) {
            userPreferences.setLastName(newLastname);
            newLastname = null;
        }
        if (newPhone != null) {
            userPreferences.setPhone(newPhone);
            newPhone = null;
        }
        if (livesInBarcelona != userPreferences.livesInBarcelona()) {
            userPreferences.setLivesInBarcelona(livesInBarcelona);
        }

        if (mListener != null) mListener.onUserProfileChanged();
    }


    private void showError(Object error) {
        if (alertErrorMessage != null) alertErrorMessage.dismissSafely();
        alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        errorString = ErrorHandler.getErrorByCode(getContext(), error);
        alertErrorMessage.showMessage(getActivity(), errorString, "");
        Log.d("pwderr", "showError: "+errorString);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        livesInBarcelona = checkedId == R.id.yes;
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        errorString = null;
        alertMessage.dismissSafely();
        repository.onResultAcknowledged();
    }


    @Override
    public void onError(Object error) {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
        showError(error);
    }

    @Override
    public void onUserDataUpdated(JSONObject userRegister) {
        processUpdateUserResponse(userRegister);

        if (repository.getStage() == ProfileEditRepository.STAGE_RESULT) {
            if (alertNonDismissable != null && getActivity() != null && isAdded()) {
                alertNonDismissable.dismissSafely();
            }

            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }

            repository.onResultAcknowledged();
        }
    }

    @Override
    public void onPasswordUpdated() {
        OtherUtils.updateAccountPassword(userPreferences.getUsername(),
                newPasswordET.getText().toString(), AccountManager.get(getActivity()));
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }

        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }

        repository.onResultAcknowledged();
    }

    public interface OnFragmentInteractionListener {
        void onUserProfileChanged();
    }
}
