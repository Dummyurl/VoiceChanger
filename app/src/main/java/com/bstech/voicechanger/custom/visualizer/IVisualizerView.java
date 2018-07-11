package com.bstech.voicechanger.custom.visualizer;

public interface IVisualizerView {
    int getCustomColorSet();

    float plusRatio();

    void refresh();

    void refreshChanged();

    void setAlpha(int i);

    void setBarSize(int i);

    void setColorSet(int i);

    void setMICSensitivity(int i);

    void setStick(boolean z);

    void setUseMic(boolean z);

    void update(byte[] bArr);
}