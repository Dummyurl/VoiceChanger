package com.bstech.voicechanger.fragment;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.service.MusicService;

import java.util.List;

import static com.bstech.voicechanger.fragment.AudioTuneFragment.PITCH;
import static com.bstech.voicechanger.fragment.AudioTuneFragment.RATE;
import static com.bstech.voicechanger.fragment.AudioTuneFragment.TEMPO;
import static com.bstech.voicechanger.service.MusicService.NO_REPEAT;
import static com.bstech.voicechanger.service.MusicService.REPEAT_ALL;
import static com.bstech.voicechanger.service.MusicService.REPEAT_ONE;
import static com.bstech.voicechanger.utils.Utils.getPathFromUri;

public class AudioTunePresenterImpl implements AudioTunePresenter, AudioTuneInteractor.OnGetDataListener
        , AudioTuneInteractor.OnInputValueListener {
    private int indexPlay = 0;
    private MusicService service;
    private AudioTuneView audioTuneView;
    private AudioTuneView.Shuffle shuffle;
    private AudioTuneView.Repeat repeat;
    private AudioTuneInteractor audioTuneInteractor;

    public AudioTunePresenterImpl(AudioTuneView audioTuneView, MusicService service
            , AudioTuneView.Shuffle shuffle, AudioTuneView.Repeat repeat, AudioTuneInteractor listener) {
        this.audioTuneView = audioTuneView;
        this.service = service;
        this.shuffle = shuffle;
        this.repeat = repeat;
        this.audioTuneInteractor = listener;

        audioTuneInteractor.onGetData(this.service, this);
    }

    @Override
    public void playAudio(List<Song> songs, int index) {
        if (songs != null && songs.size() > 0) {
            if (service != null && !service.isStartPlay) {
                service.setSongList(songs);
                service.setIndexPlay(index);
                service.playAudioEntity();
                audioTuneView.onUpdatePlay(true);
            } else {
                if (service != null && service.isPlaying()) {
                    service.pausePlayer();
                    audioTuneView.onUpdatePlay(false);
                } else {
                    service.startPlayer();
                    audioTuneView.onUpdatePlay(true);
                }
            }
        }
    }

    @Override
    public void onPlayIndexAudio(List<Song> songs, int index) {
        if (service != null) {
            indexPlay = index;
            service.setSongList(songs);
            service.setIndexPlay(index);
            service.playAudioEntity();
            audioTuneView.onUpdatePlay(true);
        }
    }

    private boolean isServiceRunning() {
        if (service != null && service.mPlayer != null) {
            return true;
        }
        return false;
    }

    @Override
    public void onPlayNext() {
        if (isServiceRunning()) {
            service.playNext();
            audioTuneView.onUpdatePlay(true);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onPlayPrevious() {
        if (isServiceRunning()) {
            service.playPrevious();
            audioTuneView.onUpdatePlay(true);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onFastNextSong() {
        if (isServiceRunning()) {
            service.fastNext();
            audioTuneView.onUpdatePlay(true);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onFastPreviousSong() {
        if (isServiceRunning()) {
            service.fastPrevious();
            audioTuneView.onUpdatePlay(true);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onShuffle() {
        if (service != null) {
            if (!service.getShuffle()) {
                service.setShuffle(true);
                shuffle.onShuffle();
            } else {
                service.setShuffle(false);
                shuffle.offShuffle();
            }
        }
    }

    @Override
    public void onRepeat() {
        if (service != null) {
            if (service.getStateRepeat() == NO_REPEAT) {
                service.setStateRepeat(REPEAT_ALL);
                repeat.onRepeatAll();
            } else if (service.getStateRepeat() == REPEAT_ALL) {
                service.setStateRepeat(REPEAT_ONE);
                repeat.onRepeatOne();
            } else if (service.getStateRepeat() == REPEAT_ONE) {
                service.setStateRepeat(NO_REPEAT);
                repeat.onNoRepeat();
            }
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onSetTempo(float tempo) {
        if (isServiceRunning()) {
            tempo = (tempo + 25) / 100;
            service.setTempo(tempo);
            audioTuneView.onUpdateTempo(tempo * 100, false);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onSetPitchSemi(float pitchSemi) {
        if (isServiceRunning()) {
            pitchSemi = pitchSemi - 12;
            service.setPitchSemi(pitchSemi);
            audioTuneView.onUpdatePitchSemi(pitchSemi, false);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onSetRate(float rate) {
        if (isServiceRunning()) {
            rate = (rate + 25) / 100;
            service.setTempo(rate);
            audioTuneView.onUpdateRate(rate * 100, false);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    /* Get list song sorted and update list in service */
    @Override
    public void onSortedList(List<Song> songs) {
        String pathSong = null;
        if (songs != null && isServiceRunning()) {
            pathSong = service.getPathSong();

            if (pathSong != null) {
                for (int i = 0; i < songs.size() - 1; i++) {
                    if (pathSong.equals(songs.get(i).getPath())) {
                        service.setIndexPlay(i);
                        service.setSongList(songs);
                    }
                }
            }
        } else if (songs != null) {
            service.setSongList(songs);
        }
    }

    /* Action add song to list playing */
    @Override
    public void onAddSongToListPlay(Song song, List<Song> songs, Uri uri, Context context) {
        boolean fileIsExistInList = false;
        if (songs != null && songs.size() > 0) {
            for (Song s : songs) {
                if (s.getPath().equals(getPathFromUri(uri, context))) {
                    fileIsExistInList = true;
                }
            }
        }

        if (fileIsExistInList) {
            audioTuneView.onAddSongFail();
        } else {
            audioTuneView.onAddSongSuccess(song);

            if (songs != null && songs.size() == 1) {
                audioTuneView.onUpdateTitleSong(song);
            }
        }
    }

    @Override
    public void refreshPitchSemi() {
        audioTuneView.onUpdatePitchSemi(12.00f, true);
    }

    @Override
    public void refreshTempo() {
        //service.setTempo(75);
        audioTuneView.onUpdateTempo(100, true);
        ;
    }

    @Override
    public void refreshRate() {
        //service.setRate(75);
        audioTuneView.onUpdateRate(100, true);
    }


    /* When open app if service is playing then get data and update on view */
    @Override
    public void getData() {
        if (isServiceRunning()) {
            audioTuneView.onGetDataSuccess(service.getSongList(), service.getPitchSemi(), service.getTempo());
        } else {
            Log.e("xxx", "no load data");
        }

    }

    /**
     * Set value media from keyboard
     *
     * @param value     value input from keyboard
     * @param typeInput is check for type of TEMPO, PITCH or RATE
     **/
    @Override
    public void inputValue(String value, int typeInput) {
        audioTuneInteractor.onInputValue(value, this, typeInput);
    }

    @Override
    public void onGetDataSuccess() {

    }

    @Override
    public void onGetDataFail() {

    }


    @Override
    public void onInputSuccess(String value, int type) {
        float v;
        if (type == TEMPO) {
            v = Float.parseFloat(value);
            if (v > 225) {
                v = 225;
            } else if (v <= 25) {
                v = 25;
            }
            service.setTempo(v);
            audioTuneView.onShowInputSuccess(v, type);

        } else if (type == PITCH) {
            v = Float.parseFloat(value);

            if (v >= 12.00f) {
                v = 12.00f;
            } else if (v < -12.00f) {
                v = -12.00f;
            }
            service.setPitchSemi(v);
            audioTuneView.onShowInputSuccess(v, type);
        } else if (type == RATE) {
            v = Float.parseFloat(value);
            if (v > 225) {
                v = 225;
            } else if (v <= 25) {
                v = 25;
            }
            service.setRate(v);
            audioTuneView.onShowInputSuccess(v, type);
        }

    }

    @Override
    public void onInputFail() {
        audioTuneView.onShowInputFail();
    }
}
