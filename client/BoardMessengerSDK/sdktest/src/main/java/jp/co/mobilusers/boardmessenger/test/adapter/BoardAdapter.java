package jp.co.mobilusers.boardmessenger.test.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseAdapter;

import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.test.R;
import jp.co.mobilusers.boardmessenger.test.RenderBoardActivity;

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

        ((TextView)view.findViewById(R.id.name_text)).setText(board.getName());
        ((TextView)view.findViewById(R.id.members_text)).setText(TextUtils.join(",", board.getMembers()));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RenderBoardActivity.start(board.getId());
            }
        });

        return view;
    }
}
