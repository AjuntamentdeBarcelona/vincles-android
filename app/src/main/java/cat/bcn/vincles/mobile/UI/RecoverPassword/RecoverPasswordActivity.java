package cat.bcn.vincles.mobile.UI.RecoverPassword;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cat.bcn.vincles.mobile.Client.Business.ValidateFields;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Requests.RecoverPasswordRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import okhttp3.ResponseBody;

public class RecoverPasswordActivity extends BaseActivity implements View.OnClickListener, RecoverPasswordRequest.OnResponse, AlertMessage.AlertMessageInterface {

    EditText emailET;
    AlertMessage alertErrorMessage, alertInfoMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        emailET = findViewById(R.id.email);
        Button recoverPasswordBtn = findViewById(R.id.recover);

        recoverPasswordBtn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(this,
                getResources().getString(R.string.tracking_recover_password));
    }

    @Override
    public void onClick(View view) {
        String email = emailET.getText().toString();
        if (isValidData(email)) {
            RecoverPasswordRequest recoverPasswordRequest = new RecoverPasswordRequest(email);
            recoverPasswordRequest.addOnOnResponse(this);
            recoverPasswordRequest.doRequest();
        }
    }

    public boolean isValidData(String email) {
        boolean isValid = true;
        ValidateFields validateFields = new ValidateFields();
        if (!validateFields.isValididEmail(email)) {
            emailET.setError(getString(R.string.not_valid_email));
            isValid = false;
        }
        return isValid;
    }

    @Override
    public void onResponseRecoverPasswordRequest(ResponseBody responseBody) {
        alertInfoMessage = new AlertMessage(this,getResources().getString(R.string.passwordrecovery));
        alertInfoMessage.showMessage(this,getResources().getString(R.string.recover_pass_success), "");
    }

    @Override
    public void onFailureRecoverPasswordRequest(Object error) {
        alertErrorMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = ErrorHandler.getErrorByCode(this, error);
        alertErrorMessage.showMessage(this,errorMsg,"");
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        if (alertMessage.equals(alertErrorMessage)) {
            alertErrorMessage.alert.cancel();
        } else if (alertMessage.equals(alertInfoMessage)) {
            alertInfoMessage.alert.cancel();
            OtherUtils.hideKeyboard(this);
            finish();
        }
    }
}
