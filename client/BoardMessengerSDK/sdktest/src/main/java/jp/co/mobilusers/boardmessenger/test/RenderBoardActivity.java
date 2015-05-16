package jp.co.mobilusers.boardmessenger.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

        findViewById(R.id.select_stroke_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RenderBoardActivity.this)
                        .setItems(new String[]{"2", "4", "8"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int strokeWidth = 0;
                                if (i == 0) {
                                    strokeWidth = 2;
                                }
                                if (i == 1) {
                                    strokeWidth = 4;
                                }
                                if (i == 2) {
                                    strokeWidth = 8;
                                }
                                mRenderBoard.setStrokeWidthInDp(strokeWidth);
                                mRenderBoard.setEraseStrokeWidthInDp(strokeWidth * 2);
                            }
                        })
                        .show();

            }
        });

        findViewById(R.id.select_color_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RenderBoardActivity.this)
                        .setItems(new String[]{"RED", "BLUE", "BLACK"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int color = 0;
                                if (i == 0) {
                                    color = 0xff0000;
                                }
                                if (i == 1) {
                                    color = 0x0000ff;
                                }
                                if (i == 2) {
                                    color = 0x0000;
                                }
                                mRenderBoard.setColorRGB(color);
                            }
                        })
                        .show();

            }
        });
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
