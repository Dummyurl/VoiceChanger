package com.bstech.voicechanger.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.activity.MainActivity;
import com.bstech.voicechanger.application.MyApplication;
import com.bstech.voicechanger.fragment.RecorderFragment;
import com.bstech.voicechanger.utils.DbHandler;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Statistic;
import com.bstech.voicechanger.utils.Utils;
import ca.uol.aig.fftpack.RealDoubleFFT;

import static com.bstech.voicechanger.utils.Utils.EMPTY;
import static com.bstech.voicechanger.utils.Utils.LOCAL_SAVE_FILE;
import static com.bstech.voicechanger.utils.Utils.START_SERVICE;
import static com.bstech.voicechanger.utils.Utils.STOP_RECORD;
import static com.bstech.voicechanger.utils.Utils.STOP_SERVICE;


public class RecordService extends Service {
    private static final int NOTIFICATION_ID_CUSTOM_BIG = 9;
    private static final String _FORMAT = "Format";
    private static final String CHANEL_ID = "1";
    public String FORMAT = ".mp3";
    public boolean isRecord = false;
    //  private RealDoubleFFT transformer;
    public boolean isRecording = false;
    String treePath;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private long time = 0;
    private String valueFile = null;
    private Message message;
    private String mFileName;
    private String mFilePath;
    private int minBuffer;
    private int inSamplerate = 44100;
    private long limitedTime = 0;
    private AudioRecord audioRecord;
    private AndroidLame androidLame;
    private FileOutputStream outputStream;
    private DbHandler dbHandler;
    private int bitRate = 320;
    private String mFileNomedia;

    public static double getFolderSize(File f) {
        double size = 0;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                size += getFolderSize(file);
            }
        } else {
            size = f.length();
        }
        return size;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHandler = DbHandler.getInstance(getApplicationContext());

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int checkRecord = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        int checkWriteExternal = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkRecord == 0 && checkWriteExternal == 0) {
            if (intent != null && intent.getAction() != null) {

                switch (intent.getAction()) {
                    case START_SERVICE:
                        if (!isRecord) {
                            isRecord = true;
                            startRecord();

                        } else {
                            isRecord = false;
                            statusStop();
                        }
                        break;

                    case STOP_SERVICE:
                        statusStop();
                        break;

                    default:
                        break;
                }
            } else {
                Toast.makeText(this, getString(R.string.grand_permission), Toast.LENGTH_SHORT).show();
            }
        }

        return START_NOT_STICKY;
    }

    private void statusStop() {
        isRecording = !isRecording;
        sendBroadcast(new Intent().setAction(STOP_RECORD));
        // SharedPrefs.getInstance().put(Utils.STATUS_PLAY, Utils.STATUS_STOP);
        //  MyWidget.updateWidget(getApplicationContext(), false);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.stopRecord), Toast.LENGTH_SHORT).show();
    }

    private void startRecord() {
        if (!isRecording) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isRecording = true;
                    mStartingTimeMillis = System.currentTimeMillis();
                    setFormat();
                    setLimitedTimeRecord();
                    setOutBitrate();
                    setFileNameAndPath();
                    startRecording();
                }
            }).start();
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.startRecord), Toast.LENGTH_SHORT).show();
            updateTime();
            createNotification();
        }
    }

    private void createNotification() {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_notification1);

        Intent stopRecord = new Intent(getApplicationContext(), RecordService.class);
        stopRecord.setAction(STOP_SERVICE);
        PendingIntent pStop = PendingIntent.getService(getApplicationContext(), 0, stopRecord, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.imgStopRecord, pStop);

        Intent notifyIntent = new Intent(getApplicationContext(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANEL_ID);
        notificationCompatBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationCompatBuilder.setOngoing(true);
        notificationCompatBuilder.setContentIntent(pendingIntent);
        notificationCompatBuilder.setCustomContentView(remoteViews);
        notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        startForeground(NOTIFICATION_ID_CUSTOM_BIG, notificationCompatBuilder.build());
    }

    private void setFormat() {
        FORMAT = SharedPrefs.getInstance().get(_FORMAT, String.class, EMPTY);
        if (FORMAT.equals(EMPTY)) {
            FORMAT = Utils.FORMAT_MP3;
        }
    }

    private void setOutBitrate() {
        switch (FORMAT) {
            case Utils.FORMAT_MP3:
                 bitRate = SharedPrefs.getInstance().get(Utils.BITRATE_MP3, Integer.class);
                break;
            case Utils.FORMAT_M4A:
                bitRate = SharedPrefs.getInstance().get(Utils.BITRATE_M4A, Integer.class);
                break;
            case Utils.FORMAT_OGG:
                bitRate = SharedPrefs.getInstance().get(Utils.BITRATE_OGG, Integer.class);
                break;
            default:
                break;
        }
        if (bitRate == 0) {
            bitRate = 128;
        }
    }

    private void readSizeFile() {
        File f = new File(String.valueOf(Uri.parse(mFilePath)));
        double Filesize = getFolderSize(f) / 1024;

        if (Filesize >= 1000) {
            BigDecimal rowOff = new BigDecimal(Filesize / 1024).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            valueFile = rowOff + " Mb";
        } else {
            BigDecimal rowOff = new BigDecimal(Filesize).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            valueFile = rowOff + " Kb";
        }
    }

    private void updateTime() {
        if (RecorderFragment.handler == null) {
            RecorderFragment.handler = new Handler();
        }

        RecorderFragment.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (limitedTime != 0 && time == limitedTime) {

                    isRecording = !isRecording;

                    sendBroadcast(new Intent().setAction(Utils.STOP_RECORD));
                    //MyWidget.updateWidget(getApplicationContext(), false);
                    RecorderFragment.handler.removeCallbacksAndMessages(null);
                    SharedPrefs.getInstance().put(Utils.STATUS_PLAY, Utils.STATUS_STOP);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.stopRecord), Toast.LENGTH_SHORT).show();
                    stopForeground(true);
                    stopSelf();
                    //MyWidget.updateWidget(getApplicationContext(), false);

                } else {
                    readSizeFile();

                    message = new Message();
                    Bundle b = new Bundle();
                    b.putLong(Utils.TIME, time);
                    b.putString(Utils.FILE_SIZE, valueFile);
                    message.setData(b);

                    RecorderFragment.handler.sendMessage(message);
                    RecorderFragment.handler.postDelayed(this, 1000);
                    time = time + 1000;
                }
            }
        }, 100);
    }

    //
    public void setFileNameAndPath() {

        long timeAddRecord = System.currentTimeMillis();

        Date date = new Date(timeAddRecord);


        String lasmod = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(date);

        File f;
        do {
            String filePath = SharedPrefs.getInstance().get(LOCAL_SAVE_FILE, String.class, null);
//            Log.d("filepath",filePath);
            mFileName = lasmod + "_" + bitRate + "kbs" + FORMAT;
            if (filePath == null) {

                mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                mFileNomedia = mFilePath + "/BVoiceChanger/";
                mFilePath += "/BVoiceChanger/" + mFileName;

            } else {
                //Log.d("filepath", filePath);
                mFileNomedia = filePath;
                mFilePath = filePath + mFileName;
            }

            f = new File(mFilePath);

        } while (f.exists() && !f.isDirectory());
    }

    private void setLimitedTimeRecord() {
        limitedTime = SharedPrefs.getInstance().get(Utils.LIMITED_TIME, Integer.class, 0);
        if (limitedTime != 0) {
            limitedTime = limitedTime * 60000;
        }
    }

    private FileOutputStream createFileOutputStreamFromDocumentTree() throws FileNotFoundException {
        //  String rootTreePath = MyApplication.getUriTree();
        treePath = SharedPrefs.getInstance().get(Utils.TREE_URI, String.class, null);
//         Log.d("lynah",treePath);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && treePath != null) {
            Log.d("lynah", "start");
            try {
                Log.e("lynah", "aaaaaa");

                DocumentFile pickedDir = DocumentFile.fromTreeUri(this, Uri.parse(treePath));
                DocumentFile mediaFile = pickedDir.createFile("audio/*", new File(mFilePath).getName());

                if (mediaFile != null) {
                    FileOutputStream outputStream = (FileOutputStream) MyApplication.getAppContext().getContentResolver().openOutputStream(mediaFile.getUri());
                    if (outputStream == null) {
                        throw new Exception("outputStream null: Create new file");
                    } else {
                        return outputStream;
                    }
                } else {
                    throw new Exception("mediaFile null: Create new file");
                }
            } catch (Exception e) {
                e.printStackTrace();
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Statistic.FOLDER_STORE;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(path, new File(mFilePath).getName());
                mFilePath = file.getAbsolutePath();
                return new FileOutputStream(mFilePath);
            }
        } else {
            return new FileOutputStream(new File(mFilePath));
        }
    }


    private ca.uol.aig.fftpack.RealDoubleFFT transformer;

    private void startRecording() {

        minBuffer = AudioRecord.getMinBufferSize(inSamplerate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, inSamplerate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2);

        short[] buffer = new short[inSamplerate * 2 * 5];

        byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

        try {
            outputStream = createFileOutputStreamFromDocumentTree();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        androidLame = new LameBuilder()
                .setInSampleRate(inSamplerate)
                .setOutChannels(1)
                .setOutBitrate(bitRate)
                .setOutSampleRate(inSamplerate)
                .setAbrMeanBitrate(bitRate)
                .build();

        audioRecord.startRecording();

        int bytesRead = 0;

        int blockSize = 1024;
        double[] toTransform = new double[blockSize];

        if (transformer == null) {
            transformer = new RealDoubleFFT(blockSize);
        }
        while (isRecording) {
            bytesRead = audioRecord.read(buffer, 0, minBuffer);

            for (int i = 0; i < blockSize && i < bytesRead; i++) {
                toTransform[i] = (double) buffer[i] / 32768.0d; // signed 16 bit
            }
            transformer.ft(toTransform);

            if (RecorderFragment.handler != null) {
                RecorderFragment.handler.sendMessage(RecorderFragment.handler.obtainMessage(1, toTransform));
            }

            if (bytesRead > 0) {

                int bytesEncoded = androidLame.encode(buffer, buffer, bytesRead, mp3buffer);

                if (bytesEncoded > 0) {
                    try {
                        outputStream.write(mp3buffer, 0, bytesEncoded);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        RecorderFragment.handler.removeCallbacksAndMessages(null);

        int outputMp3buf = androidLame.flush(mp3buffer);

        if (outputMp3buf > 0) {
            try {

                outputStream.write(mp3buffer, 0, outputMp3buf);
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);

        audioRecord.stop();
        audioRecord.release();

        androidLame.close();
        File file = new File(mFileNomedia, ".nomedia");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && treePath != null) {
            dbHandler.addRecord(mFileName, mFilePath, time - 100, valueFile, treePath);
        } else {

            dbHandler.addRecord(mFileName, mFilePath, Utils.getMediaDuration(mFilePath), valueFile, Utils.EMPTY);
        }

        sendBroadcast(new Intent().setAction(Utils.UPDATE_LIST));

        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
