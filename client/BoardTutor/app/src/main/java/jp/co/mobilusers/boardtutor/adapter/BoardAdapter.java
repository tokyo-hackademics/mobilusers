/**
 * Created by huytran on 5/16/15.
 */
package jp.co.mobilusers.boardtutor.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseAdapter;
import com.datdo.mobilib.util.MblUtils;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
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
}
