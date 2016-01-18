package com.example.maciej.eventag.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.maciej.eventag.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;
import java.util.List;

import static com.example.maciej.eventag.models.Constants.*;

public class LoginActivity extends ActionBarActivity {

    private CallbackManager callbackManager;
    private static final List<String> PERMISSIONS = Arrays.asList("user_friends", "email", "public_profile");
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences(KEYS, Context.MODE_PRIVATE);

        if (isLoggedIn()){
            startMapActivity();
        }

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(PERMISSIONS);

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.i("(callback): ", loginResult.toString());
                        Log.i("(callback): ", "success");
                        Toast.makeText(LoginActivity.this, getString(R.string.login_ok), Toast.LENGTH_SHORT).show();
                        startMapActivity();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.w("(callback): ", "cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.e("(callback): ", "error");
                    }
                });

    }

    private void startMapActivity() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ACCESS, AccessToken.getCurrentAccessToken().getToken());
        editor.commit();

        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private boolean isLoggedIn(){
        if ( AccessToken.getCurrentAccessToken() != null ){
            // for testing login page, change the part "!= null" to "== null"
            return true;
        } else {
            return false;
        }
    }

}
