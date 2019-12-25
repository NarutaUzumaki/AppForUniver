package com.grin.appforuniver.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.grin.appforuniver.R;
import com.grin.appforuniver.data.WebServices.AuthInterface;
import com.grin.appforuniver.data.WebServices.ServiceGenerator;
import com.grin.appforuniver.data.WebServices.UserInterface;
import com.grin.appforuniver.data.model.dto.AuthenticationRequestDto;
import com.grin.appforuniver.data.model.user.User;
import com.grin.appforuniver.data.utils.PreferenceUtils;

import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public final String TAG = LoginActivity.class.getSimpleName();


    @BindView(R.id.activity_login_username_et)
    TextInputLayout usernameTIL;
    @BindView(R.id.activity_login_password_et)
    TextInputLayout passwordTIL;

    //
    // Variables for visual settings
    //
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.university_name)
    TextView universityName;
    @BindView(R.id.login_header_layout)
    LinearLayout loginHeaderActivity;

    private ProgressDialog mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceUtils.context = getApplicationContext();
        PreferenceUtils.saveUserToken(null);

        mProgressBar = new ProgressDialog(LoginActivity.this);

        if (PreferenceUtils.getUsername() != null & PreferenceUtils.getPassword() != null) {
            mProgressBar.show();
            loginUser(PreferenceUtils.getUsername(), PreferenceUtils.getPassword());
        } else {
            setContentView(R.layout.activity_login);

            ButterKnife.bind(this);
        }

        //
        // Change header visibility
        //
        final ConstraintLayout loginPageActivity = findViewById(R.id.login_activity_constraint);
        loginPageActivity.post(() -> {
            int height = loginPageActivity.getHeight();
            loginPageActivity.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                final int startHeight = loginPageActivity.getHeight();
                Log.d("ActivityHeight", "Start result = " + height);
                if ((bottom - top) == height) {
                    changeHeaderVisible("visible");
                } else {
                    changeHeaderVisible("gone");
                }
            });
        });

    }

    private void loginUser(String username, String password) {
        AuthInterface authInterface = ServiceGenerator.createService(AuthInterface.class);
        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto(username, password);

        Call<Map<Object, Object>> call = authInterface.loginUser(authenticationRequestDto);
        call.enqueue(new Callback<Map<Object, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<Object, Object>> call, @NonNull Response<Map<Object, Object>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        for (Map.Entry<Object, Object> item : response.body().entrySet()) {
                            if (item.getKey().equals("token")) {
                                PreferenceUtils.saveUserToken(item.getValue().toString());
                            }
                        }

                        if (PreferenceUtils.getUsername() == null) {
                            PreferenceUtils.saveUsername(username);
                            PreferenceUtils.savePassword(password);
                        }

                        getMe();

                    }
                } else {
                    mProgressBar.dismiss();
                    PreferenceUtils.saveUserToken(null);
                    Toasty.error(Objects.requireNonNull(getApplicationContext()), "Fail username OR acc is NOT ACTIVE", Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<Object, Object>> call, @NonNull Throwable t) {
                mProgressBar.dismiss();
                if (Objects.requireNonNull(t.getMessage()).contains("Failed to connect to /194.9.70.244:8075")) {
                    PreferenceUtils.saveUserToken(null);
                    Toasty.error(Objects.requireNonNull(getApplicationContext()), "Check your internet connection!", Toasty.LENGTH_LONG, true).show();
                }
            }
        });

    }

    private void getMe() {
        UserInterface userInterface = ServiceGenerator.createService(UserInterface.class);

        Call<User> call = userInterface.getMe();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !PreferenceUtils.getUserToken().isEmpty()) {
                        PreferenceUtils.saveUser(response.body());
                        PreferenceUtils.saveUserRoles(response.body().getRoles());

                        mProgressBar.dismiss();

                        Intent intent = new Intent(getApplicationContext(), NavigationDrawer.class);
                        startActivity(intent);

                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toasty.error(Objects.requireNonNull(getApplicationContext()), Objects.requireNonNull(t.getMessage()), Toast.LENGTH_SHORT, true).show();
                mProgressBar.dismiss();
            }
        });
    }

    private boolean validateLoginInput() {
        String loginInput = Objects.requireNonNull(usernameTIL.getEditText()).getText().toString().trim();

        if (loginInput.isEmpty()) {
            usernameTIL.setError("Field can't be empty");
            return false;
        } else {
            usernameTIL.setError(null);
            return true;
        }

    }

    private boolean validatePasswordInput() {
        String passwordInput = Objects.requireNonNull(passwordTIL.getEditText()).getText().toString().trim();

        if (passwordInput.isEmpty()) {
            passwordTIL.setError("Field can't be empty");
            return false;
        } else {
            passwordTIL.setError(null);
            return true;
        }

    }

    @OnClick(R.id.activity_login_login_btn)
    void logIn() {
        if (validateLoginInput() & validatePasswordInput()) {
            mProgressBar.setMessage("Checking...");
            mProgressBar.show();

            loginUser(Objects.requireNonNull(usernameTIL.getEditText()).getText().toString(),
                    Objects.requireNonNull(passwordTIL.getEditText()).getText().toString());


        }
    }

    // Visual settings
    private void changeHeaderVisible(String visibility) {
        switch (visibility) {
            case "gone":
                logo.setVisibility(View.GONE);
                universityName.setVisibility(View.GONE);
                break;
            case "visible":
                logo.setVisibility(View.VISIBLE);
                universityName.setVisibility(View.VISIBLE);
                break;
        }
    }

}
