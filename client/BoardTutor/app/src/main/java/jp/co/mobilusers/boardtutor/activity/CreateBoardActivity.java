package jp.co.mobilusers.boardtutor.activity;

import android.content.Intent;
import android.util.Log;
import android.widget.ListView;

import com.facebook.Request;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import jp.co.mobilusers.boardtutor.R;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.create_board_layout)
public class CreateBoardActivity extends BaseActivity {

    @ViewById(R.id.friendList)
    ListView friendList;

    SimpleFacebook simpleFacebook;

    private static String TAG = CreateBoardActivity.class.getName();

    OnFriendsListener onFriendsListener = new OnFriendsListener() {
        @Override
        public void onComplete(List<Profile> response) {
            super.onComplete(response);
            Log.e(TAG, "friends count is " + response.size());
            for(Profile profile : response){
                Log.e(TAG, "data of friend :" +  profile.getFirstName() + ";" + profile.getLastName() + ";" + profile.getEmail()
                + ";" + profile.getId());
            }

        }

        @Override
        public void onException(Throwable throwable) {
            super.onException(throwable);
            Log.e(TAG, "exception " + throwable.getMessage());
        }
    };

    @AfterInject
    void initData(){
        simpleFacebook = SimpleFacebook.getInstance(this);
        simpleFacebook.getFriends(onFriendsListener);
    }

    @AfterViews
    void initView(){

    }

    @Override
    protected void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        simpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
