package com.bstech.voicechanger.custom.visualizer;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.AsyncTask;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;

import java.lang.reflect.Method;

public class VisualizerManager {
    private static final String TAG = "VisualizerManager";
    private static VisualizerManager mVisualizerUtil;
    private boolean CANCELLED_FLAG;
    private int audioEncoding;
    private AudioRecord audioRecord;
    private boolean bLocal;
    private int blockSize;
    private int channelConfiguration;
    private int frequency;
    private AudioManager mAudioManager;
    private byte[] mBytes;
    private Equalizer mEqualizer;
    private int mSessionID;
    private IVisualizerView mView;
    private Visualizer mVisualizer;
    private RecordAudio recordTask;
    private ca.uol.aig.fftpack.RealDoubleFFT transformer;

    private class RecordAudio extends AsyncTask<Void, double[], Boolean> {
        RecordAudio() {
        }

        protected Boolean doInBackground(Void... params) {
            Log.d(VisualizerManager.TAG, "doInBackground()");
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(0, frequency, channelConfiguration, audioEncoding, bufferSize);
            short[] buffer = new short[blockSize];
            double[] toTransform = new double[blockSize];
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                Log.e("Recording failed", e.toString());
            }
            while (!CANCELLED_FLAG && !isCancelled()) {
                int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                int i = 0;
                while (i < blockSize && i < bufferReadResult) {
                    toTransform[i] = ((double) buffer[i]) / 32768.0d;
                    i++;
                }
                transformer.ft(toTransform);
                publishProgress(new double[][]{toTransform});
            }
            Log.d("doInBackground", "Cancelling the RecordTask");
            CANCELLED_FLAG = false;
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(double[]... progress) {
            byte[] bytes = new byte[progress[0].length];
            for (int i = 0; i < progress[0].length; i++) {
                bytes[i] = (byte) ((int) progress[0][i]);
            }
            if (mView != null) {
                mView.update(bytes);
            }
            mBytes = bytes;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d(VisualizerManager.TAG, "onPostExecute()");
            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                Log.e("Stop failed", e.toString());
            }
        }
    }

    static {
        mVisualizerUtil = new VisualizerManager();
    }

    public static VisualizerManager getInstance() {
        return mVisualizerUtil;
    }

    private VisualizerManager() {
        mSessionID = 0;
        bLocal = false;
        frequency = 8000;
        channelConfiguration = 2;
        audioEncoding = 2;
        blockSize = AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        CANCELLED_FLAG = false;
        turnoffLPASystemProperty();
    }

    public void setupView(IVisualizerView view) {
        Log.d(TAG, "setupView()");
        mView = view;
    }

    public void setAudioManager(AudioManager am, int sessionid) {
        mAudioManager = am;
        mSessionID = sessionid;
    }

    public boolean toggleSessionID() {
        Log.d(TAG, "toggleSessionID()");
        if (bLocal) {
            bLocal = false;
            releaseSession();
            setupSession(0);
        } else {
            bLocal = true;
            releaseSession();
            setupSession(mSessionID);
        }
        return bLocal;
    }

    public void update(double[] toTransform) {
        byte[] bytes = new byte[toTransform.length];
        for (int i = 0; i < toTransform.length; i++) {
            bytes[i] = (byte) ((int) toTransform[i]);
        }
        if (mView != null) {
            mView.update(bytes);
        }
        mBytes = bytes;
    }

    public boolean isMusicActive() {
        return !(mAudioManager == null || !mAudioManager.isMusicActive());
    }

    public boolean isVisualizerActive() {
        if (mAudioManager != null && mAudioManager.getStreamVolume(3) == 0) {
            return true;
        }
        if (mBytes == null) {
            return false;
        }
        for (byte b : mBytes) {
            if (b != 0) {
                return true;
            }
        }
        return false;
    }

    public void setupMic() {
        Log.d(TAG, "setupMic()");
        releaseSession();
        if (transformer == null) {
            transformer = new ca.uol.aig.fftpack.RealDoubleFFT(blockSize);
        }
        recordTask = new RecordAudio();
        recordTask.execute();
    }

    public void releaseMic() {
        Log.d(TAG, "releaseMic()");
        if (recordTask != null) {
            CANCELLED_FLAG = true;
        }
    }

    public void setupSession() {
        bLocal = false;
        releaseSession();
        setupSession(0);
    }

    public void setupSession(int sessionId) {
        Log.d(TAG, "setupSession() " + sessionId);
        releaseMic();
        if (mVisualizer == null) {
            try {
                mVisualizer = new Visualizer(sessionId);
                mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                mVisualizer.setDataCaptureListener(new OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    }

                    public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                        if (!isMusicActive()) {
                            bytes = null;
                        }
                        if (mView != null) {
                            mView.update(bytes);
                        }
                        mBytes = bytes;
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true);
                Log.d(TAG, "=>CaptureSizeRangeMin:" + Visualizer.getCaptureSizeRange()[0]);
                Log.d(TAG, "=>CaptureSizeRangeMax:" + Visualizer.getCaptureSizeRange()[1]);
                Log.d(TAG, "=>MaxCaptureRate:" + Visualizer.getMaxCaptureRate());
                mVisualizer.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseSession() {
        Log.d(TAG, "release()");
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
        }
        if (mEqualizer != null) {
            Log.d(TAG, "=>equalizer for visualizer is released!!");
            mEqualizer.release();
            mEqualizer = null;
        }
    }

    public void release() {
        releaseSession();
        releaseMic();
    }

    private void turnoffLPASystemProperty() {
        try {
            Object obj = Class.forName("android.os.SystemProperties").getConstructor(new Class[0]).newInstance();
            Method get = obj.getClass().getMethod("get", String.class);
            Method set = obj.getClass().getMethod("set", String.class, String.class);
            boolean disable = Boolean.valueOf((String) get.invoke(obj, "audio.offload.disable"));
            Log.w(TAG, "disable!!:" + disable);
            if (!disable) {
                Log.w(TAG, "turnoffOffloadSystemProperty()");
                set.invoke(obj, "audio.offload.disable", "true");
            }
            if (Boolean.valueOf((String) get.invoke(obj, "lpa.decode"))) {
                Log.w(TAG, "turnoffLPASystemProperty()");
                set.invoke(obj, "lpa.decode", "false");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}