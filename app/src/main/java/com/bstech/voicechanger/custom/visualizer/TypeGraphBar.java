package com.bstech.voicechanger.custom.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;

import com.bstech.voicechanger.utils.PreferenceUtils;

public class TypeGraphBar implements ITypeView {
    private final int CUSTOM_COLORSET;
    private final int GRAPHBAR_BASE;
    private final int GRAPHBAR_GAP;
    private final int GRAPHBAR_SHOW_MAX;
    private int GRAPH_TUNING_END;
    private int GRAPH_TUNING_GAP;
    private final int GRAPH_TUNING_MAX;
    private final int GRAPH_TUNING_MIN;
    private int GRAPH_TUNING_START;
    private int mAlphaValue;
    private int mColorValue;
    private Context mContext;
    private int mGRAPHBAR_HEIGHT;
    private int mGRAPHBAR_SHOW_COUNT;
    private int mGRAPHBAR_WIDTH;
    private int mGRAPH_HEIGHT;
    private int mGRAPH_WIDTH;
    private int mGRAPH_X;
    private int mGRAPH_Y;
    private GraphBar[] mGraphBar;
    private final int[] mSpectrumColors;
    private final int[] mGreenColorSet;
    private final int[] mRedColorSet;
    private final int[] mPurpleColorSet;
    private final int[] mYellowColorSet;
    private final int[] mBlueColors;
    private float mHeightRatio;
    private int mMICSensitivity;
    private int mMaxStep;
    private Paint mPaintBarLine;
    private float mREFLECTION_RATIO;
    private int mSCREEN_DENSITY;
    private int mSCREEN_HEIGHT;
    private int mSCREEN_WIDTH;
    private boolean mShowStick;
    private boolean mUseMic;
    private int mViewSize;
    private Woofer[] mWoofer;

    private class GraphBar {
        private boolean bAvailable;
        private int currPos;
        private int gap;
        private int imgColorDown;
        private int imgColorUp;
        private int linePos;
        private int livePos;
        private int liveStep;
        private Rect mDstRect;
        private Paint mOrPaint;
        private Paint mRePaint;
        private int myIndex;
        private int posX;
        private int posY;
        private int prevPos;
        private int stayCnt;
        private int stickH;
        private float velocity;
        private int width;

        GraphBar(int index) {
            bAvailable = true;
            mDstRect = new Rect();
            mRePaint = new Paint();
            mOrPaint = new Paint();
            myIndex = index;
            updatePosition();
            linePos = mGRAPH_Y + mGRAPHBAR_HEIGHT;
            updateColorSet(mColorValue);
            updateAlpha(mAlphaValue);
        }

        public void updateColorSet(int value) {
            int[] colorSet;
            if (value == 0) {
                imgColorUp = mSpectrumColors[((myIndex * mSpectrumColors.length) / mGRAPHBAR_SHOW_COUNT) % mSpectrumColors.length];
//                imgColorUp = Color.WHITE;
                imgColorDown = imgColorUp;
                mOrPaint.setColor(imgColorUp);
                mRePaint.setColor(imgColorUp);
                mOrPaint.setColorFilter(null);
                mRePaint.setColorFilter(null);
                mOrPaint.setShader(null);
                mRePaint.setShader(null);
            } else if (value == 7) {
                imgColorUp = mSpectrumColors[((myIndex * mSpectrumColors.length) / mGRAPHBAR_SHOW_COUNT) % mSpectrumColors.length];
                imgColorDown = imgColorUp;
                mOrPaint.setColor(imgColorUp);
                mRePaint.setColor(imgColorUp);
                mOrPaint.setShader(null);
                mRePaint.setShader(null);
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0.0f);
                ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
                mOrPaint.setColorFilter(f);
                mRePaint.setColorFilter(f);
            } else if (value == 8) {
                int i;
                colorSet = new int[PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_COLORSET_NUM, 20)];
                for (i = 0; i < colorSet.length; i++) {
                    colorSet[i] = PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_COLORSET + (i + 1));
                }
                imgColorUp = colorSet[((myIndex * colorSet.length) / mGRAPHBAR_SHOW_COUNT) % colorSet.length];
                for (i = 0; i < colorSet.length; i++) {
                    colorSet[i] = PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_BOTTOMSET + (i + 1));
                }
                imgColorDown = colorSet[((myIndex * colorSet.length) / mGRAPHBAR_SHOW_COUNT) % colorSet.length];
                mOrPaint.setColorFilter(null);
                mRePaint.setColorFilter(null);
            } else {
                colorSet = mSpectrumColors;
                switch (value) {
                    case 1:
                        colorSet = mSpectrumColors;
                        break;
                    case 2:
                        colorSet = mBlueColors;
                        break;
                    case 3:
                        colorSet = mPurpleColorSet;
                        break;
                    case 4:
                        colorSet = mGreenColorSet;
                        break;
                    case 5:
                        colorSet = mYellowColorSet;
                        break;
                    case 6:
                        colorSet = mRedColorSet;
                        break;
                }
                imgColorUp = colorSet[((myIndex * colorSet.length) / mGRAPHBAR_SHOW_COUNT) % colorSet.length];
                imgColorDown = -1;
                mOrPaint.setColor(imgColorUp);
                mRePaint.setColor(imgColorUp);
                mOrPaint.setColorFilter(null);
                mRePaint.setColorFilter(null);
            }
        }

        public void updateAlpha(int value) {
            mAlphaValue = value;
            mOrPaint.setAlpha(value);
            mRePaint.setAlpha((value * 2) / 5);
        }

        public void updatePosition() {
            width = mGRAPHBAR_WIDTH;
            posX = mGRAPH_X + (myIndex * width);
            stickH = (width / 16) + 1;
            gap = (width / 20) + 1;
            if (mViewSize == 1) {
                posY = mSCREEN_HEIGHT;
            } else {
                posY = (int) (((float) mSCREEN_HEIGHT) * (1.0f - mREFLECTION_RATIO));
            }
            bAvailable = posX + width <= mSCREEN_WIDTH;
        }

        public void updateBar(int value) {
            prevPos = currPos;
            livePos = currPos;
            currPos = value;
            liveStep = 0;
        }

        public boolean isAvailable() {
            return bAvailable;
        }

        public void draw(Canvas c) {
            if (bAvailable) {
                liveStep++;
                if (currPos < prevPos) {
                    livePos = prevPos + AnimationEffect.getValueFromSineData(liveStep, (mMaxStep * 3) / 2, currPos - prevPos);
                } else {
                    livePos = prevPos + AnimationEffect.getValueFromSineData(liveStep, mMaxStep, currPos - prevPos);
                }
                int intensity = (mGRAPHBAR_HEIGHT * livePos) / 250;
                if (intensity < 5) {
                    intensity = 5;
                }
                if (imgColorUp == imgColorDown) {
                    mOrPaint.setColor(imgColorUp);
                    mRePaint.setColor(imgColorUp);
                    updateAlpha(mAlphaValue);
                    mOrPaint.setShader(null);
                    mRePaint.setShader(null);
                } else {
                    mOrPaint.setShader(new LinearGradient(0.0f, (float) (posY - ((int) (((float) intensity) * (1.0f - mREFLECTION_RATIO)))), 0.0f, (float) posY, imgColorUp, imgColorDown, TileMode.CLAMP));
                    mRePaint.setShader(new LinearGradient(0.0f, (float) (posY + 5), 0.0f, (float) ((posY + 5) + ((int) (((float) intensity) * mREFLECTION_RATIO))), imgColorDown, imgColorUp, TileMode.CLAMP));
                }
                if (mViewSize == 0) {
                    mDstRect.set(posX, posY + 5, posX + (width - gap), (posY + 5) + ((int) (((float) intensity) * mREFLECTION_RATIO)));
                    c.drawRect(mDstRect, mRePaint);
                    mDstRect.set(posX, posY - ((int) (((float) intensity) * (1.0f - mREFLECTION_RATIO))), posX + (width - gap), posY);
                    c.drawRect(mDstRect, mOrPaint);
                } else {
                    mDstRect.set(posX, posY - intensity, posX + (width - gap), posY);
                    c.drawRect(mDstRect, mOrPaint);
                }
                if (mViewSize == 0 || mShowStick) {
                    if (stayCnt > 0) {
                        stayCnt--;
                    } else {
                        velocity += 0.2f;
                        linePos = (int) (((float) linePos) + velocity);
                    }
                    if (linePos >= mDstRect.top - 10) {
                        linePos = mDstRect.top - 10;
                        stayCnt = 10;
                        velocity = 0.0f;
                    }
                    c.drawRect((float) (posX + 1), (float) (linePos - stickH), (float) ((posX + (width - gap)) - 1), (float) linePos, mOrPaint);
                }
            }
        }
    }

    private class Woofer {
        private int currPos;
        private int livePos;
        private int liveStep;
        private Paint mWoPaint;
        private int myIndex;
        private int prevPos;

        Woofer(int index) {
            mWoPaint = new Paint();
            myIndex = index;
        }

        protected void updateBase(int value) {
            prevPos = currPos;
            livePos = currPos;
            currPos = value;
            liveStep = 0;
        }

        public void draw(Canvas c) {
            liveStep++;
            if (currPos <= prevPos) {
                livePos--;
            } else {
                livePos = currPos;
            }
            mWoPaint.setColor(SupportMenu.CATEGORY_MASK);
            c.drawCircle((float) ((mSCREEN_WIDTH / 20) + ((myIndex * mSCREEN_WIDTH) / 10)), (float) (mSCREEN_HEIGHT / 2), (float) livePos, mWoPaint);
        }
    }

    public TypeGraphBar(Context context, int viewSize) {
        mHeightRatio = 0.4f;
        mAlphaValue = MotionEventCompat.ACTION_MASK;
        mColorValue = 0;
        mMaxStep = 5;
        mViewSize = 0;
        mShowStick = false;
        mUseMic = false;
        mMICSensitivity = 70;
        CUSTOM_COLORSET = 8;
        GRAPHBAR_SHOW_MAX = 46;
        GRAPHBAR_BASE = 5;
        GRAPHBAR_GAP = 1;
        GRAPH_TUNING_MAX = 250;
        GRAPH_TUNING_MIN = 1;
        GRAPH_TUNING_START = 80;
        GRAPH_TUNING_END = 600;
        GRAPH_TUNING_GAP = (GRAPH_TUNING_END - GRAPH_TUNING_START) / 45;
        mGRAPHBAR_SHOW_COUNT = 0;
        mSCREEN_DENSITY = 0;
        mSCREEN_WIDTH = 0;
        mSCREEN_HEIGHT = 0;
        mGRAPH_X = 0;
        mGRAPH_Y = 0;
        mGRAPH_WIDTH = 0;
        mGRAPH_HEIGHT = 0;
        mGRAPHBAR_WIDTH = 0;
        mGRAPHBAR_HEIGHT = 0;
        mREFLECTION_RATIO = 0.25f;
        mSpectrumColors = new int[]{
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 100, 150, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145)
        };
        mBlueColors = new int[]{
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 100, 150, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145)
        };
        mPurpleColorSet = new int[]{
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 100, 150, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145)
        };
        mGreenColorSet = new int[]{
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 100, 150, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145)
        };
        mYellowColorSet = new int[]{
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 100, 150, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145)
        };
        mRedColorSet = new int[]{
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 100, 150, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 110, 158, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 116, 166, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 124, 173, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 131, 179, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 137, 186, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 145, 194, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 151, 201, 0),
                Color.argb(MotionEventCompat.ACTION_MASK, 169, 209, 36),
                Color.argb(MotionEventCompat.ACTION_MASK, 181, 217, 74),
                Color.argb(MotionEventCompat.ACTION_MASK, 197, 224, 110),
                Color.argb(MotionEventCompat.ACTION_MASK, 208, 230, 145)
        };
        mContext = context;
        mViewSize = viewSize;
        mPaintBarLine = new Paint();
        mPaintBarLine.setARGB(MotionEventCompat.ACTION_MASK, MotionEventCompat.ACTION_MASK, MotionEventCompat.ACTION_MASK, MotionEventCompat.ACTION_MASK);
        mAlphaValue = PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_ALPHA, MotionEventCompat.ACTION_MASK);
//        mColorValue = PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_COLOR, 0);
        mShowStick = PreferenceUtils.loadBooleanValue(mContext, PreferenceUtils.KEY_VI_STICK, true);
        mUseMic = PreferenceUtils.loadBooleanValue(mContext, PreferenceUtils.KEY_RC_PACKAGENAME, false);
        mMICSensitivity = PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_RC_PACKAGENAME, 80);
        mHeightRatio = 0.6f + ((((float) PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_BARRATIO, 0)) * 0.3f) / 100.0f);
        setUseMic(mUseMic);
    }

    public void onSizeChanged(int width, int height, int density) {
        int i;
        updateMaxStep(mHeightRatio);
        mSCREEN_WIDTH = width;
        mSCREEN_HEIGHT = height;
        mSCREEN_DENSITY = density;
        mGRAPHBAR_WIDTH = width / 46;
        mGRAPHBAR_WIDTH = (int) (((float) mGRAPHBAR_WIDTH) * (1.0f + ((mHeightRatio - 0.6f) * 3.0f)));
        if (mGRAPHBAR_WIDTH <= 0) {
            mGRAPHBAR_WIDTH = 1;
        }
        mGRAPHBAR_SHOW_COUNT = mSCREEN_WIDTH / mGRAPHBAR_WIDTH;
        if (mGRAPHBAR_SHOW_COUNT == 0) {
            mGRAPHBAR_SHOW_COUNT = 1;
        }
        mGRAPHBAR_HEIGHT = (int) (((float) height) * mHeightRatio);
        mGRAPH_WIDTH = mGRAPHBAR_WIDTH * mGRAPHBAR_SHOW_COUNT;
        mGRAPH_X = (width - mGRAPH_WIDTH) / 2;
        if (mViewSize == 1) {
            mGRAPH_Y = height - mGRAPHBAR_HEIGHT;
        } else {
            mGRAPH_Y = (height - mGRAPHBAR_HEIGHT) / 2;
        }
        if (mGraphBar == null) {
            mGraphBar = new GraphBar[46];
            for (i = 0; i < 46; i++) {
                mGraphBar[i] = new GraphBar(i);
            }
        } else {
            for (i = 0; i < 46; i++) {
                mGraphBar[i].updatePosition();
                mGraphBar[i].updateColorSet(mColorValue);
                mGraphBar[i].updateAlpha(mAlphaValue);
            }
        }
        if (mWoofer == null) {
            mWoofer = new Woofer[100];
            for (i = 0; i < 100; i++) {
                mWoofer[i] = new Woofer(i);
            }
        }
    }

    public void refreshChanged() {
        if (mSCREEN_WIDTH > 0 && mSCREEN_HEIGHT > 0) {
            onSizeChanged(mSCREEN_WIDTH, mSCREEN_HEIGHT, mSCREEN_DENSITY);
        }
    }

    public void update(byte[] bytes) {
        if (mGraphBar != null) {
            int i;
            if (bytes == null) {
                for (i = 0; i < 46; i++) {
                    mGraphBar[i].updateBar(0);
                }
                return;
            }
            int midValue;
            double average;
            int f;
            for (i = 1; i < bytes.length - 1; i++) {
                if (bytes[i] < 0) {
                    bytes[i] = (byte) (-bytes[i]);
                }
            }
            int availableCount = 0;
            for (i = 0; i < 46; i++) {
                if (mGraphBar[i].isAvailable()) {
                    availableCount++;
                }
            }
            if (availableCount % 2 == 0) {
                midValue = availableCount - 1;
            } else {
                midValue = availableCount;
            }
            for (i = 0; i < availableCount; i++) {
                average = 0.0d;
                f = GRAPH_TUNING_START + (GRAPH_TUNING_GAP * i);
                while (f < (GRAPH_TUNING_START + (GRAPH_TUNING_GAP * i)) + 10 && f < bytes.length) {
                    average += (double) bytes[f];
                    f++;
                }
                if (mUseMic) {
                    average = Math.log(1.0d + average) * (30.0d + (((double) mMICSensitivity) / 2.0d));
                } else {
                    average = Math.log(1.0d + (average / 10.0d)) * 150.0d;
                }
                if (average > 250.0d) {
                    average = 250.0d + (Math.log10(average - 250.0d) * 10.0d);
                } else if (average < 1.0d) {
                    average = 1.0d;
                }
                if (i % 2 == 0) {
                    mGraphBar[(midValue - i) / 2].updateBar((int) average);
                } else {
                    mGraphBar[(midValue + i) / 2].updateBar((int) average);
                }
            }
            i = 0;
            while (i < 5) {
                average = 0.0d;
                for (f = i * GRAPH_TUNING_GAP; f < (i + 1) * GRAPH_TUNING_GAP; f++) {
                    average += (double) bytes[f];
                }
                average = Math.log(1.0d + (average / 10.0d)) * 20.0d;
                if (i == 0 || i == 1) {
                    mWoofer[i].updateBase((int) average);
                } else {
                    mWoofer[i].updateBase((int) average);
                }
                i++;
            }
            i = 5;
            while (i < 10 && GRAPH_TUNING_END + ((i + 1) * GRAPH_TUNING_GAP) < bytes.length) {
                average = 0.0d;
                for (f = GRAPH_TUNING_END + (GRAPH_TUNING_GAP * i); f < GRAPH_TUNING_END + ((i + 1) * GRAPH_TUNING_GAP); f++) {
                    average += (double) bytes[f];
                }
                mWoofer[i].updateBase(((int) average) * 2);
                i++;
            }
        }
    }

    public void draw(Canvas canvas) {
        for (int i = 0; i < 46; i++) {
            mGraphBar[i].draw(canvas);
        }
    }

    private void updateMaxStep(float ratio) {
        if (ratio <= 0.6f) {
            mMaxStep = 6;
        } else if (ratio <= 0.9f) {
            mMaxStep = 7;
        } else {
            mMaxStep = 8;
        }
    }

    public float plusRatio() {
        if (mHeightRatio >= 0.9f) {
            mHeightRatio = 0.6f;
        } else {
            mHeightRatio += 0.1f;
        }
        onSizeChanged(mSCREEN_WIDTH, mSCREEN_HEIGHT, mSCREEN_DENSITY);
        PreferenceUtils.saveFloatValue(mContext, PreferenceUtils.KEY_VISUALIZER_RATIO, mHeightRatio);
        return mHeightRatio;
    }

    public void setAlpha(int value) {
        if (mGraphBar != null) {
            for (int i = 0; i < 46; i++) {
                mGraphBar[i].updateAlpha(value);
            }
        }
    }

    public void setColorSet(int value) {
        if (mGraphBar != null) {
            mAlphaValue = PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_ALPHA, MotionEventCompat.ACTION_MASK);
//            mColorValue = PreferenceUtils.loadIntegerValue(mContext, PreferenceUtils.KEY_VI_COLOR, 0);
            mColorValue = value;
            for (int i = 0; i < 46; i++) {
                mGraphBar[i].updateColorSet(value);
                mGraphBar[i].updateAlpha(mAlphaValue);
            }
        }
    }

    public void setBarSize(int value) {
        mHeightRatio = 0.6f + ((((float) value) * 0.3f) / 100.0f);
        onSizeChanged(mSCREEN_WIDTH, mSCREEN_HEIGHT, mSCREEN_DENSITY);
    }

    public void setStick(boolean show) {
        mShowStick = show;
    }

    public void setUseMic(boolean use) {
        mUseMic = use;
        Log.e("TEST", "mUseMic:" + mUseMic);
        if (mUseMic) {
            GRAPH_TUNING_START = 0;
            GRAPH_TUNING_END = 240;
        } else {
            GRAPH_TUNING_START = 80;
            GRAPH_TUNING_END = 600;
        }
        GRAPH_TUNING_GAP = (GRAPH_TUNING_END - GRAPH_TUNING_START) / 45;
    }

    public void setMICSensitivity(int value) {
        mMICSensitivity = value;
    }

    public int getCustomColorSet() {
        return 8;
    }
}