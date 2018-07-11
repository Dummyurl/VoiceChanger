package com.bstech.voicechanger.custom.visualizer;

import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bstech.voicechanger.utils.PreferenceUtils;


public class VisualizerAnytime {
    public static final String CLOSE_FLOATING_VIEW = "noh.jinil.app.anytime.xfloatingview";
    public static final String HIDE_FLOATING_VIEW = "noh.jinil.app.anytime.hidefloatingview";
    public static final String SHOW_FLOATING_VIEW = "noh.jinil.app.anytime.showfloatingview";
    private static final String TAG = "VisualizerAnytime";
    public static final String TOGGLE_FLOATING_VIEW = "noh.jinil.app.anytime.togglefloatingview";
    public static final String TOGGLE_MIC_USE = "noh.jinil.app.anytime.micuse";
    public static final String TOGGLE_VIEW_RATIO = "noh.jinil.app.anytime.viewratio";
    private float PREV_X;
    private float PREV_Y;
    private float START_X;
    private float START_Y;
    private boolean bShowVisualizer;
    private boolean bShowWindow;
    private boolean hasMoved;
    private LayoutParams mBtnParams;
    private LinearLayout mFloatingButtons;
    private RelativeLayout mFloatingLayout;
    ImageView mFloatingOnOff;
    private Handler mHandler;
    private Runnable mHideFloatingWindow;
    ImageView mMicOnOff;
    LinearLayout mOnOffLayout;
    private OnTouchListener mViewTouchListener;
    private VisualizerMiniView mVisualizerView;
    private WindowManager mWindowManager;

    /* renamed from: noh.jinil.app.anytime.music.VisualizerAnytime.1 */
    class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ Context context;

        AnonymousClass1(Context context) {
            this.context = context;
        }

        public void onClick(View v) {
            if (!hasMoved) {
                if (bShowVisualizer) {
                    hideFloatingVisualizer(this.context);
//                    mFloatingOnOff.setImageResource(R.drawable.noti_visualizer_off);
                    return;
                }
                showFloatingVisualizer(this.context);
//                mFloatingOnOff.setImageResource(R.drawable.noti_visualizer_on);
            }
        }
    }

    /* renamed from: noh.jinil.app.anytime.music.VisualizerAnytime.2 */
    class AnonymousClass2 implements OnClickListener {
        final /* synthetic */ Context val$context;

        AnonymousClass2(Context context) {
            val$context = context;
        }

        public void onClick(View v) {
            if (!hasMoved) {
                if (PreferenceUtils.loadBooleanValue(val$context, PreferenceUtils.KEY_RC_PACKAGENAME, false)) {
                    setupSession();
                    PreferenceUtils.saveBooleanValue(val$context, PreferenceUtils.KEY_RC_PACKAGENAME, false);
//                    mMicOnOff.setImageResource(R.drawable.microphone_off);
                    return;
                }
                setupMic();
                PreferenceUtils.saveBooleanValue(val$context, PreferenceUtils.KEY_RC_PACKAGENAME, true);
//                mMicOnOff.setImageResource(R.drawable.microphone_on);
            }
        }
    }

    public VisualizerAnytime() {
        mFloatingLayout = null;
        mFloatingButtons = null;
        mVisualizerView = null;
        mWindowManager = null;
        bShowWindow = false;
        bShowVisualizer = false;
        hasMoved = false;
        mViewTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case SwipeRefreshLayout.LARGE /*0*/:
                        START_X = event.getRawX();
                        START_Y = event.getRawY();
                        PREV_X = (float) mBtnParams.x;
                        PREV_Y = (float) mBtnParams.y;
                        hasMoved = false;
                        break;
                    case DrawerLayout.STATE_SETTLING /*2*/:
                        float x = (float) ((int) (event.getRawX() - START_X));
                        float y = (float) ((int) (event.getRawY() - START_Y));
                        mBtnParams.x = (int) (PREV_X + x);
                        mBtnParams.y = (int) (PREV_Y + y);
                        mWindowManager.updateViewLayout(mFloatingButtons, mBtnParams);
                        hasMoved = true;
                        break;
                }
                return true;
            }
        };
        mHideFloatingWindow = new Runnable() {
            public void run() {
                try {
                    mWindowManager.removeView(mFloatingLayout);
                    mFloatingLayout = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mHandler = new Handler() {
        };
    }

    public VisualizerAnytime(Context context, int sessionID) {
        mFloatingLayout = null;
        mFloatingButtons = null;
        mVisualizerView = null;
        mWindowManager = null;
        bShowWindow = false;
        bShowVisualizer = false;
        hasMoved = false;
        mViewTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case SwipeRefreshLayout.LARGE /*0*/:
                        START_X = event.getRawX();
                        START_Y = event.getRawY();
                        PREV_X = (float) mBtnParams.x;
                        PREV_Y = (float) mBtnParams.y;
                        hasMoved = false;
                        break;
                    case DrawerLayout.STATE_SETTLING /*2*/:
                        float x = (float) ((int) (event.getRawX() - START_X));
                        float y = (float) ((int) (event.getRawY() - START_Y));
                        mBtnParams.x = (int) (PREV_X + x);
                        mBtnParams.y = (int) (PREV_Y + y);
                        mWindowManager.updateViewLayout(mFloatingButtons, mBtnParams);
                        hasMoved = true;
                        break;
                }
                return true;
            }
        };
        mHideFloatingWindow = new Runnable() {
            public void run() {
                try {
                    mWindowManager.removeView(mFloatingLayout);
                    mFloatingLayout = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mHandler = new Handler() {
        };
        Log.w(TAG, "VisualizerAnytime()");
        VisualizerManager.getInstance().setAudioManager((AudioManager) context.getSystemService(Context.AUDIO_SERVICE), sessionID);
        if (PreferenceUtils.loadBooleanValue(context, PreferenceUtils.KEY_RC_PACKAGENAME, false)) {
            VisualizerManager.getInstance().setupMic();
        } else {
            VisualizerManager.getInstance().setupSession(0);
        }
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void release() {
        Log.w(TAG, "release()");
        VisualizerManager.getInstance().release();
    }

    public void setupMic() {
        VisualizerManager.getInstance().setupMic();
        mVisualizerView.setUseMic(true);
    }

    public void setupSession() {
        VisualizerManager.getInstance().setupSession();
        mVisualizerView.setUseMic(false);
    }

    public void setSessionID(int sessionid) {
        VisualizerManager.getInstance().setupSession(sessionid);
    }

    public void plusViewRatio() {
        mVisualizerView.plusRatio();
    }

    public void showFloatingWindow(Context context) {
        bShowWindow = true;
        if (PreferenceUtils.loadBooleanValue(context, PreferenceUtils.KEY_VI_FLOATING_ONOFF, false)) {
            showFloatingButton(context);
        }
        showFloatingVisualizer(context);
    }

    public void hideFloatingWindow(Context context) {
        bShowWindow = false;
        hideFloatingButton(context);
        hideFloatingVisualizer(context);
    }

    private void showFloatingButton(Context context) {
//        mFloatingButtons = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.visualizer_onoffbutton, null);
//        mOnOffLayout = (LinearLayout) mFloatingButtons.findViewById(R.id.visualizer_floating_onoff_layout);
//        mMicOnOff = (ImageView) mFloatingButtons.findViewById(R.id.visualizer_mic_onoff);
//        mFloatingOnOff = (ImageView) mFloatingButtons.findViewById(R.id.visualizer_floating_onoff);
//        mFloatingOnOff.setOnClickListener(new AnonymousClass1(context));
//        mMicOnOff.setOnClickListener(new AnonymousClass2(context));
//        mFloatingOnOff.setImageResource(R.drawable.noti_visualizer_on);
//        if (PreferenceUtils.loadBooleanValue(context, PreferenceUtils.KEY_RC_PACKAGENAME, false)) {
//            mMicOnOff.setImageResource(R.drawable.microphone_on);
//        } else {
//            mMicOnOff.setImageResource(R.drawable.microphone_off);
//        }
//        mMicOnOff.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
//                    case SwipeRefreshLayout.LARGE /*0*/:
//                    case SettingFragment.REQUEST_CODE_CUSTOMCOLOR /*1*/:
//                    case DrawerLayout.STATE_SETTLING /*2*/:
//                        mViewTouchListener.onTouch(v, event);
//                        break;
//                }
//                return false;
//            }
//        });
//        mFloatingOnOff.setOnTouchListener(new OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
//                    case SwipeRefreshLayout.LARGE /*0*/:
//                    case SettingFragment.REQUEST_CODE_CUSTOMCOLOR /*1*/:
//                    case DrawerLayout.STATE_SETTLING /*2*/:
//                        mViewTouchListener.onTouch(v, event);
//                        break;
//                }
//                return false;
//            }
//        });
//        mBtnParams = getWindowParamsButton(context);
//        try {
//            mWindowManager.addView(mFloatingButtons, mBtnParams);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void hideFloatingButton(Context context) {
        if (mFloatingButtons != null) {
            try {
                mWindowManager.removeView(mFloatingButtons);
                mFloatingButtons = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showFloatingVisualizer(Context context) {
//        bShowVisualizer = true;
//        if (mFloatingLayout == null) {
//            mFloatingLayout = (RelativeLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.visualizer_miniview, null);
//            mVisualizerView = (VisualizerMiniView) mFloatingLayout.findViewById(R.id.visualizer_mini_view);
//            try {
//                mWindowManager.addView(mFloatingLayout, getWindowParamsVisualizer(context));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        VisualizerManager.getInstance().setupView(mVisualizerView);
//        mVisualizerView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.push_up_in));
//        mVisualizerView.setVisibility(View.VISIBLE);
    }

    private void hideFloatingVisualizer(Context context) {
//        bShowVisualizer = false;
//        if (mFloatingLayout != null) {
//            Animation ani = AnimationUtils.loadAnimation(context, R.anim.push_down_out);
//            ani.setFillAfter(true);
//            mVisualizerView.startAnimation(ani);
//            mVisualizerView.setVisibility(4);
//            mHandler.postDelayed(mHideFloatingWindow, 400);
//        }
    }

    public void updateFloatingWindow(Context context) {
        if (mFloatingLayout != null && bShowVisualizer) {
            mWindowManager.updateViewLayout(mFloatingLayout, getWindowParamsVisualizer(context));
        }
    }

    LayoutParams getWindowParamsButton(Context context) {
        Display mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getMetrics(metrics);
        return new LayoutParams(((int) metrics.density) * 40, ((int) metrics.density) * 40, 2002, 8, -3);
    }

    private LayoutParams getWindowParamsVisualizer(Context context) {
        Log.e(TAG, "getWindowParams()");
        Point mPoint = new Point();
        Display mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mDisplay.getSize(mPoint);
        DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getMetrics(metrics);
        int maxWidth = mPoint.x;
        int maxHeight = mPoint.y;
        int wRatio = PreferenceUtils.loadIntegerValue(context, PreferenceUtils.KEY_VI_WIDTH_RATIO, 100);
        int width = (maxWidth * wRatio) / 100;
        int height = (maxHeight * PreferenceUtils.loadIntegerValue(context, PreferenceUtils.KEY_VI_HEIGHT_RATIO, 10)) / 100;
        if (((float) width) < 40.0f * metrics.density) {
            width = ((int) metrics.density) * 40;
        }
        if (((float) height) < 40.0f * metrics.density) {
            height = ((int) metrics.density) * 40;
        }
        int gravity = PreferenceUtils.loadIntegerValue(context, PreferenceUtils.KEY_VI_GRAVITY, 0);
        if (gravity == 1) {
            gravity = GravityCompat.START;
        } else if (gravity == 2) {
            gravity = GravityCompat.END;
        } else {
            gravity = 1;
        }
        LayoutParams params = new LayoutParams(width, height, 2006, AccessibilityNodeInfoCompat.ACTION_EXPAND, -3);
        params.gravity = gravity | 80;
        return params;
    }

    public boolean isFloatingView() {
        return bShowWindow;
    }
}