package jp.co.mobilusers.boardtutor.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.datdo.mobilib.event.MblCommonEvents;
import com.datdo.mobilib.event.MblEventCenter;
import com.datdo.mobilib.event.MblEventListener;
import com.datdo.mobilib.util.MblUtils;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.adapter.BoardAdapter;
import jp.co.mobilusers.boardtutor.auth.GoogleApi;
import jp.co.mobilusers.boardtutor.auth.SimpleCallback;

@SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
public class LoginActivity extends BaseActivity implements MblEventListener {

    private WebView         mWebView;
    private boolean mIsAuthenticating;

    @SuppressWarnings("deprecation")
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // web view
        mWebView = new WebView(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setSavePassword(false);
        setContentView(mWebView);

        // notification
        MblEventCenter.addListener(this, MblCommonEvents.NETWORK_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsAuthenticating && MblUtils.isNetworkConnected()) {
            startAuth();
        }
    }

    private void startAuth() {
        if (MblUtils.isNetworkConnected()) {
            mIsAuthenticating = true;
            GoogleApi.startOauth(mWebView, new SimpleCallback() {
                @Override
                public void onSuccess() {
                    startAuth_getGoogleContacts();
                }

                @Override
                public void onError() {
                    mIsAuthenticating = false;
                    MblUtils.showAlert(
                            R.string.error,
                            R.string.alert_google_auth_failed_due_to_access_token,
                            new Runnable() {
                                @Override
                                public void run() {
                                    startAuth();
                                }
                            });
                }
            });
        } else {
            mIsAuthenticating = false;
            MblUtils.showAlert(R.string.error, R.string.alert_google_auth_failed_due_to_network_unavailable, null);
        }
    }

    protected void startAuth_getGoogleContacts() {
        GoogleApi.getMyProfileAndContacts(new SimpleCallback() {
            @Override
            public void onSuccess() {
                mIsAuthenticating = false;
                loginBoardMessenger();
            }

            @Override
            public void onError() {
                mIsAuthenticating = false;
                MblUtils.showAlert(
                        R.string.error,
                        R.string.alert_google_auth_failed_due_to_user_info_and_contacts,
                        new Runnable() {
                            @Override
                            public void run() {
                                startAuth();
                            }
                        });
            }
        });
    }

    private void loginBoardMessenger() {
        BoardMessenger.getInstance().setAccount(
                GoogleApi.getCurrentUserId(),
                GoogleApi.getAccessToken());
        BoardMessenger.getInstance().addListener(new BoardMessenger.Listener() {
            @Override
            public void onLoginSuccess() {
                finish();
            }

            @Override
            public void onLoginError() {
                handleError();
            }

            @Override
            public void onDisconnect() {
                handleError();
            }

            void handleError() {
                new AlertDialog.Builder(LoginActivity.this)
                        .setMessage(R.string.login_error)
                        .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startAuth();
                            }
                        })
                        .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MblUtils.closeApp(MainActivity.class);
                            }
                        })
                        .show();
            }
        });
        BoardMessenger.getInstance().connect();
    }

    @Override
    public void onBackPressed() {
        // do not allow pressing back
    }

    @Override
    public void onEvent(Object sender, String name, Object... args) {
        if (name == MblCommonEvents.NETWORK_ON) {
            if (isTopActivity() && !mIsAuthenticating) {
                startAuth();
            }
        }
    }
}
