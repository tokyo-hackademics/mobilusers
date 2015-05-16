package jp.co.mobilusers.boardmessenger.test;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.test.adapter.BoardAdapter;

public class BoardListActivity extends BaseActivity {

    private BoardAdapter mAdapter = new BoardAdapter();
    private BoardMessenger.Listener mListener = new BoardMessenger.Listener() {

        @Override
        public void onLoginSuccess() {
            reload();
        }

        @Override
        public void onNewBoard(Board board) {
            MblUtils.showToast("New board: " + board.getName(), Toast.LENGTH_SHORT);
            reload();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);
        findViewById(R.id.create_board_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBoard();
            }
        });

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        BoardMessenger.getInstance().addListener(mListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        mAdapter.changeData(BoardMessenger.getInstance().getAllBoards());
    }

    @Override
    protected void onDestroy() {
        BoardMessenger.getInstance().removeListener(mListener);
        super.onDestroy();
    }

    private void createBoard() {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_board, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        dialogView.findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String members = MblViewUtil.extractText((EditText) dialogView.findViewById(R.id.members_edit));
                String name = MblViewUtil.extractText((EditText) dialogView.findViewById(R.id.name_edit));
                MblUtils.showProgressDialog("Wait", false);
                BoardMessenger.getInstance().createBoard(
                        members.split(","),
                        name,
                        "#ffffff",
                        3000,
                        2000,
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
    public void onBackPressed() {
        MblUtils.closeApp(MainActivity.class);
    }
}
