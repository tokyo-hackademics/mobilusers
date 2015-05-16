package jp.co.mobilusers.boardtutor.activity;

import android.content.Intent;

import com.facebook.FacebookSdk;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardtutor.R;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.main_activity)
public class MainActivity extends BaseActivity{

    @AfterInject
    void init(){
        if(!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(getApplicationContext());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (BoardMessenger.getInstance().needAccount()) {
            startActivity(new Intent(this, LoginActivity_.class));
        } else {
            BoardMessenger.getInstance().connect();
            startActivity(new Intent(this, ListBoardActivity_.class));
        }
    }
}
