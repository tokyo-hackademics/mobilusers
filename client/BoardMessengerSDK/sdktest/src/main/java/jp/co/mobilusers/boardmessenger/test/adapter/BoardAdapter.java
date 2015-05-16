package jp.co.mobilusers.boardmessenger.test.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseAdapter;
import com.datdo.mobilib.util.MblSimpleImageLoader;
import com.datdo.mobilib.util.MblUtils;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.test.R;
import jp.co.mobilusers.boardmessenger.test.RenderBoardActivity;

public class BoardAdapter extends MblBaseAdapter<Board> {

    private MblSimpleImageLoader<Board> mImageLoader = new MblSimpleImageLoader<Board>() {
        @Override
        protected Board getItemBoundWithView(View view) {
            return (Board) view.getTag();
        }

        @Override
        protected ImageView getImageViewBoundWithView(View view) {
            return (ImageView) view.findViewById(R.id.thumbnail_image);
        }

        @Override
        protected String getItemId(Board board) {
            return board.getId();
        }

        @Override
        protected void retrieveImage(Board board, MblRetrieveImageCallback cb) {
            String path = BoardMessenger.getInstance().getBoardThumbnailPath(board.getId());
            cb.onRetrievedFile(path);
        }

        @Override
        protected void onError(ImageView imageView, Board item) {
            imageView.setImageBitmap(null);
        }
    };

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

        ((TextView)view.findViewById(R.id.name_text)).setText(board.getName());
        ((TextView)view.findViewById(R.id.members_text)).setText(TextUtils.join(",", board.getMembers()));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RenderBoardActivity.start(board.getId());
            }
        });

        mImageLoader.loadImage(view);

        return view;
    }
}
