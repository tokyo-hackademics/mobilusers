package jp.co.mobilusers.boardmessenger.test;

import com.datdo.mobilib.base.MblBaseApplication;
import com.datdo.mobilib.event.MblCommonEvents;
import com.datdo.mobilib.event.MblEventCenter;
import com.datdo.mobilib.event.MblEventListener;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Board;


public class Application extends MblBaseApplication implements MblEventListener {

    @Override
    public void onCreate() {
        super.onCreate();
        BoardMessenger.init(this, BuildConfig.SERVER, null);

        MblEventCenter.addListener(this, new String[] {
                MblCommonEvents.NETWORK_OFF,
                MblCommonEvents.NETWORK_ON,
                MblCommonEvents.GO_TO_BACKGROUND,
                MblCommonEvents.GO_TO_FOREGROUND
        });
    }

    @Override
    public void onVersionCodeChanged(int oldVersionCode, int newVersionCode) {

    }

    @Override
    public void onVersionNameChanged(String oldVersionName, String newVersionName) {

    }

    @Override
    public void onEvent(Object sender, String name, Object... args) {

        if (name == MblCommonEvents.NETWORK_ON || name == MblCommonEvents.GO_TO_FOREGROUND) {
            BoardMessenger.getInstance().connect();
        }

        if (name == MblCommonEvents.NETWORK_OFF || name == MblCommonEvents.GO_TO_BACKGROUND) {
            BoardMessenger.getInstance().disconnect();
        }
    }
}
