package com.bstech.voicechanger.custom.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;

public class VisualizerFullView extends View implements Callback, IVisualizerView {
    private static final String TAG = "VisualizerFullView";
    private final boolean USE_SURFACEVIEW;
    private Context mContext;
    private int mCountdownToStop;
    private int mDensity;
    private SurfaceHolder mHolder;
    private boolean mUpdate;
    private ITypeView mViewType;
    private ViewThread thread;

    class ViewThread extends Thread {
        private boolean mRun;

        public ViewThread(SurfaceHolder holder, Context context) {
            mRun = false;
            mHolder = holder;
        }

        public void run() {
            Log.d(VisualizerFullView.TAG, "run(" + mRun + ")");
            while (mRun) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                }
                Canvas c = null;
                try {
                    c = mHolder.lockCanvas(null);
                    synchronized (mHolder) {
                        doDraw(c);
                    }
                    if (c != null) {
                        mHolder.unlockCanvasAndPost(c);
                    }
                } catch (Exception ex) {
                    try {
                        Log.e(VisualizerFullView.TAG, "Exception!!:" + ex);
                        if (c != null) {
                            mHolder.unlockCanvasAndPost(c);
                        }
                    } catch (Throwable th) {
                        if (c != null) {
                            mHolder.unlockCanvasAndPost(c);
                        }
                    }
                }
            }
        }

        public void setRunning(boolean b) {
            mRun = b;
        }

        private void doDraw(Canvas canvas) {
            if (mRun) {
                canvas.drawColor(ViewCompat.MEASURED_STATE_MASK);
                mViewType.draw(canvas);
            }
        }
    }

    public VisualizerFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        USE_SURFACEVIEW = false;
        mDensity = 1;
        mCountdownToStop = 0;
        mUpdate = true;
        mContext = null;
        thread = null;
        mContext = context;
        mDensity = (int) mContext.getResources().getDisplayMetrics().density;
        mViewType = new TypeGraphBar(context, ITypeView.FULL_VIEW);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");
        mHolder = holder;
        startViewThread();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed()");
        stopViewThread();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged(" + width + ", " + height + ")");
        mHolder = holder;
    }

    private void startViewThread() {
        if (thread == null) {
            Log.d(TAG, "startThread()");
            thread = new ViewThread(mHolder, mContext);
            thread.setName("SoundFlip ViewThread");
            thread.setRunning(true);
            thread.start();
        }
    }

    private void stopViewThread() {
        if (thread != null) {
            Log.d(TAG, "stopThread()");
            Log.d(TAG, "=>time check1");
            thread.setRunning(false);
            thread.interrupt();
            boolean retry = true;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException");
                }
            }
            thread.interrupt();
            thread = null;
            Log.d(TAG, "=>time check2");
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged(" + w + ", " + h + ")");
        mViewType.onSizeChanged(w, h, mDensity);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void refreshChanged() {
        mViewType.refreshChanged();
    }

    public void update(byte[] bytes) {
        mViewType.update(bytes);
        if (bytes != null) {
            mUpdate = true;
        } else if (mUpdate && mCountdownToStop == 0) {
            mCountdownToStop = 70;
        }
        if (mUpdate) {
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mViewType.draw(canvas);
        if (mCountdownToStop > 0) {
            int i = mCountdownToStop - 1;
            mCountdownToStop = i;
            if (i == 0) {
                mUpdate = false;
            }
        }
        if (mUpdate) {
            invalidate();
        }
    }

    public float plusRatio() {
        return mViewType.plusRatio();
    }

    public void setAlpha(int value) {
        mViewType.setAlpha(value);
        if (!mUpdate) {
            mUpdate = true;
            mCountdownToStop = 5;
        }
    }

    public void setColorSet(int value) {
        mViewType.setColorSet(value);
        if (!mUpdate) {
            mUpdate = true;
            mCountdownToStop = 5;
        }
    }

    public void setBarSize(int value) {
        mViewType.setBarSize(value);
        if (!mUpdate) {
            mUpdate = true;
            mCountdownToStop = 5;
        }
    }

    public void setStick(boolean show) {
        mViewType.setStick(show);
        if (!mUpdate) {
            mUpdate = true;
            mCountdownToStop = 5;
        }
    }

    public void setUseMic(boolean use) {
        mViewType.setUseMic(use);
        if (!mUpdate) {
            mUpdate = true;
            mCountdownToStop = 5;
        }
    }

    public void refresh() {
        if (!mUpdate) {
            mUpdate = true;
            mCountdownToStop = 5;
        }
    }

    public void setMICSensitivity(int value) {
        mViewType.setMICSensitivity(value);
        if (!mUpdate) {
            mUpdate = true;
            mCountdownToStop = 5;
        }
    }

    public int getCustomColorSet() {
        return mViewType.getCustomColorSet();
    }
}