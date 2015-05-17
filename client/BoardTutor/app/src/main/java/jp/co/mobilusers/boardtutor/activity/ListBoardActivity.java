package jp.co.mobilusers.boardtutor.activity;

import android.content.Intent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.datdo.mobilib.util.MblUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.adapter.BoardAdapter;
import jp.co.mobilusers.boardtutor.model.User;

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

        @Override
        public void onNewAction(Action action) {
            super.onNewAction(action);
            reload();
        }
    };

    private SlidingMenu menu ;

    private void reload() {
        mAdapter.changeData(BoardMessenger.getInstance().getAllBoards());
    }

    @AfterViews
    void initView(){
        boardList.setAdapter(mAdapter);
        setupSlidingMenu();
        BoardMessenger.getInstance().addListener(mListener);
    }

    private void createBoard() {
        startActivity(new Intent(this, CreateBoardActivity_.class));
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
        menu.setMode(SlidingMenu.RIGHT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.sliding_menu);
        Button createBoardBtn = (Button) menu.findViewById(R.id.create_board_button);
        createBoardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBoard();
            }
        });

        Button logoutButton = (Button) menu.findViewById(R.id.logout_btn);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BoardMessenger.getInstance().logout();
                User.clearData();
                CookieSyncManager.createInstance(getApplicationContext()).sync();
                CookieManager.getInstance().removeAllCookie();
                finish();
                startActivity(new Intent(ListBoardActivity.this, MainActivity_.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
        if(menu != null && menu.isMenuShowing()){
            menu.showContent();
        }
    }

    @Override
    public void onBackPressed() {
        if(menu != null ){
            if(menu.isMenuShowing()){
                menu.showContent();
            } else {
                MblUtils.closeApp(MainActivity_.class);
            }
        } else {
            MblUtils.closeApp(MainActivity_.class);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
