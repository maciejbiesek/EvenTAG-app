package com.example.maciej.eventag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

public class LoginActivity extends ActionBarActivity {
    CallbackManager callbackManager;
    private static final List<String> PERMISSIONS = Arrays.asList("user_friends", "email", "public_profile");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showActionBar();
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        if ( isLoggedIn() ){
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
                        Toast.makeText(LoginActivity.this, "Logowanie pomyslne!", Toast.LENGTH_SHORT).show();
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

    private void startMapActivity(){
        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
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



    // MENU

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View cView = getLayoutInflater().inflate(R.layout.custom_login_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actionBar.setCustomView(cView, layout);
    }

    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.logo: {
                Toast.makeText(this, "EvenTAG", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

}
