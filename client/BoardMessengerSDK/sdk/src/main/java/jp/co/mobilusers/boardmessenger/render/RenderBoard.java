package jp.co.mobilusers.boardmessenger.render;

import android.content.Context;
import android.view.SurfaceView;

/**
 * Created by dat on 5/16/15.
 */
public class RenderBoard extends SurfaceView {

    public RenderBoard(Context context, String boardId) {
        super(context);
    }

    public void resume(final ResumeCallback callback) {
    }

    public static interface ResumeCallback {
        public void onSuccess();
        public void onError();
    }

    public void setFreeDrawMode() {
    }

    public boolean isFreeDrawMode() {
        return false;
    }

    public void setEraseMode() {
    }

    public boolean isEraseMode() {
        return false;
    }

    public void setStrokeWidthInDp(int dp) {
    }

    public void setEraseStrokeWidthInDp(int dp) {
    }

    public void setColorRGB(int rgb) {
    }
}
