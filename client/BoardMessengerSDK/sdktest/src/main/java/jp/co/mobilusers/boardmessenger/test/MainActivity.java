package jp.co.mobilusers.boardmessenger.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.datdo.mobilib.util.MblUtils;

import java.net.URISyntaxException;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.ws.WSClient;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (BoardMessenger.getInstance().needAccount()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            BoardMessenger.getInstance().connect();
            startActivity(new Intent(this, BoardListActivity.class));
        }
    }
}
