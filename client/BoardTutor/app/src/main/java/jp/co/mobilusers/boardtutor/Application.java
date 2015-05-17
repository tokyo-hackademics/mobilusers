package jp.co.mobilusers.boardtutor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.datdo.mobilib.base.MblBaseApplication;
import com.datdo.mobilib.event.MblCommonEvents;
import com.datdo.mobilib.event.MblEventCenter;
import com.datdo.mobilib.event.MblEventListener;

import java.util.Random;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardtutor.activity.MainActivity;
import jp.co.mobilusers.boardtutor.activity.MainActivity_;
import jp.co.mobilusers.boardtutor.auth.GoogleApi;


public class Application extends MblBaseApplication implements MblEventListener {

    @Override
    public void onCreate() {
        super.onCreate();
        BoardMessenger.init(this, BuildConfig.SERVER, null);
        GoogleApi.init();

        MblEventCenter.addListener(this, new String[] {
                MblCommonEvents.NETWORK_OFF,
                MblCommonEvents.NETWORK_ON,
                MblCommonEvents.GO_TO_BACKGROUND,
                MblCommonEvents.GO_TO_FOREGROUND
        });

        addNotificationToStatusBar();
    }

    private void addNotificationToStatusBar() {

        Intent intent = new Intent(getApplicationContext(), MainActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                (new Random()).nextInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.let_draw))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis());

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationManager.notify("boardtutor", 0, builder.build());
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
