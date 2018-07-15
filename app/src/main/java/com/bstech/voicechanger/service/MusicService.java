package com.bstech.voicechanger.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.utils.MediaButtonIntentReceiver;
import com.bstech.voicechanger.utils.NotificationHelper;
import com.bstech.voicechanger.utils.Utils;
import com.smp.soundtouchandroid.OnProgressChangedListener;
import com.smp.soundtouchandroid.SoundStreamAudioPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.bstech.voicechanger.utils.Utils.UI_PLAY_SONG;


public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static final String NOTIFY_PREVIOUS = "PREVIOUS";
    public static final String NOTIFY_NEXT = "NEXT";
    public static final String NOTIFY_PLAY = "PLAY";
    public static final String NOTIFY_STOP = "STOP";
    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ONE = 2;
    public static final int REPEAT_ALL = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "volume_boot_notif_channel";
    private static final int NOTIFICATION_ID = 2342;
    private static final int UNPLUGIN_HEADPHONE = 0;
    private static final int PLUGIN_HEADPHONE = 1;
    private static int d = 0;
    private static int nClick = 0;
    private final IBinder musicBind = new MusicBinder();
    public boolean isStart = false;
    public SoundStreamAudioPlayer mPlayer = null;
    public float pitchSemi = 0.0f;
    public float tempo = 1.0f;
    public boolean isStartPlay = false;
    private NotificationManager notificationManager;
    private Notification mNotification;
    private List<Song> songList;
    private int indexPlay;
    private boolean shuffle = false;
    private boolean repeat = false;
    private Random rand;
    //private Equalizer equalizer;
    private SharedPreferences prefs;
    private NotificationChannel notificationChannel;
    private Bitmap bitmap;
    private MediaSessionCompat mSession;
    private AudioManager mAudioManager;
    private int STATE_REPEAT = 0;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    int state = intent.getIntExtra("state", -1);
                    switch (state) {
                        case UNPLUGIN_HEADPHONE:
                            if (mPlayer != null) {
                                if (!mPlayer.isPaused()) {
                                    mPlayer.pause();
                                    //sendBroadcast(new Intent(UI_PLAY_AUDIO));
                                }
                            }
                            break;
                        case PLUGIN_HEADPHONE:

                            break;
                    }
                }
            }
        }
    };
    private List<Song> shuffleList = new ArrayList<>();
    private float rate;

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    private void initActions() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initActions();
        rand = new Random();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //equalizer = new Equalizer(getApplicationContext());
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel existingNotificationChannel = null;
            if (notificationManager != null) {
                existingNotificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
            }
            if (existingNotificationChannel == null) {
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        getApplicationContext().getString(R
                                .string.app_name),
                        NotificationManager.IMPORTANCE_LOW);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);

                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName()));
        }

        setUpMediaSession();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return musicBind;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case NOTIFY_NEXT:
                    playNext();
                    sendBroadcast(new Intent(Utils.UI_PLAY_SONG));
                    Log.e("xxx", "next");
                    break;

                case NOTIFY_PLAY:
                    playPauseMusic();
                    break;

                case NOTIFY_PREVIOUS:
                    playPrevious();
                    break;

                case NOTIFY_STOP:
                    stopPlayer();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void playPauseMusic() {
        if (!mPlayer.isPaused()) {
            pausePlayer();
        } else {
            startPlayer();
        }

        sendBroadcast(new Intent(UI_PLAY_SONG));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            Log.e("service null", "start");
            mPlayer.start();
        } catch (Exception e) {
            Log.e("service null", "error start ");
        }
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songs) {
        if (songList == null) {
            songList = new ArrayList<>();
        } else {
            songList.clear();

        }
        songList.addAll(songs);
        //songList = songs;
    }

    public void addSongShuffle() {
        if (STATE_REPEAT == NO_REPEAT && getShuffle()) {
            shuffleList.clear();
            shuffleList.add(songList.get(indexPlay));
        }
    }

    public String getPathSong() {
        return songList.get(indexPlay).getPath();
    }

    public int getIndexPlay() {
        return indexPlay;
    }

    public void setIndexPlay(int index) {
        indexPlay = index;
    }

    public int getDuration() {
        return (int) songList.get(indexPlay).getDuration();
    }

    public void pausePlayer() {
        mPlayer.pause();

        sendBroadcast(new Intent(UI_PLAY_SONG));

        mNotification = NotificationHelper.buildNotification(getApplicationContext(), mSession, !mPlayer.isPaused(), songList.get(indexPlay));

        startForeground(NOTIFICATION_ID, mNotification);
        stopForeground(false);
    }

    public int getAudioSessionId() {
        if (mPlayer != null) {
            return mPlayer.getSessionId();
        } else {
            return 0;
        }
    }

    public void startPlayer() {
        mPlayer.start();
        sendBroadcast(new Intent(UI_PLAY_SONG));
        mNotification = NotificationHelper.buildNotification(getApplicationContext(), mSession, !mPlayer.isPaused(), songList.get(indexPlay));
        startForeground(NOTIFICATION_ID, mNotification);
    }

    /* Seek increase 5 seconds */
    public void fastNext() {
        mPlayer.seekTo(mPlayer.getPlayedDuration() + 5000000);
    }

    /* Seek subtract 5 seconds */
    public void fastPrevious() {
        mPlayer.seekTo(mPlayer.getPlayedDuration() - 5000000);
    }

    /* Stop play song */
    public void stopPlayer() {
        if (mPlayer != null && !mPlayer.isPaused()) {
            mPlayer.pause();
        }
        sendBroadcast(new Intent(UI_PLAY_SONG));
        stopForeground(true);
    }

    /* Get current position is playing */
    public int getCurrentPosition() {
        return (int) (mPlayer.getPlayedDuration() / 1000000);
    }

    /* Seek to duration current song playing */
    public void seek(int duration) {
        mPlayer.seekTo(duration);
    }

    public String nameArtist() {
        return songList.get(indexPlay).getNameArtist();
    }

    public String getNameSong() {
        return songList.get(indexPlay).getNameSong();
    }

    public String getThumbSong() {
        return songList.get(indexPlay).getPath();
    }

    /* Complete play song */
    public void setOnComplete() {
//        mPlayer.set(mediaPlayer -> {
//                    playNextComplete();
//                    sendBroadcast(new Intent(Utils.UPDATE_PAUSE_NOTIFICATION));
//                }
//        );


        mPlayer.setOnProgressChangedListener(new OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int track, double currentPercentage, long position) {

            }

            @Override
            public void onTrackEnd(int track) {
                Log.e("xxx", "complete");
                isStartPlay = false;

                mPlayer.stop();
                playNextComplete();
                sendBroadcast(new Intent(Utils.UPDATE_PAUSE_NOTIFICATION));
            }

            @Override
            public void onExceptionThrown(String string) {

            }
        });
    }

    /* Play next song after play complete song */
    private void playNextComplete() {

        if (STATE_REPEAT == NO_REPEAT) {

            if (shuffle) {
                playSongShuffle(true, true);
            } else {

                if (indexPlay >= songList.size() - 1) {
                    Log.e("xxx", "stop");
                    sendBroadcast(new Intent(Utils.STOP_MUSIC));
                    stopForeground(true);
                } else {
                    indexPlay++;
                    playAudioEntity();
                }
            }

        } else if (STATE_REPEAT == REPEAT_ALL) {
            if (shuffle) {
                playSongShuffle(false, false);
            } else {
                indexPlay++;
                if (indexPlay >= songList.size()) indexPlay = 0;
                playAudioEntity();
            }


        } else if (STATE_REPEAT == REPEAT_ONE) {
            playAudioEntity();
        }

    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return !mPlayer.isPaused();
        }
        return false;

    }

    /* Play previous song */
    public void playPrevious() {
        if (STATE_REPEAT == NO_REPEAT) {
            if (shuffle) {
                playSongShuffle(true, false);
            } else {
                if (indexPlay == 0) {
                    playAudioEntity();
                } else if (indexPlay >= 1) {
                    indexPlay--;
                    playAudioEntity();
                }
            }
        } else if (STATE_REPEAT == REPEAT_ALL) {

            if (shuffle) {
                playSongShuffle(false, false);
            } else {

                indexPlay--;
                if (indexPlay < 0) indexPlay = songList.size() - 1;

                playAudioEntity();
            }

        } else if (STATE_REPEAT == REPEAT_ONE) {

            if (shuffle) {
                playSongShuffle(false, false);

            } else {
                if (indexPlay == 0) {
                    indexPlay = songList.size() - 1;
                    playAudioEntity();

                } else if (indexPlay >= 1) {
                    indexPlay--;
                    playAudioEntity();
                }
            }
        }
    }

    /* Play next song */
    public void playNext() {

        if (STATE_REPEAT == NO_REPEAT) {

            if (shuffle) {
                playSongShuffle(true, false);
            } else {
                if (indexPlay == songList.size() - 1) {
                    //sendBroadcast(new Intent(Utils.STOP_MUSIC));
                    // stopForeground(true);
                } else {

                    indexPlay++;
                    playAudioEntity();
                }
            }

        } else if (STATE_REPEAT == REPEAT_ALL) {

            if (shuffle) {
                playSongShuffle(false, false);
            } else {

                indexPlay++;
                if (indexPlay >= songList.size()) indexPlay = 0;
                playAudioEntity();
            }

        } else if (STATE_REPEAT == REPEAT_ONE) {

            if (shuffle) {
                playSongShuffle(false, false);
            } else {

                indexPlay++;
                if (indexPlay >= songList.size()) indexPlay = 0;
                playAudioEntity();
            }
        }
    }

    public void playSongShuffle(boolean isNoRepeat, boolean isComplete) {
        if (isNoRepeat) {

            if (songList.size() == 1) {
                if (isComplete) {
                    stopForeground(true);
                    sendBroadcast(new Intent(Utils.STOP_MUSIC));
                } else {
                    playAudioEntity();
                }

                //(true);
                //sendBroadcast(new Intent(Utils.STOP_MUSIC));
            } else {
                if (shuffleList.size() >= songList.size()) {
                    shuffleList.clear();
                    stopForeground(true);
                    sendBroadcast(new Intent(Utils.STOP_MUSIC));
                } else {

                    int newSong = indexPlay;
                    while (newSong == indexPlay) {
                        newSong = rand.nextInt(songList.size());

                        for (Song song : shuffleList) {
                            if (song.equals(songList.get(newSong))) {
                                newSong = indexPlay;
                                break;
                            }
                        }
                    }

                    indexPlay = newSong;
                    boolean isExist = false;
                    for (Song song : shuffleList) {
                        if (songList.equals(songList.get(indexPlay))) {
                            isExist = true;
                        }
                    }

                    if (!isExist) {
                        shuffleList.add(songList.get(indexPlay));
                    }
                    playAudioEntity();
                }
            }

        } else {
            if (songList.size() == 1) {
                indexPlay = 0;
            } else {

                int newAudioEnity = indexPlay;
                while (newAudioEnity == indexPlay) {
                    newAudioEnity = rand.nextInt(songList.size() - 1);
                }
                indexPlay = newAudioEnity;
            }

            playAudioEntity();
        }
    }

    public float getPitchSemi() {
        return pitchSemi;
    }

    public void setPitchSemi(float pitchSemi) {
        this.pitchSemi = pitchSemi;
        if (mPlayer != null) {
            mPlayer.setPitchSemi(this.pitchSemi);
        }
    }

    public float getTempo() {
        return tempo;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
        if (mPlayer != null) {
            mPlayer.setTempo(this.tempo);
        }
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void playAudioEntity() {
        if (mPlayer != null) {
            if (!mPlayer.isPaused()) {
                mPlayer.stop();
            }
        }

        try {
            mPlayer = new SoundStreamAudioPlayer(0, songList.get(indexPlay).getPath(), 1.0f, 0.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(mPlayer).start();

        if (mPlayer != null) {
            isStartPlay = true;
            mPlayer.start();
            mNotification = NotificationHelper.buildNotification(getApplicationContext(), mSession, !mPlayer.isPaused(), songList.get(indexPlay));
            startForeground(NOTIFICATION_ID, mNotification);
        } else {
            playNext();
        }

        sendBroadcast(new Intent(UI_PLAY_SONG));
    }

    private void setUpMediaSession() {
        mSession = new MediaSessionCompat(this, "MediaNotification");
        mSession.setActive(true);

        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                startPlayer();
            }

            @Override
            public void onPause() {
                pausePlayer();
            }

            @Override
            public void onSkipToNext() {
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                playPrevious();
            }

            @Override
            public void onStop() {
                stopPlayer();
            }

            @Override
            public boolean onMediaButtonEvent(final Intent mediaButtonIntent) {
                KeyEvent keyEvent = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                // ...do something with keyEvent, super... does nothing.
                if (keyEvent == null) {
                    return false;
                }

                int action = keyEvent.getAction();
                final Handler handler = new Handler();
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                        if (action == KeyEvent.ACTION_DOWN) {
                            d++;
                            nClick = nClick + 1;

                            Runnable r = new Runnable() {

                                @Override
                                public void run() {
                                    // single click *******************************
                                    if (nClick == 1) {
                                        if (!mPlayer.isPaused()) {
                                            pausePlayer();
                                        } else {
                                            startPlayer();
                                        }
                                    }
                                    // double click *********************************
                                    if (nClick >= 2) {
                                        playNext();
                                    }
                                    d = 0;
                                    nClick = 0;
                                }
                            };
                            if (d == 1) {
                                //Flog.d("MediaSessionCompat", "getKeyCode: " + keyEvent.getKeyCode());
                                handler.postDelayed(r, 500);
                            }
                        }
                        break;
                }

                return false;
            }
        });

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    public boolean isShuffle() {
        if (shuffle) {
            shuffle = false;

        } else {
            shuffle = true;
        }
        return shuffle;
    }

    public boolean isRepeat() {
        if (repeat) {
            repeat = false;
        } else {
            repeat = true;
        }
        return repeat;
    }

    public int getStateRepeat() {
        return STATE_REPEAT;
    }

    public void setStateRepeat(int state) {
        STATE_REPEAT = state;
    }

    public boolean getRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean getShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    @Override
    public void onDestroy() {
        //Shutdown the EQ
        //Intent shutdownEqualizer = new Intent(MusicService.this, Equalizer.class);
        //stopService(shutdownEqualizer);
        unregisterReceiver(receiver);
        //equalizer.releaseEffects();
        //equalizer.closeEqualizerSessions(true, getAudioSessionId());
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        super.onDestroy();

    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
