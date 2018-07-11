package com.bstech.voicechanger.custom.visualizer;

import android.graphics.Canvas;

public interface ITypeView {
    public static final int FULL_VIEW = 0;
    public static final int MINI_VIEW = 1;

    void draw(Canvas canvas);

    int getCustomColorSet();

    void onSizeChanged(int i, int i2, int i3);

    float plusRatio();

    void refreshChanged();

    void setAlpha(int i);

    void setBarSize(int i);

    void setColorSet(int i);

    void setMICSensitivity(int i);

    void setStick(boolean z);

    void setUseMic(boolean z);

    void update(byte[] bArr);
}