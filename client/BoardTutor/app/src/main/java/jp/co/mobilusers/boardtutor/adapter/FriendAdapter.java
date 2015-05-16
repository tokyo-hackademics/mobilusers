package jp.co.mobilusers.boardtutor.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseAdapter;
import com.datdo.mobilib.util.MblUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.model.User;

/**
 * Created by huytran on 5/16/15.
 */
public class FriendAdapter extends MblBaseAdapter<User> {

    private ArrayList<User> invitedUser  = new ArrayList<>();



    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view;

        final User user = (User) getItem(i);

        if (convertView == null) {
            view = getLayoutInflater().inflate(R.layout.friend_list_item, null);
        } else {
            view = convertView;
        }

        Log.e("FRIEND ADAPTER", user.getNickname());


        ((TextView)view.findViewById(R.id.userName)).setText(user.getNickname());

        ((CheckBox)view.findViewById(R.id.checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !invitedUser.contains(user)) {
                    invitedUser.add(user);
                } else if (!b && invitedUser.contains(user)) {
                    invitedUser.remove(user);
                }
            }
        });

        if(!user.getThumbnail().isEmpty()) {
            Picasso.with(MblUtils.getCurrentContext()).load(user.getThumbnail()).into((ImageView) view.findViewById(R.id.userIcon));
        }
        return view;
    }

    public ArrayList<User> getInvitedUserList(){
        return invitedUser;
    }


}
