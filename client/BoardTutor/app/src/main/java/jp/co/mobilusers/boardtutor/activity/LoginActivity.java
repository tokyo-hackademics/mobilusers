package jp.co.mobilusers.boardtutor.activity;

import android.content.Intent;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardtutor.R;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.login_activity)
public class LoginActivity extends BaseActivity {

    @ViewById(R.id.login_button)
    LoginButton loginButton;

    CallbackManager callbackManager;

    private static final String TAG = LoginActivity.class.getName();

    @AfterInject
    void init(){
        if(!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(getApplicationContext());
        }
        if(callbackManager == null){
            callbackManager = CallbackManager.Factory.create();
        }
    }

    @AfterViews
    void initView(){
        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d(TAG, "login success with token: " + loginResult.getAccessToken().getToken());
                BoardMessenger.getInstance().setAccount(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken());
                BoardMessenger.getInstance().connect();
                // TODO : redirect to board list activity
                startActivity(new Intent(LoginActivity.this, ListBoardActivity_.class));
                finish();
            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "login cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e(TAG, "login failure");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
