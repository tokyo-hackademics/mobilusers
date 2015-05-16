package jp.co.mobilusers.boardmessenger.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;

import jp.co.mobilusers.boardmessenger.BoardMessenger;

public class LoginActivity extends BaseActivity {

    private EditText mUserIdEdit;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserIdEdit = (EditText) findViewById(R.id.user_id_edit);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = MblViewUtil.extractText(mUserIdEdit);
                if (MblUtils.isEmpty(userId)) {
                    MblUtils.showAlert("Error", "Please input username", null);
                    mUserIdEdit.requestFocus();
                    return;
                }

                BoardMessenger.getInstance().setAccount(userId, "dummy password");
                MblUtils.showProgressDialog("Wait...", false);
                BoardMessenger.getInstance().connect();
            }
        });

        BoardMessenger.getInstance().addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        BoardMessenger.getInstance().removeListener(mListener);
        super.onDestroy();
    }

    private BoardMessenger.Listener mListener = new BoardMessenger.Listener() {

        @Override
        public void onLoginSuccess() {
            MblUtils.hideProgressDialog();
            MblUtils.showToast("Login successfully.", Toast.LENGTH_SHORT);
            finish();
        }

        @Override
        public void onLoginError() {
            MblUtils.hideProgressDialog();
            MblUtils.showToast("Login ERROR.", Toast.LENGTH_SHORT);
        }

        @Override
        public void onDisconnect() {
            MblUtils.hideProgressDialog();
            MblUtils.showToast("Websocket is disconnected.", Toast.LENGTH_SHORT);
        }
    };

    @Override
    public void onBackPressed() {
        MblUtils.closeApp(MainActivity.class);
    }
}
