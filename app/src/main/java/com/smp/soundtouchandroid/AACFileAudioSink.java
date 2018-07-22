package com.smp.soundtouchandroid;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AACFileAudioSink implements com.smp.soundtouchandroid.AudioSink {
    private AudioEncoder encoder;
    private ExecutorService exec;
    private volatile boolean finishedWriting;

    public AACFileAudioSink(int samplingRate, int channels) throws IOException {
        encoder = new MediaCodecAudioEncoder(samplingRate, channels);
        exec = Executors.newSingleThreadExecutor();
    }

    @Override
    public int write(byte[] input, final int offSetInBytes, final int sizeInBytes) throws IOException {
        final byte[] tmp = Arrays.copyOf(input, input.length);
        if (!exec.isShutdown()) {
            exec.submit(() -> {
                try {
                    encoder.writeChunk(tmp, offSetInBytes, sizeInBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        return sizeInBytes - offSetInBytes;
    }

    @Override
    public void close() throws IOException {
        // an unorderly shutdown
        //if (!finishedWriting)
        //encoder.close();
    }

    public void finishWriting() throws IOException {
        finishedWriting = true;
        exec.submit(() -> {
            try {
                encoder.finishWriting();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        exec.shutdown();
        try {
            exec.awaitTermination(240, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        encoder.close();
    }

    public void setFileOutputName(String fileNameOut) throws IOException {
        encoder.initFileOutput(fileNameOut);
    }

}
