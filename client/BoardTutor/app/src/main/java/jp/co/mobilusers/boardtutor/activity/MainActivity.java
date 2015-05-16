package jp.co.mobilusers.boardtutor.activity;

import android.app.Activity;
import android.content.Intent;

import org.androidannotations.annotations.EActivity;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardtutor.R;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.main_activity)
public class MainActivity extends Activity{

    @Override
    protected void onResume() {
        super.onResume();

        // TODO : get correct instance
        if (BoardMessenger.getInstance().needAccount()) {
            startActivity(new Intent(this, LoginActivity_.class));
        } else {
            BoardMessenger.getInstance().connect();
            //startActivity(new Intent(this, BoardListActivity.class));
        }
    }
}
