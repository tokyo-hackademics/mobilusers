package jp.co.mobilusers.boardtutor.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.datdo.mobilib.util.MblUtils;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;
import java.util.List;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.render.RenderBoard;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.auth.GoogleApi;
import jp.co.mobilusers.boardtutor.model.User;
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

    @ViewById(R.id.userIcon1)
    ImageView userIcon1;

    @ViewById(R.id.userIcon2)
    ImageView userIcon2;

    @ViewById(R.id.userIcon3)
    ImageView userIcon3;

    @ViewById(R.id.userIcon4)
    ImageView userIcon4;

    @ViewById(R.id.userIcon5)
    ImageView userIcon5;

    String currentUserId;

    @AfterInject
    void init(){
        currentUserId = BoardMessenger.getInstance().getAccount().getUserId();
    }

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
        showMemberIcon();
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
        strokeSeekBar.setMax(20);

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

    void showMemberIcon() {
        Board boardInfo = BoardMessenger.getInstance().getBoard(mBoardId);
        String[] memberIds = boardInfo.getMembers();
        List<User> userList = User.get(Arrays.asList(memberIds));
        final ImageView[] userIcons = {userIcon1, userIcon2, userIcon3, userIcon4, userIcon5};
        int limit = userList.size() <= 5 ? userList.size() : 5;
        for(int i = 0; i < limit; i ++) {
            if(userList.get(i).getId().equals(currentUserId)){
                continue;
            }
            String userIconPath = userList.get(i).getThumbnail();
            final int index = i;
            GoogleApi.downloadImage(userIconPath, new GoogleApi.DownloadImageCallback() {
                @Override
                public void onSuccess(String path) {
                    if (!path.startsWith("file:///")) {
                        path = "file:///" + path;
                    }
                    Picasso.with(MblUtils.getCurrentContext()).load(path).placeholder(
                            getResources().getDrawable(R.drawable.default_user_icon)
                    ).into(userIcons[index]);
                }

                @Override
                public void onError() {

                }
            });
        }
    }
}
