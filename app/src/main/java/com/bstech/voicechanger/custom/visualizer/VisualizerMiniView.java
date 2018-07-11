package com.bstech.voicechanger.custom.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class VisualizerMiniView extends View implements IVisualizerView {
    private static final String TAG = "MiniEQView";
    private Context mContext;
    private int mCountdownToStop;
    private int mDensity;
    private boolean mUpdate;
    private ITypeView mViewType;

    public VisualizerMiniView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDensity = 1;
        mCountdownToStop = 0;
        mUpdate = true;
        mContext = null;
        mContext = context;
        mViewType = new TypeGraphBar(context, ITypeView.MINI_VIEW);
        mDensity = (int) mContext.getResources().getDisplayMetrics().density;
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