package jp.co.mobilusers.boardtutor.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.datdo.mobilib.util.MblUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import jp.co.mobilusers.boardmessenger.render.RenderBoard;
import jp.co.mobilusers.boardtutor.R;
import yuku.ambilwarna.AmbilWarnaDialog;

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


    @ViewById(R.id.select_color_button)
    View selectColorButton;

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
        //MblUtils.showProgressDialog("Resuming board", false);
        mRenderBoard.resume(new RenderBoard.ResumeCallback() {
            @Override
            public void onSuccess() {
                //      MblUtils.hideProgressDialog();
            }

            @Override
            public void onError() {
//                MblUtils.hideProgressDialog();
                MblUtils.showAlert("Error", "Failed to resume board", null);
            }
        });
    }

    public static void start(String boardId) {
        Intent intent = new Intent(MblUtils.getCurrentContext(), RenderBoardActivity_.class);
        intent.putExtra(BOARD_ID, boardId);
        MblUtils.getCurrentContext().startActivity(intent);
    }

    @Click(R.id.select_color_button)
    void selectColor() {
        int initialColor = getResources().getColor(android.R.color.holo_red_dark);

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, initialColor, true, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
               mRenderBoard.setColorRGB(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }

        });

        dialog.show();
    }

    @Click(R.id.select_stroke_button)
    void selectStroke() {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_fix_stroke, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        final SeekBar strokeSeekBar = (SeekBar) dialogView.findViewById(R.id.strokeSeek);

        strokeSeekBar.setProgress((int) mRenderBoard.getStrokeWidthInDp());
        strokeSeekBar.setMax(8);

        dialogView.findViewById(R.id.setStrokeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderBoard.setStrokeWidthInDp(strokeSeekBar.getProgress());
                mRenderBoard.setEraseStrokeWidthInDp(strokeSeekBar.getProgress() * 2);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
