package jp.co.mobilusers.boardtutor.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.adapter.FriendAdapter;
import jp.co.mobilusers.boardtutor.model.User;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.create_board_layout)
public class CreateBoardActivity extends BaseActivity {

    @ViewById(R.id.friendList)
    ListView friendList;

    FriendAdapter friendAdapter;

    SimpleFacebook simpleFacebook;

    private static String TAG = CreateBoardActivity.class.getName();

    BoardSize[] boardSizes = new BoardSize[3];

    BoardSize boardSize;

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
//        simpleFacebook = SimpleFacebook.getInstance(this);
//        simpleFacebook.getFriends(onFriendsListener);
        boardSizes[0] = new BoardSize(1500, 1000);
        boardSizes[1] = new BoardSize(3000, 2000);
        boardSizes[2] = new BoardSize(6000, 4000);
        boardSize = boardSizes[1];
    }

    @AfterViews
    void initView(){
        friendAdapter = new FriendAdapter();

        friendList.setAdapter(friendAdapter);

        // TODO : show correct user data
        User.getAllUserIds();
        BoardMessenger.getInstance().checkUsers(User.getAllUserIds(), new BoardMessenger.CheckUsersCallback() {
            @Override
            public void onSuccess(List<String> availableUserIds) {
                List<User> userList = User.get(availableUserIds);
                friendAdapter.changeData(userList);
            }

            @Override
            public void onError() {

            }
        });

//        ArrayList<User> users = new ArrayList<>();
//        for(int i = 0; i < 10; i++){
//            User testUser = new User();
//            testUser.setNickname("hoge " + i);
//            testUser.setThumbnail("");
//            users.add(testUser);
//        }
//
//        friendAdapter.changeData(users);
    }

    @Click(R.id.inviteBtn)
    void createBoard(){
        final String[] members = getListInvitedFriendId();
        if(members == null || members.length == 0){
            MblUtils.showAlert("Error", "You must invite at least one person !!!", null);
            return;
        }

        int initialColor = getResources().getColor(android.R.color.white);

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, initialColor, true, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                showCreateBoardDialog(members, color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }

        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(this);
    }

    String[] getListInvitedFriendId(){
        return getListInvitedFriendContribute(true);
    }

    String[] getListInvitedFriendName() {
        return getListInvitedFriendContribute(false);
    }

    String[] getListInvitedFriendContribute(boolean getId) {
        ArrayList<User> invitedUsers = friendAdapter.getInvitedUserList();
        if(invitedUsers == null || invitedUsers.size() == 0) {
            return null;
        }
        String[] invitedMember = new String[invitedUsers.size()];
        for(int i = 0; i < invitedUsers.size(); i ++){
            invitedMember[i] = getId ? invitedUsers.get(i).getId() : invitedUsers.get(i).getNickname();
        }
        return invitedMember;
    }

    private void showCreateBoardDialog(final String [] members, final int backgroundColor){
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_board, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        dialogView.findViewById(R.id.backgroundView).setBackgroundColor(backgroundColor);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.boardSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.boardSize, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setSelection(1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boardSize = boardSizes[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialogView.findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = MblViewUtil.extractText((EditText) dialogView.findViewById(R.id.name_edit));
                MblUtils.showProgressDialog("Wait", false);
                BoardMessenger.getInstance().createBoard(
                        members,
                        name ,
                        String.format("#%06X", (0xFFFFFF & backgroundColor)),
                        boardSize.width,
                        boardSize.height,
                        null,
                        new BoardMessenger.SimpleCallback() {

                            @Override
                            public void onSuccess() {
                                MblUtils.hideProgressDialog();
                                dialog.dismiss();
                            }

                            @Override
                            public void onError() {
                                MblUtils.hideProgressDialog();
                                MblUtils.showAlert("Error", "Failed to create board.", null);
                            }
                        });
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        simpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    class BoardSize {
        int width;
        int height;

        public BoardSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
