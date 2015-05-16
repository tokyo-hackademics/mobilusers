package jp.co.mobilusers.boardmessenger.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.List;

import jp.co.mobilusers.boardmessenger.BoardMessenger;
import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.model.Board;


public class RenderBoard extends SurfaceView {

    public static enum Mode {
        FREE("free"),
        LINE("line"),
        OVAL("oval"),
        ERASE("erase"),
        SCALE("scale"),
        DOUBLE("double"),
        SINGLE("single");

        String type;
        Mode(String type) {
            this.type = type;
        }

        boolean isType(Action a) {
            return TextUtils.equals(a.getType(), type);
        }
    }

    private static final String TAG         = RenderBoard.class.getSimpleName();
    private static final long   TAPZOOM_ANIMATE_DURATION        = 250;  // duration when zoom in/out by tapping single/double
    private static final float  THUMBNAIL_RATIO                 = 0.3f; // thumbnailWidth / canvasWidth
    private static final float  THUMBNAIL_TRANSLATE_MULTIPLY    = 4f;   // amplify dragging on thumbnail
    private static final long   TRANSFORM_DELAY                 = 200;  // delay after actions like span or drag

    private static final String MINOR_SEPARATOR = ",";
    private static final String MAJOR_SEPARATOR = "#";

    private HandlerThread   mHandlerThread;
    private Handler         mHandler;
    private Handler         mMainThreaHandler;
    private boolean         mRunning;

    private Board           mBoard;
    private BoardMessenger  mBoardMessenger;
    private String          mCurrentUserId;

    private float           mDensity;

    private int             mPrevX;
    private int             mPrevY;
    private long            mDownAt;
    private Matrix          mBitmapToCanvasMatrix = new Matrix();   // map from RenderBitmap to Canvas
    private Matrix          mCanvasToBitmapMatrix = new Matrix();   // map from Canvas to RenderBitmap
    private Matrix          mBitmapToThumbnailMatrix;               // map from RenderBitmap to Thumbnail (on Canvas)
    private Matrix          mThumbnailToBitmapMatrix;               // map from Thumbnail (on Canvas) to RenderBitmap
    private RectF           mThumbnailRect;                         // rect of thumbnail on Canvas
    private int             mBackgroundColor;
    private Paint           mPaint;
    private Paint           mErasePaint;
    private Paint           mEraseIconPaint;
    private Paint           mThumbnailBorderPaint;
    private Paint           mThumbnailPaint;
    private Paint           mViewPortBorderPaint;
    private Bitmap          mRenderBitmap;
    private Canvas          mRenderCanvas;

    private ScaleGestureDetector    mScaleGestureDetector;
    private GestureDetector         mGestureDetector;
    private GestureDetector         mThumbnailGestureDetector;

    private Mode            mMode = Mode.FREE;

    private long            mLatestActionTo;
    private long            mLastReloadAt;

    // for FREE mode
    private List<Integer>   mFreeModeCoordinates = new ArrayList<Integer>();

    private BoardMessenger.Listener mBMListener = new BoardMessenger.Listener() {
        @Override
        public void onNewAction(Action action) {
            if (!TextUtils.equals(action.getBoardId(), mBoard.getId())) {
                return;
            }
            if (TextUtils.equals(action.getSender(), mCurrentUserId)) {
                return;
            }
            render(action, true, null);
        }
    };

    public RenderBoard(Context context, String boardId) {
        super(context);

        mBoardMessenger = BoardMessenger.getInstance();
        mBoard = mBoardMessenger.getBoard(boardId);
        mCurrentUserId = mBoardMessenger.getAccount().getUserId();

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mMainThreaHandler = new Handler(Looper.getMainLooper());

        mDensity = context.getResources().getDisplayMetrics().density;

        // get background color
        mBackgroundColor = 0xffffffff;
        int reversedBackgroundColor = 0xff000000;
        if (mBoard.getBackground() != null) {
            if (mBoard.getBackground().matches("^#[0-9a-f]{6}$")) {
                mBackgroundColor = Integer.parseInt(mBoard.getBackground().replace("#", ""), 16);
            }
            if (mBoard.getBackground().matches("^#[0-9a-f]{8}$")) {
                mBackgroundColor = Integer.parseInt(mBoard.getBackground().replace("#", "").substring(2), 16);
            }
            reversedBackgroundColor = 0xffffffff - mBackgroundColor;
            mBackgroundColor += 0xff000000;
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(reversedBackgroundColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(pxFromDp(5));

        mErasePaint = new Paint();
        mErasePaint.setAntiAlias(true);
        mErasePaint.setColor(mBackgroundColor);
        mErasePaint.setStyle(Paint.Style.STROKE);
        mErasePaint.setStrokeWidth(pxFromDp(20));

        mEraseIconPaint = new Paint();
        mEraseIconPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mEraseIconPaint.setColor(Color.RED);

        mThumbnailBorderPaint = new Paint();
        mThumbnailBorderPaint.setStyle(Paint.Style.STROKE);
        mThumbnailBorderPaint.setStrokeWidth(pxFromDp(1));
        mThumbnailBorderPaint.setColor(reversedBackgroundColor);

        mThumbnailPaint = new Paint();
        mThumbnailPaint.setAlpha(0x88);

        mViewPortBorderPaint = new Paint();
        mViewPortBorderPaint.setStyle(Paint.Style.STROKE);
        mViewPortBorderPaint.setStrokeWidth(pxFromDp(1));
        mViewPortBorderPaint.setColor(Color.RED);

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {

            Mode preservedMode;

            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

                float scaleFactor = scaleGestureDetector.getScaleFactor();
                mBitmapToCanvasMatrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                mBitmapToCanvasMatrix.invert(mCanvasToBitmapMatrix);

                render();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                preservedMode = mMode;
                mMode = Mode.SCALE;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
                mMainThreaHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMode = preservedMode;
                    }
                }, TRANSFORM_DELAY);
            }
        });

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            Mode preservedMode;

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                preservedMode = mMode;
                mMode = Mode.SINGLE;
                mMainThreaHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMode = preservedMode;
                    }
                }, TAPZOOM_ANIMATE_DURATION);

                animateToZoomFocusedPoint(e.getX(), e.getY(), 0.5f);

                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {

                preservedMode = mMode;
                mMode = Mode.DOUBLE;
                mMainThreaHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMode = preservedMode;
                    }
                }, TAPZOOM_ANIMATE_DURATION);

                animateToZoomFocusedPoint(e.getX(), e.getY(), 2f);

                return true;
            }
        });

        mThumbnailGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                float[] p = new float[] {
                        e.getX(),
                        e.getY()
                };
                mThumbnailToBitmapMatrix.mapPoints(p);
                mBitmapToCanvasMatrix.mapPoints(p);

                mBitmapToCanvasMatrix.postTranslate(getWidth()/2 - p[0], getHeight()/2 - p[1]);
                mBitmapToCanvasMatrix.invert(mCanvasToBitmapMatrix);

                render();

                return true;
            }
        });

        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {}

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (Build.VERSION.SDK_INT < 18) {
                    mHandlerThread.quit();
                } else {
                    mHandlerThread.quitSafely();
                }
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBoardMessenger.addListener(mBMListener);
        mRunning = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        mRunning = false;
        mBoardMessenger.removeListener(mBMListener);
        if (mRenderBitmap != null) {
            mRenderBitmap.recycle();
            mRenderBitmap = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mThumbnailRect.contains(event.getX(), event.getY())) {

            if (mThumbnailGestureDetector.onTouchEvent(event)) {
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPrevX = Math.round(event.getX());
                mPrevY = Math.round(event.getY());
            } else if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_CANCEL
                    || event.getAction() == MotionEvent.ACTION_UP) {

                int x = Math.round(event.getX());
                int y = Math.round(event.getY());

                mBitmapToCanvasMatrix.postTranslate(
                        THUMBNAIL_TRANSLATE_MULTIPLY * (mPrevX - x),
                        THUMBNAIL_TRANSLATE_MULTIPLY * (mPrevY - y));
                mBitmapToCanvasMatrix.invert(mCanvasToBitmapMatrix);
                mPrevX = x;
                mPrevY = y;

                render();
            }

            return true;
        }

        mScaleGestureDetector.onTouchEvent(event);

        mGestureDetector.onTouchEvent(event);

        if (mMode == Mode.FREE || mMode == Mode.ERASE) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                mDownAt = System.currentTimeMillis();
                mFreeModeCoordinates.clear();

                float[] point = new float[] {
                        event.getX(),
                        event.getY()
                };
                mCanvasToBitmapMatrix.mapPoints(point);
                mFreeModeCoordinates.add(Math.round(point[0]));
                mFreeModeCoordinates.add(Math.round(point[1]));

            } else if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_CANCEL
                    || event.getAction() == MotionEvent.ACTION_UP) {

                float[] point = new float[] {
                        event.getX(),
                        event.getY()
                };
                mCanvasToBitmapMatrix.mapPoints(point);
                mFreeModeCoordinates.add(Math.round(point[0]));
                mFreeModeCoordinates.add(Math.round(point[1]));

                if (mFreeModeCoordinates.size() < 4) {
                    return true;
                }

                Action action = new Action();
                action.setType(mMode.type);
                int s = mFreeModeCoordinates.size() >= 6 ? 6 : 4;
                List<Integer> subCoordinates = mFreeModeCoordinates.subList(
                        mFreeModeCoordinates.size()-s,
                        mFreeModeCoordinates.size());
                Paint paint = mMode == Mode.ERASE ? mErasePaint : mPaint;
                action.setData(generateActionData(mMode.type, paint, subCoordinates));
                ExtraRenderer extraRenderer = null;
                if (mMode == Mode.ERASE && event.getAction() == MotionEvent.ACTION_MOVE) {
                    final RectF rect = new RectF(
                            point[0] - mErasePaint.getStrokeWidth()/2,
                            point[1] - mErasePaint.getStrokeWidth()/2,
                            point[0] + mErasePaint.getStrokeWidth()/2,
                            point[1] + mErasePaint.getStrokeWidth()/2);
                    mBitmapToCanvasMatrix.mapRect(rect);
                    extraRenderer = new ExtraRenderer() {
                        @Override
                        public void render(Canvas canvas) {
                            canvas.drawRect(rect, mEraseIconPaint);
                        }
                    };
                }
                render(action, false, extraRenderer);

                if (event.getAction() != MotionEvent.ACTION_MOVE) {
                    String data = generateActionData(mMode.type, paint, mFreeModeCoordinates);
                    mBoardMessenger.sendAction(
                            mBoard.getId(),
                            action.getType(),
                            data,
                            convertUTCTimeToBoardTime(mDownAt),
                            System.currentTimeMillis() - mDownAt,
                            null);
                }
            }
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void render(
            final Action action,
            final boolean careDuration,
            final ExtraRenderer extraRenderer) {

        if (Mode.FREE.isType(action) || Mode.ERASE.isType(action)) {

            final int[] coordinates = getActionCoordinates(action);
            if (coordinates.length < 4) {
                return;
            }

            final Matrix bitmapToCanvasMatrix = new Matrix(mBitmapToCanvasMatrix);
            final Matrix canvasToBitmapMatrix = new Matrix(mCanvasToBitmapMatrix);
            final Paint paint;
            if (Mode.FREE.isType(action)) {
                paint = new Paint(mPaint);
            } else if (Mode.ERASE.isType(action)) {
                paint = new Paint(mErasePaint);
            } else {
                paint = null; // never happen
            }
            setPaint(action, paint);

            if (careDuration) {
                long interval = action.getDuration() / (coordinates.length / 2 - 1);
                for (int i = 0; i < coordinates.length - 2; i += 2) {
                    final int index = i;
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {

                            if (!mRunning) return;

                            Canvas canvas = getHolder().lockCanvas();

                            int[] points;
                            if (index >= 2) {
                                points = new int[] {
                                        coordinates[index-2],
                                        coordinates[index-1],
                                        coordinates[index],
                                        coordinates[index + 1],
                                        coordinates[index + 2],
                                        coordinates[index + 3]
                                };
                            } else {
                                points = new int[]{
                                        coordinates[index],
                                        coordinates[index + 1],
                                        coordinates[index + 2],
                                        coordinates[index + 3]
                                };
                            }

                            mRenderCanvas.drawPath(coordinatesToPath(points), paint);

                            exportRenderBitmapToCanvas(
                                    canvas,
                                    bitmapToCanvasMatrix,
                                    canvasToBitmapMatrix,
                                    extraRenderer);
                            getHolder().unlockCanvasAndPost(canvas);
                        }
                    };
                    if (i == 0) {
                        mHandler.post(r);
                    } else {
                        mHandler.postDelayed(r, interval);
                    }
                }
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (!mRunning) return;

                        Canvas canvas = getHolder().lockCanvas();
                        mRenderCanvas.drawPath(coordinatesToPath(coordinates), paint);
                        exportRenderBitmapToCanvas(
                                canvas,
                                bitmapToCanvasMatrix,
                                canvasToBitmapMatrix,
                                extraRenderer);
                        getHolder().unlockCanvasAndPost(canvas);
                    }
                });
            }
        }
    }

    private static interface ExtraRenderer {
        void render(Canvas canvas);
    }

    private Path coordinatesToPath(int[] coordinates) {
        Path path = new Path();
        if (coordinates.length < 4) {
            return path;
        }
        path.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length-1; i+=2) {
            path.lineTo(coordinates[i], coordinates[i+1]);
        }
        return path;
    }

    private int[] getActionCoordinates(Action action) {
        if (action.getData() != null) {
            String[] s1 = action.getData().split(MAJOR_SEPARATOR);
            String[] s2 = s1[0].split(MINOR_SEPARATOR);
            final int[] coordinates = new int[s2.length];
            for (int i = 0; i < s2.length; i++) {
                coordinates[i] = Integer.parseInt(s2[i]);
            }
            return coordinates;
        } else {
            return new int[] {};
        }
    }

    private void setPaint(Action action, Paint paint) {

        if (action.getData() == null) {
            return;
        }

        if (Mode.FREE.isType(action)) {
            String[] s = action.getData().split(MAJOR_SEPARATOR);
            int rbg = Integer.parseInt(s[1], 16);
            paint.setColor(0xff000000 + rbg);
            paint.setStrokeWidth(Float.parseFloat(s[2]));
        }

        if (Mode.ERASE.isType(action)) {
            String[] s = action.getData().split(MAJOR_SEPARATOR);
            paint.setStrokeWidth(Float.parseFloat(s[2]));
        }
    }

    private String generateActionData(String type, Paint paint, List<Integer> coordinates) {

        List<String> tokens = new ArrayList<String>();

        if (Mode.FREE.type.equals(type)) {
            tokens.add(TextUtils.join(MINOR_SEPARATOR, coordinates));
            tokens.add(Integer.toString(paint.getColor() & 0xffffff, 16));
            tokens.add(Float.toString(dpFromPx(paint.getStrokeWidth())));
            return TextUtils.join(MAJOR_SEPARATOR, tokens);
        }

        if (Mode.ERASE.type.equals(type)) {
            tokens.add(TextUtils.join(MINOR_SEPARATOR, coordinates));
            tokens.add("0");
            tokens.add(Float.toString(dpFromPx(paint.getStrokeWidth())));
            return TextUtils.join(MAJOR_SEPARATOR, tokens);
        }

        return null;
    }

    private void render() {

        final Matrix bitmapToCanvasMatrix = new Matrix(mBitmapToCanvasMatrix);
        final Matrix canvasToBitmapMatrix = new Matrix(mCanvasToBitmapMatrix);

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (!mRunning) return;

                Canvas canvas = getHolder().lockCanvas();
                exportRenderBitmapToCanvas(
                        canvas,
                        bitmapToCanvasMatrix,
                        canvasToBitmapMatrix,
                        null);
                getHolder().unlockCanvasAndPost(canvas);
            }
        });
    }

    private void exportRenderBitmapToCanvas(
            Canvas canvas,
            Matrix bitmapToCanvasMatrix,
            Matrix canvasToBitmapMatrix,
            ExtraRenderer extraRenderer) {

        if (canvas == null) {
            return;
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(mRenderBitmap, bitmapToCanvasMatrix, null);
        if (extraRenderer != null) {
            extraRenderer.render(canvas);
        }

        // draw thumbnail
        canvas.drawBitmap(mRenderBitmap, mBitmapToThumbnailMatrix, mThumbnailPaint);
        canvas.drawRect(mThumbnailRect, mThumbnailBorderPaint);

        // draw viewport inside thumbnail
        RectF viewport = new RectF(0, 0, getWidth(), getHeight());
        canvasToBitmapMatrix.mapRect(viewport); // canvas -> bitmap
        mBitmapToThumbnailMatrix.mapRect(viewport); // bitmap -> thumbnail
        canvas.save();
        canvas.clipRect(mThumbnailRect);
        canvas.drawRect(viewport, mViewPortBorderPaint);
        canvas.restore();
    }

    private long convertUTCTimeToBoardTime(long t) {
        return mLatestActionTo + t - mLastReloadAt;
    }

    public void resume(final ResumeCallback callback) {
        BoardMessenger.getInstance().getBoardActions(mBoard.getId(), new BoardMessenger.GetBoardActionsCallback() {
            @Override
            public void onSuccess(List<Action> actions) {
                resume(actions, callback);
            }

            @Override
            public void onError() {
                if (callback != null) {
                    mMainThreaHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError();
                        }
                    });
                }
            }
        });
    }

    public static interface ResumeCallback {
        public void onSuccess();
        public void onError();
    }

    private void resume(final List<Action> actions, final ResumeCallback callback) {

        if (getWidth() == 0 || getHeight() == 0 || !mRunning) {
            mMainThreaHandler.post(new Runnable() {
                @Override
                public void run() {
                    resume(actions, callback);
                }
            });
            return;
        }

        if (mRenderBitmap != null) {
            mRenderCanvas.drawColor(mBackgroundColor);
        } else {
            mRenderBitmap = Bitmap.createBitmap(
                    mBoard.getWidth(),
                    mBoard.getHeight(),
                    Bitmap.Config.ARGB_8888);
            mRenderCanvas = new Canvas();
            mRenderCanvas.setBitmap(mRenderBitmap);
            mRenderCanvas.drawColor(mBackgroundColor);

            float scaleFactor = Math.min(
                    getWidth() * THUMBNAIL_RATIO / mRenderBitmap.getWidth(),
                    getHeight() * THUMBNAIL_RATIO / mRenderBitmap.getHeight());
            mBitmapToThumbnailMatrix = new Matrix();
            mBitmapToThumbnailMatrix.postScale(scaleFactor, scaleFactor, 0, 0);
            mThumbnailRect = new RectF(0, 0, mRenderBitmap.getWidth(), mRenderBitmap.getHeight());
            mBitmapToThumbnailMatrix.mapRect(mThumbnailRect);
            mBitmapToThumbnailMatrix.postTranslate(0, getHeight() - mThumbnailRect.height());
            mThumbnailToBitmapMatrix = new Matrix();
            mBitmapToThumbnailMatrix.invert(mThumbnailToBitmapMatrix);
            mThumbnailRect = new RectF(0, 0, mRenderBitmap.getWidth(), mRenderBitmap.getHeight());
            mBitmapToThumbnailMatrix.mapRect(mThumbnailRect);
        }

        final Matrix bitmapToCanvasMatrix = new Matrix(mBitmapToCanvasMatrix);
        final Matrix canvasToBitmapMatrix = new Matrix(mCanvasToBitmapMatrix);
        final Paint paint = new Paint(mPaint);
        final Paint erasePaint = new Paint(mErasePaint);
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                Canvas canvas = getHolder().lockCanvas();
                for (final Action a : actions) {
                    if (Mode.FREE.isType(a) || Mode.ERASE.isType(a)) {
                        final int[] coordinates = getActionCoordinates(a);
                        if (coordinates.length < 4) {
                            return;
                        }
                        if (Mode.FREE.isType(a)) {
                            setPaint(a, paint);
                            mRenderCanvas.drawPath(coordinatesToPath(coordinates), paint);
                        }
                        if (Mode.ERASE.isType(a)) {
                            setPaint(a, erasePaint);
                            mRenderCanvas.drawPath(coordinatesToPath(coordinates), erasePaint);
                        }
                    }
                }
                exportRenderBitmapToCanvas(
                        canvas,
                        bitmapToCanvasMatrix,
                        canvasToBitmapMatrix,
                        null);
                getHolder().unlockCanvasAndPost(canvas);
            }
        });


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!actions.isEmpty()) {
                    Action latestAction = actions.get(actions.size()-1);
                    mLatestActionTo = latestAction.getFrom() + latestAction.getDuration();
                } else {
                    mLatestActionTo = 0;
                }
                mLastReloadAt = System.currentTimeMillis();

                if (callback != null) {
                    mMainThreaHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                }
            }
        });
    }

    private void animateToZoomFocusedPoint(final float px, final float py, float ratio) {

        float[] mMatrixValues = new float[9];
        mBitmapToCanvasMatrix.getValues(mMatrixValues);

        final float currentScale = mMatrixValues[Matrix.MSCALE_X];
        final float targetScale = currentScale * ratio;
        final Matrix preservedMatrix = new Matrix(mBitmapToCanvasMatrix);

        ValueAnimator anim = ValueAnimator.ofFloat(currentScale, targetScale);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator anim) {

                float scale = (Float) anim.getAnimatedValue();
                mBitmapToCanvasMatrix.set(preservedMatrix);
                mBitmapToCanvasMatrix.postScale(scale / currentScale, scale / currentScale, px, py);
                mBitmapToCanvasMatrix.invert(mCanvasToBitmapMatrix);

                render();
            }
        });

        if (anim != null) {
            anim.setDuration(TAPZOOM_ANIMATE_DURATION);
            anim.start();
        }
    }

    public void setFreeDrawMode() {
        mMode = Mode.FREE;
    }

    public boolean isFreeDrawMode() {
        return mMode == Mode.FREE;
    }

    public void setEraseMode() {
        mMode = Mode.ERASE;
    }

    public boolean isEraseMode() {
        return mMode == Mode.ERASE;
    }

    public void setStrokeWidthInDp(int dp) {
        mPaint.setStrokeWidth(pxFromDp(dp));
    }

    public float getStrokeWidthInDp() {
        return dpFromPx(mPaint.getStrokeWidth());
    }

    public void setEraseStrokeWidthInDp(float dp) {
        mErasePaint.setStrokeWidth(pxFromDp(dp));
    }

    public float getEraseStrokeWidthInDp() {
        return dpFromPx(mPaint.getStrokeWidth());
    }

    public void setColorRGB(int rgb) {
        rgb = rgb & 0xffffff;
        mPaint.setColor(0xff000000 + rgb);
    }

    public int getColorRGB() {
        return mPaint.getColor() & 0xffffff;
    }

    private float pxFromDp(float dp) {
        return dp * mDensity;
    }

    private float dpFromPx(float px) {
        return px / mDensity;
    }
}
