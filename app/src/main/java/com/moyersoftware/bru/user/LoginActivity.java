package com.moyersoftware.bru.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.moyersoftware.bru.R;
import com.moyersoftware.bru.main.MainActivity;
import com.moyersoftware.bru.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.sign_up)
    TextView mSignUp;

    private CallbackManager mCallbackManager;
    private String mName;
    private String mPhoto;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initSignUp();
        setTransparentStatusBar();
        initFacebook();
    }

    private void initSignUp() {
        SpannableStringBuilder sb = new SpannableStringBuilder(getString
                (R.string.create_an_account));
        sb.setSpan(new UnderlineSpan(), 23, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mSignUp.setText(sb);
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * Makes status bar transparent, instead of opaque primary color.
     */
    private void setTransparentStatusBar() {
        // Check if Android version is 5.0 or higher
        if (Util.isLollipop()) {
            // Make status bar transparent
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        super.onBackPressed();
    }

    public void onLoginButtonClicked(View view) {
    }

    public void onFacebookButtonClicked(View view) {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Collections.singletonList("email"));
    }

    private void initFacebook() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject me, GraphResponse response) {
                                        if (response.getError() != null) {
                                            Util.Log("Can't retrieve Facebook info");
                                        } else {
                                            try {
                                                mName = response.getJSONObject()
                                                        .get("name").toString();
                                                mPhoto = "https://graph.facebook.com/"
                                                        + response.getJSONObject()
                                                        .get("id").toString() + "/picture?type=large";
                                                mEmail = response.getJSONObject()
                                                        .get("email").toString();
                                                Util.Log("Logged in");
                                            } catch (JSONException e) {
                                                Util.Log("Can't retrieve Facebook name: " + e
                                                        + ", " + me.toString());
                                            }
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Util.Log("Facebook error: " + exception);
                        Toast.makeText(LoginActivity.this, exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
