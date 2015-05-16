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
import com.datdo.mobilib.util.MblSimpleImageLoader;
import com.datdo.mobilib.util.MblUtils;
import com.squareup.picasso.Picasso;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.activity.RenderBoardActivity_;

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

        ((TextView) view.findViewById(R.id.name_text)).setText(board.getName());
        ((TextView) view.findViewById(R.id.members_text)).setText(TextUtils.join(",", board.getMembers()));

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
}
