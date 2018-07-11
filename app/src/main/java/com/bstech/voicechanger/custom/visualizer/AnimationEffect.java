package com.bstech.voicechanger.custom.visualizer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AnimationEffect {
    private static final int[] CurveData;
    public static final int EFFECT_ADD_DISTANCE = 300;
    public static final int EFFECT_ADD_POSITION = 200;
    public static final int EFFECT_ADD_SIZE = 400;
    public static final int EFFECT_DISTANCE = 10;
    public static final int EFFECT_FLING = 5;
    public static final int EFFECT_NEXT = 1;
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_PLAY_FORCE = 100;
    public static final int EFFECT_PREV = 2;
    public static final int EFFECT_RELOCATE = 3;
    public static final int EFFECT_TO_FULL = 6;
    public static final int EFFECT_TO_NARROW = 9;
    public static final int EFFECT_TO_NAVI = 7;
    public static final int EFFECT_TO_WIDE = 8;
    private static final int MSG_EFFECT_END = 1;
    private static final int[] SineData;
    private static final String TAG = "AnimationEffect";
    private int curMoveX;
    private int curMoveY;
    private int curSize;
    private int curStep;
    private int dstPosX;
    private int dstPosY;
    private int dstSize;
    private int idx_of_move;
    private float mAcceleration;
    public callback mCallback;
    private int mGENERAL_MAXSTEP;
    private Handler mHandler;
    private double mTheta;
    private int mVelocity;
    private int maxMoveX;
    private int maxMoveY;
    private int maxSize;
    private int maxStep;
    private int movePosX;
    private int movePosY;
    private int next;
    private int srcPosX;
    private int srcPosY;
    private int srcSize;
    private int type;

    class PosInfo {
        int posX;
        int posY;
        int size;

        PosInfo() {
        }
    }

    public interface callback {
        void onAnimationBegin(int i);

        void onAnimationEnd(AnimationEffect animationEffect, int i, int i2);
    }

    static {
        CurveData = new int[]{EFFECT_NONE, 85, 159, 231, 295, 348, 401, 448, 489, 533, 566, 603, 633, 660, 688, 711, 734, 755, 774, 794, 812, 827, 843, 857, 872, 882, 892, 902, 913, 921, 929, 937, 944, 948, 955, 961, 965, 968, 975, 977, 981, 983, 987, 989, 991, 993, 995, 995, 997, 998, 1000, 1000};
        SineData = new int[]{EFFECT_NONE, 31, 62, 94, 125, 156, 187, 218, 248, 278, 309, 338, 368, 397, 425, 453, 481, 509, 535, 562, 587, 612, 637, 661, 684, 707, 728, 750, 770, 790, 809, 827, 844, 860, 876, 891, 904, 917, 929, 940, 951, 960, 968, 975, 982, 987, 992, 995, 998, 999, 1000, 1000};
    }

    public AnimationEffect() {
        mGENERAL_MAXSTEP = 15;
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AnimationEffect.MSG_EFFECT_END /*1*/:
                        if (mCallback != null) {
                            mCallback.onAnimationEnd(AnimationEffect.this, msg.arg1, msg.arg2);
                        }
                    default:
                        Log.e(AnimationEffect.TAG, "handleMessage() msg : " + msg);
                }
            }
        };
        type = EFFECT_NONE;
    }

    private int getData(int[] dataArray, int step, int maxStep, int maxLen) {
        return (int) ((((long) dataArray[((step * 52) / maxStep) - 1]) * ((long) maxLen)) / 1000);
    }

    public static int getValueFromCurveData(int step, int maxStep, int maxLen) {
        return step >= maxStep ? maxLen : (CurveData[((step * 52) / maxStep) - 1] * maxLen) / 1000;
    }

    public static int getValueFromSineData(int step, int maxStep, int maxLen) {
        return step >= maxStep ? maxLen : (SineData[((step * 52) / maxStep) - 1] * maxLen) / 1000;
    }

    private void OnEnd() {
        Log.v(TAG, "OnEnd(" + type + ")");
        Message msg = mHandler.obtainMessage(MSG_EFFECT_END);
        msg.arg1 = type;
        msg.arg2 = next;
        type = EFFECT_NONE;
        next = EFFECT_NONE;
        if (!mHandler.hasMessages(MSG_EFFECT_END)) {
            mHandler.sendMessage(msg);
        }
    }

    public void initIndex(int index) {
        idx_of_move = index;
    }

    public void initPosition(int oriPosX, int oriPosY, int dstPosX, int dstPosY, int maxStep) {
        srcPosX = oriPosX;
        srcPosY = oriPosY;
        dstPosX = dstPosX;
        dstPosY = dstPosY;
        movePosX = oriPosX;
        movePosY = oriPosY;
        maxMoveX = dstPosX - oriPosX;
        maxMoveY = dstPosY - oriPosY;
        curStep = EFFECT_NONE;
        maxStep = maxStep;
        if (maxStep <= 0) {
            maxStep = mGENERAL_MAXSTEP;
        }
    }

    public void initVelocity(int posX, int posY, int velocity, float acceleration, double theta) {
        movePosX = posX;
        movePosY = posY;
        mTheta = theta;
        mVelocity = velocity;
        mAcceleration = acceleration;
        if (velocity < 0) {
            mAcceleration = acceleration;
        } else {
            mAcceleration = -acceleration;
        }
    }

    public void initSize(int oriSize, int dstSize, int maxStep) {
        srcSize = oriSize;
        dstSize = dstSize;
        curSize = oriSize;
        maxSize = dstSize - oriSize;
        curStep = EFFECT_NONE;
        maxStep = maxStep;
        if (maxStep <= 0) {
            maxStep = mGENERAL_MAXSTEP;
        }
    }

    public void addDistance(int oriSize, int addSize, int maxStep) {
        if (type != EFFECT_ADD_DISTANCE) {
            initSize(oriSize, oriSize + addSize, maxStep);
        } else {
            initSize(oriSize, dstSize + addSize, maxStep);
        }
    }

    public void addPosition(int oriPosX, int oriPosY, int addPosX, int addPosY, int maxStep) {
        if (type != EFFECT_ADD_POSITION) {
            initPosition(oriPosX, oriPosY, oriPosX + addPosX, oriPosY + addPosY, maxStep);
            return;
        }
        initPosition(oriPosX, oriPosY, dstPosX + addPosX, dstPosY + addPosY, maxStep);
    }

    public void addSize(int oriSize, int addSize, int maxStep) {
        if (type != EFFECT_ADD_SIZE) {
            initSize(oriSize, oriSize + addSize, maxStep);
        } else {
            initSize(oriSize, dstSize + addSize, maxStep);
        }
    }

    public void start(int effect) {
        Log.v(TAG, "start(" + effect + ")");
        type = effect;
        mHandler.removeMessages(MSG_EFFECT_END);
        if (mCallback != null) {
            mCallback.onAnimationBegin(effect);
        }
    }

    public void stop() {
        if (type != 0) {
            Log.v(TAG, "stop()");
            type = EFFECT_NONE;
        }
    }

    public void setMoreAcceleration(int multiple) {
        mAcceleration *= (float) multiple;
    }

    public void next(int effect) {
        Log.v(TAG, "next(" + effect + ")");
        next = effect;
    }

    public void update() {
        switch (type) {
            case MSG_EFFECT_END /*1*/:
            case EFFECT_PREV /*2*/:
            case EFFECT_ADD_POSITION /*200*/:
                if (curStep == maxStep || (srcPosX == dstPosX && srcPosY == dstPosY)) {
                    OnEnd();
                    return;
                }
                curStep += MSG_EFFECT_END;
                curMoveX = getData(CurveData, curStep, maxStep, maxMoveX);
                movePosX = srcPosX + curMoveX;
                curMoveY = getData(CurveData, curStep, maxStep, maxMoveY);
                movePosY = srcPosY + curMoveY;
            case EFFECT_RELOCATE /*3*/:
                if (calculate_pos_step(SineData)) {
                    OnEnd();
                }
            case EFFECT_FLING /*5*/:
                mVelocity = (int) (((float) mVelocity) + mAcceleration);
                if ((mVelocity <= 0 || mAcceleration <= 0.0f) && (mVelocity >= 0 || mAcceleration >= 0.0f)) {
                    movePosX = (int) (((double) movePosX) + (((double) mVelocity) * Math.cos(mTheta)));
                    movePosY = (int) (((double) movePosY) + (((double) mVelocity) * Math.sin(mTheta)));
                    return;
                }
                OnEnd();
            case EFFECT_TO_FULL /*6*/:
            case EFFECT_TO_NAVI /*7*/:
            case EFFECT_ADD_SIZE /*400*/:
                if (calculate_both_step(CurveData, CurveData)) {
                    OnEnd();
                }
            case EFFECT_TO_WIDE /*8*/:
            case EFFECT_TO_NARROW /*9*/:
            case EFFECT_DISTANCE /*10*/:
            case EFFECT_ADD_DISTANCE /*300*/:
                if (calculate_size_step(CurveData)) {
                    OnEnd();
                }
            default:
        }
    }

    private boolean calculate_size_step(int[] data) {
        if (curStep == maxStep || srcSize == dstSize) {
            return true;
        }
        curStep += MSG_EFFECT_END;
        curSize = srcSize + getData(CurveData, curStep, maxStep, maxSize);
        return false;
    }

    private boolean calculate_pos_step(int[] data) {
        if (curStep == maxStep || (srcPosX == dstPosX && srcPosY == dstPosY)) {
            return true;
        }
        curStep += MSG_EFFECT_END;
        curMoveX = getData(data, curStep, maxStep, maxMoveX);
        movePosX = srcPosX + curMoveX;
        curMoveY = getData(data, curStep, maxStep, maxMoveY);
        movePosY = srcPosY + curMoveY;
        return false;
    }

    private boolean calculate_both_step(int[] data1, int[] data2) {
        if (curStep == maxStep) {
            return true;
        }
        curStep += MSG_EFFECT_END;
        curSize = srcSize + getData(CurveData, curStep, maxStep, maxSize);
        curMoveX = getData(data1, curStep, maxStep, maxMoveX);
        movePosX = srcPosX + curMoveX;
        curMoveY = getData(data2, curStep, maxStep, maxMoveY);
        movePosY = srcPosY + curMoveY;
        return false;
    }

    public int moveX() {
        return movePosX;
    }

    public int moveY() {
        return movePosY;
    }

    public int getSize() {
        return curSize;
    }

    public int getIndex() {
        return idx_of_move;
    }

    public boolean isWorking() {
        return type != 0;
    }

    public boolean isMoving() {
        return type == MSG_EFFECT_END || type == EFFECT_PREV || type == EFFECT_RELOCATE || type == EFFECT_FLING || type == EFFECT_ADD_POSITION;
    }

    public boolean isFling() {
        return type == EFFECT_FLING;
    }

    public boolean isIndexMove() {
        return type == EFFECT_TO_FULL || type == EFFECT_TO_NAVI || type == EFFECT_ADD_SIZE;
    }

    public boolean isResizing() {
        return type == EFFECT_TO_FULL || type == EFFECT_TO_NAVI || type == EFFECT_ADD_SIZE;
    }

    public boolean isDistance() {
        return type == EFFECT_TO_WIDE || type == EFFECT_TO_NARROW || type == EFFECT_DISTANCE || type == EFFECT_ADD_DISTANCE;
    }

    public int hasNext() {
        return next;
    }

    public void addCallback(callback callback) {
        mCallback = callback;
    }
}