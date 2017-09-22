package edu.tamu.geoinnovation.fpx.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import edu.tamu.geoinnovation.fpx.Modules.BasicServerResponse;
import edu.tamu.geoinnovation.fpx.R;
import edu.tamu.geoinnovation.fpx.RestClient;
import edu.tamu.geoinnovation.fpx.Utils.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by atharmon on 4/15/2016.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    public static final String PREFS_NAME = "fpx";
    public static final String EXTRA_KEY = "hadToLogin";
    private EditText emailEditText;
    private TextView emailErrorText;
    private EditText passwordEditText;
    private TextView passwordErrorText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        if (preferences.getString(UserInfo.USER_GUID, null) != null) {
            Log.d(TAG, preferences.getString(UserInfo.USER_GUID, "NO GUID"));
            finish();
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);

        } else {
            setContentView(R.layout.activity_login);
            emailEditText = (EditText) findViewById(R.id.activity_login_email_et);
            emailErrorText = (TextView) findViewById(R.id.textview_error_email);
            passwordEditText = (EditText) findViewById(R.id.activity_login_password_et);
            passwordErrorText = (TextView) findViewById(R.id.textview_error_password);

            emailEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    emailErrorText.setVisibility(View.INVISIBLE);
                }
            });
            passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    passwordErrorText.setVisibility(View.INVISIBLE);
                }
            });
        }

//        emailEditText.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);
//        passwordEditText.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);

    }

    public void startLogin(View v) {
        String emailText = emailEditText.getText().toString();
        String passwordText = passwordEditText.getText().toString();
        boolean emailIsValid = false;
        boolean passwordIsValid = false;

        if(emailText != null && emailText.length() > 0) {
            if (emailText.contains("@") && emailText.length() > 3) {
                // email field is good to go
                emailIsValid = true;
            } else {
                // not a valid email address
                showErrorMessageWithTextView("Invalid email address", emailErrorText);
            }

        } else {
            // tried to login with nothing in the email field
            showErrorMessageWithTextView("Please enter your email", emailErrorText);
        }

        if(passwordText != null && passwordText.length() > 2) {
            // password is good
            passwordIsValid = true;
        } else {
            showErrorMessageWithTextView("Invalid password", passwordErrorText);
        }

        if(emailIsValid && passwordIsValid) {
            loginUser(emailText, passwordText);
        }
    }

    public void showErrorMessageWithTextView(String errorMessage, TextView textView) {
        textView.setText(errorMessage);
        textView.setVisibility(View.VISIBLE);
    }

    public void loginUser(String email, String password) {
        Call<BasicServerResponse> login = RestClient.get().loginUserServer(email, password);

        login.enqueue(new Callback<BasicServerResponse>() {

            @Override
            public void onResponse(Call<BasicServerResponse> call, Response<BasicServerResponse> response) {
                if (response.body() != null) {
                    String status = response.body().Status;
                    switch (status) {
                        case "AccountNotFound":
                            showErrorMessageWithTextView("Account not found", emailErrorText);
                            break;
                        case "InputParameterMissing":
                            showErrorMessageWithTextView("Input parameter missing", passwordErrorText);
                            break;
                        case "PasswordIncorrect":
                            showErrorMessageWithTextView("Incorrect password", passwordErrorText);
                            break;
                        case "AccountNotActivated":
                            showErrorMessageWithTextView("Account not activated", emailErrorText);
                            break;
                        case "AccountDisabled":
                            showErrorMessageWithTextView("Account disabled", emailErrorText);
                            break;
                        case "Success":
                            // INTENT TO GO TO MAIN ACTIVITY HERE
                            Log.d(TAG, "Success");
                            if (response.body().userGuid != null) {
                                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString(UserInfo.USER_GUID, response.body().userGuid);
                                editor.apply();
                                finish();
                                Intent main = new Intent(LoginActivity.this, MainActivity.class);
                                main.putExtra(EXTRA_KEY, true);
                                startActivity(main);
                            }

                            break;
                        default:
                            showErrorMessageWithTextView("Unknown error", emailErrorText);
                    }
                } else {
                    Log.d(TAG, "Body is null");
                }
            }

            @Override
            public void onFailure(Call<BasicServerResponse> call, Throwable t) {
                if (t != null) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                }
            }

        });
    }

    public void signupUser(View v) {
        Intent signup = new Intent(Intent.ACTION_VIEW);
        signup.setData(Uri.parse("https://gisday.tamu.edu/Signup/"));
        try {
            startActivity(signup);
        } catch (ActivityNotFoundException e) {
//            Log.d(TAG, e.getMessage());
        }
    }

}
