package jp.co.mobilusers.boardmessenger.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.datdo.mobilib.util.MblUtils;

import java.util.List;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.render.RenderBoard;


public class RenderBoardActivity extends BaseActivity {

    private static final String BOARD_ID = "board_id";

    private String mBoardId;
    private RenderBoard mRenderBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBoardId = getIntent().getStringExtra(BOARD_ID);

        setContentView(R.layout.activity_render_board);
        mRenderBoard = new RenderBoard(this, mBoardId);
        mRenderBoard.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        FrameLayout renderBoardContainer = (FrameLayout) findViewById(R.id.render_board_container);
        renderBoardContainer.addView(mRenderBoard);

        // control panel
        final View freeModeButton = findViewById(R.id.free_mode_button);
        freeModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderBoard.setFreeDrawMode();
                onModeButtonClicked(freeModeButton);
            }
        });

        final View eraseModeButton = findViewById(R.id.erase_mode_button);
        eraseModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderBoard.setEraseMode();
                onModeButtonClicked(eraseModeButton);
            }
        });

        freeModeButton.performClick();


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
        Intent intent = new Intent(MblUtils.getCurrentContext(), RenderBoardActivity.class);
        intent.putExtra(BOARD_ID, boardId);
        MblUtils.getCurrentContext().startActivity(intent);
    }
}
