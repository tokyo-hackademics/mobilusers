/**
 * Created by huytran on 5/16/15.
 */
package jp.co.mobilusers.boardtutor.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardtutor.R;

public class BoardAdapter extends ArrayAdapter<Board>{

    public BoardAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View view;
        final Board board = getItem(i);

        if (convertView == null) {
            view = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cell_board, null);
        } else {
            view = convertView;
        }

        ((TextView)view.findViewById(R.id.name_text)).setText(board.getName());
        ((TextView)view.findViewById(R.id.members_text)).setText(TextUtils.join(",", board.getMembers()));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                RenderBoardActivity.start(board.getId());
            }
        });

        return view;
    }
}
