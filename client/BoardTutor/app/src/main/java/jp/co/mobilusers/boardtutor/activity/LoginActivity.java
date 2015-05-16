package jp.co.mobilusers.boardtutor.activity;

import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;

import org.androidannotations.annotations.Click;
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
    ImageView loginButton;


    private static final String TAG = LoginActivity.class.getName();

    OnLoginListener loginListener = new OnLoginListener() {
        @Override
        public void onLogin() {
            final Session session = Session.getActiveSession();
            if (session != null && session.isOpened()) {
                // If the session is open, make an API call to get user data
                // and define a new callback to handle the response
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // If the response is successful
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                String user_ID = user.getId();//user id
                                String accessToken = session.getAccessToken();
                                Log.d(TAG, "login success with " + user_ID + " " + accessToken);
                                BoardMessenger.getInstance().setAccount(user_ID, accessToken);
                                BoardMessenger.getInstance().connect();
                                startActivity(new Intent(LoginActivity.this, ListBoardActivity_.class));
                                finish();
                            }
                        }
                    }
                });
                Request.executeBatchAsync(request);
            }
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {

        }

        @Override
        public void onThinking() {

        }

        @Override
        public void onException(Throwable throwable) {

        }

        @Override
        public void onFail(String s) {

        }
    };

    SimpleFacebook simpleFacebook;

    @Override
    protected void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Click(R.id.login_button)
    void login(){
        simpleFacebook.login(loginListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        simpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
