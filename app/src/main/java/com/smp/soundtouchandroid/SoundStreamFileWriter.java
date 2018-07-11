package com.smp.soundtouchandroid;

import android.content.Context;

import com.bstech.voicechanger.utils.DbHandler;
import com.bstech.voicechanger.utils.FileUtil;
import com.bstech.voicechanger.utils.Utils;

import java.io.File;
import java.io.IOException;

public class SoundStreamFileWriter extends SoundStreamRunnable implements OnProgressChangedListener {
    private long start, end;
    private AACFileAudioSink file;
    private FileWritingListener fileListener;
    private DbHandler dbHandler;
    private String fileName = null;

    public SoundStreamFileWriter(int id, String fileNameIn, String fileNameOut, float tempo, float pitchSemi, Context context) throws IOException {
        super(id, fileNameIn, tempo, pitchSemi);
        file.setFileOutputName(fileNameOut);
        fileName = fileNameOut;
        dbHandler = DbHandler.getInstance(context);
        setOnProgressChangedListener(this);

    }

    public SoundStreamFileWriter(int id, String fileNameIn, String fileNameOut, float tempo, float pitchSemi, float rate, Context context) throws IOException {
        super(id, fileNameIn, tempo, pitchSemi);
        setRate(rate);
        file.setFileOutputName(fileNameOut);
        fileName = fileNameOut;
        dbHandler = DbHandler.getInstance(context);
        setOnProgressChangedListener(this);
    }

    public void setFileWritingListener(FileWritingListener listener) {
        this.fileListener = listener;
    }

    @Override
    protected AudioSink initAudioSink() throws IOException {
        file = new AACFileAudioSink(getSamplingRate(), getChannels());
        return file;
    }

    @Override
    protected void onStart() {
        start = System.nanoTime();
    }

    @Override
    protected void onPause() {

    }

    @Override
    protected void onStop() {
        try {
            file.finishWriting();
            handler.post(() -> fileListener.onFinishedWriting(true));
           // dbHandler.addRecord(new File(fileName).getName(),fileName, Utils.getMediaDuration(fileName), FileUtil.getFolderSizeLabel(new File(fileName)),"");


        } catch (IOException e) {
            e.printStackTrace();
            handler.post(() -> fileListener.onFinishedWriting(true));
           // dbHandler.addRecord(new File(fileName).getName(),fileName, Utils.getMediaDuration(fileName), FileUtil.getFolderSizeLabel(new File(fileName)),"");
        }
        dbHandler.addRecord(new File(fileName).getName(),fileName, Utils.getMediaDuration(fileName), FileUtil.getFolderSizeLabel(new File(fileName)),"");

        end = System.nanoTime();
//        long elapsedTime = end - start;
//        double seconds = (double) elapsedTime / 1000000000.0;
        //Log.i("ENCODE", "SECONDS: " + String.valueOf(seconds));
    }

    @Override
    public void onProgressChanged(int track, double currentPercentage, long position) {
        fileListener.onProgressChanged(track, currentPercentage, position);
    }

    @Override
    public void onTrackEnd(int track) {
        finished = true;
        //Don't want to call it on the main thread - the most likely place we are.
        new Thread(this::stop).start();
    }

    @Override
    public void onExceptionThrown(String string) {
        fileListener.onExceptionThrown(string);
    }


    public interface FileWritingListener extends OnProgressChangedListener {
        public void onFinishedWriting(boolean success);
    }
}
