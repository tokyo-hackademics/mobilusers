package jp.co.mobilusers.boardtutor.activity;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.datdo.mobilib.util.MblUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import jp.co.mobilusers.boardmessenger.render.RenderBoard;
import jp.co.mobilusers.boardtutor.R;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.activity_render_board)
public class RenderBoardActivity extends BaseActivity {
    private static final String BOARD_ID = "board_id";

    private String mBoardId;
    private RenderBoard mRenderBoard;

    @ViewById(R.id.free_mode_button)
    View freeModeButton;

    @ViewById(R.id.erase_mode_button)
    View eraseModeButton;

    @AfterViews
    void initView(){
        mBoardId = getIntent().getStringExtra(BOARD_ID);
        mRenderBoard = new RenderBoard(this, mBoardId);
        mRenderBoard.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        FrameLayout renderBoardContainer = (FrameLayout) findViewById(R.id.render_board_container);
        renderBoardContainer.addView(mRenderBoard);
        freeModeButton.performClick();
    }

    @Click(R.id.free_mode_button)
    void clickOnFreeMode(){
        mRenderBoard.setFreeDrawMode();
        onModeButtonClicked(freeModeButton);
    }

    @Click(R.id.erase_mode_button)
    void clickOnEraseMode(){
        mRenderBoard.setEraseMode();
        onModeButtonClicked(eraseModeButton);
    }

    private void onModeButtonClicked(View button) {
        ViewGroup parent = (ViewGroup) button.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof Button) {
                child.setSelected(child == button);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        MblUtils.showProgressDialog("Resuming board", false);
        mRenderBoard.resume(new RenderBoard.ResumeCallback() {
            @Override
            public void onSuccess() {
                MblUtils.hideProgressDialog();
            }

            @Override
            public void onError() {
                MblUtils.hideProgressDialog();
                MblUtils.showAlert("Error", "Failed to resume board", null);
            }
        });
    }

    public static void start(String boardId) {
        Intent intent = new Intent(MblUtils.getCurrentContext(), RenderBoardActivity_.class);
        intent.putExtra(BOARD_ID, boardId);
        MblUtils.getCurrentContext().startActivity(intent);
    }
}
