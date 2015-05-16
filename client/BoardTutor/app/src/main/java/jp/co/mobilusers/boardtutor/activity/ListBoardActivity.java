package jp.co.mobilusers.boardtutor.activity;

import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.datdo.mobilib.util.MblUtils;
import com.datdo.mobilib.util.MblViewUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.adapter.BoardAdapter;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.list_board_activity)
public class ListBoardActivity extends BaseActivity {

    @ViewById(R.id.boardList)
    ListView boardList;

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

    private SlidingMenu menu ;

    private void reload() {
        mAdapter.changeData(BoardMessenger.getInstance().getAllBoards());
    }

    @AfterViews
    void initView(){
        reload();
        boardList.setAdapter(mAdapter);
        setupSlidingMenu();
    }

    @Click(R.id.create_board_button)
    void createBoard() {
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

    @Click(R.id.menuBtn)
    void showMenu(){
        if(menu != null ){
            if(menu.isMenuShowing()){
                menu.showContent();
            } else {
                menu.showMenu();
            }
        }
    }

    private void setupSlidingMenu(){
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.sliding_menu);
    }

    @Override
    public void onBackPressed() {
        MblUtils.closeApp(MainActivity_.class);
    }
}
