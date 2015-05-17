/**
 * Created by huytran on 5/16/15.
 */
package jp.co.mobilusers.boardtutor.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseAdapter;
import com.datdo.mobilib.util.MblUtils;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.activity.RenderBoardActivity_;
import jp.co.mobilusers.boardtutor.model.User;

public class BoardAdapter extends MblBaseAdapter<Board> {

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View view;
        final Board board = (Board) getItem(i);

        if (convertView == null) {
            view = getLayoutInflater().inflate(R.layout.cell_board, null);
        } else {
            view = convertView;
        }

        view.setTag(board);

        String [] userNameList;
        if(view.getTag(R.string.member_key) == null) {
            userNameList = getMemberNames(board.getMembers());
            view.setTag(R.string.member_key, userNameList);
        } else {
            userNameList = (String []) view.getTag(R.string.member_key);
        }

        ((TextView) view.findViewById(R.id.name_text)).setText(board.getName());
        ((TextView) view.findViewById(R.id.members_text)).setText(TextUtils.join(", ", userNameList));
        ((TextView) view.findViewById(R.id.timeAgo)).setText(getStandardTime(board.getLastActionTime()));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RenderBoardActivity_.start(board.getId());
            }
        });

        String path = BoardMessenger.getInstance().getBoardThumbnailPath(board.getId());
        if(!path.startsWith("file:///")){
            path = "file:///" + path;
        }
        Picasso.with(MblUtils.getCurrentContext()).load(path)
                .into((ImageView) view.findViewById(R.id.thumbnail_image));

        return view;
    }

    private String[] getMemberNames(String[] memberIds) {
        if(memberIds == null || memberIds.length == 0){
            return null;
        }
        String[] memberNames = new String[memberIds.length];
        List<User> userList = User.get(Arrays.asList(memberIds));
        for(int i = 0; i < memberIds.length; i++) {
            memberNames[i] = userList.get(i).getNickname();
        }
        return memberNames;
    }

    //Convert Time
    public static String getStandardTime(long timeInput){
        if(timeInput < 0) {
            return "";
        }
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - timeInput;
        if(delta >= 0 && delta < 10000){
            return delta/1000  + " seconds ago";
        }else if(delta >= 10000 && delta < 60000){
            return "about a minute ago";
        }else if(delta >= 60000 && delta<3600000){
            return delta/1000/60  + " minutes ago";
        }else if(delta >= 3600000 && delta < 7200000){
            return "a hour ago";
        }else if(delta >= 7200000 && delta < 86400000){
            return delta/1000/60/60  + " hours ago";
        }else if(delta >= 86400000 && delta < 172800000){
            return "about a day ago";
        }else if(delta >= 172800000 && delta < 2073600000){
            return delta/1000/60/60/24  + " days ago";
        }else{
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date resultdate = new Date(timeInput);
            return  sdf.format(resultdate);
        }
    }
}
