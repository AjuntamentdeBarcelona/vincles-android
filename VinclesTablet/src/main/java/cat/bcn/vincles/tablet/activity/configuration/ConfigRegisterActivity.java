/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.Security;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.BuildConfig;
import cat.bcn.vincles.tablet.R;

public class ConfigRegisterActivity extends ConfigActivity {
    private static final String TAG = "ConfigRegisterActivity";
    private EditText edtUsername;
    private EditText edtPassword;
    private View btnConfirm;
    private String suffix = "@suffix.org";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_register);

        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        edtUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (    !hasFocus &&
                        edtUsername.getText().length() > 0 &&
                        !edtUsername.getText().toString().endsWith(suffix))
                    edtUsername.setText(edtUsername.getText() + suffix);
            }
        });
    }

    public void confirm(View view) {
        final String username = edtUsername.getText().toString();
        final String password = edtPassword.getText().toString();

        btnConfirm.setEnabled(false);
        mainModel.login(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {

                // Reset password
                if (BuildConfig.DEBUG) { // Don't call 'resetPassword' in debug mode
                    // Invoke service to set current user ID
                    mainModel.getAndSetUserID(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            // Go to next screen
                            finishAffinity();
                            mainModel.startFCM(ConfigRegisterActivity.this);
                            startActivity(new Intent(ConfigRegisterActivity.this, ConfigMainActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(Object error) {
                            btnConfirm.setEnabled(true);
                            String errorMessage = mainModel.getErrorByCode(error);
                            Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }, username, password);
                } else {
                    mainModel.resetPassword(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            String newPassword = (String) result;
                            // HACK TOKEN AUTHENTICATOR TO ACCESS NEW TOKEN BEFORE SAVE USER
                            TokenAuthenticator.username = username;
                            try {
                                Security sec = new Security();
                                sec.loadPlainAESKey(TokenAuthenticator.key);
                                TokenAuthenticator.password = sec.AESencrypt(newPassword);
                            } catch (Exception e) { e.printStackTrace(); }

                            // Invoke service to set current user ID
                            mainModel.getAndSetUserID(new AsyncResponse() {
                                @Override
                                public void onSuccess(Object result) {
                                    // Go to next screen
                                    finishAffinity();
                                    mainModel.startFCM(ConfigRegisterActivity.this);
                                    startActivity(new Intent(ConfigRegisterActivity.this, ConfigMainActivity.class));
                                    finish();
                                }

                                @Override
                                public void onFailure(Object error) {
                                    btnConfirm.setEnabled(true);
                                    String errorMessage = mainModel.getErrorByCode(error);
                                    Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }, username, newPassword);
                        }

                        @Override
                        public void onFailure(Object error) {
                            btnConfirm.setEnabled(true);
                            String errorMessage = mainModel.getErrorByCode(error);
                            Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Object error) {
                btnConfirm.setEnabled(true);
                Log.i(TAG, "login() - error: " + error);
                String errorMessage = mainModel.getErrorByCode(error);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }, username, password);
    }
}
