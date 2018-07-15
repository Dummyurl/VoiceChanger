package com.bstech.voicechanger.fragment;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.service.MusicService;

import java.util.List;

import static com.bstech.voicechanger.service.MusicService.NO_REPEAT;
import static com.bstech.voicechanger.service.MusicService.REPEAT_ALL;
import static com.bstech.voicechanger.service.MusicService.REPEAT_ONE;
import static com.bstech.voicechanger.utils.Utils.getPathFromUri;

public class AudioTunePresenterImpl implements AudioTunePresenter {
    private int indexPlay = 0;
    private MusicService service;
    private AudioTuneView audioTuneView;
    private AudioTuneView.Shuffle shuffle;
    private AudioTuneView.Repeat repeat;

    public AudioTunePresenterImpl(AudioTuneView audioTuneView, MusicService service, AudioTuneView.Shuffle shuffle, AudioTuneView.Repeat repeat) {
        this.audioTuneView = audioTuneView;
        this.service = service;
        this.shuffle = shuffle;
        this.repeat = repeat;
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
            audioTuneView.onUpdateTempo(tempo * 100);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onSetPitchSemi(float pitchSemi) {
        if (isServiceRunning()) {
            pitchSemi = pitchSemi - 12;
            service.setPitchSemi(pitchSemi);
            audioTuneView.onUpdatePitchSemi(pitchSemi);
        } else {
            audioTuneView.onServiceNull();
        }
    }

    @Override
    public void onSortedList(List<Song> songs) {
        String pathSong = null;
        if (songs != null && isServiceRunning()) {
            pathSong = service.getPathSong();

            if (pathSong != null) {
                for (int i = 0; i < service.getSongList().size() - 1; i++) {
                    if (pathSong.equals(service.getSongList().get(i).getPath())) {
                        service.setIndexPlay(i);
                        service.setSongList(songs);
                    }
                }
            }
        }
    }

    @Override
    public void onAddSongToListPlay(Song song, List<Song> songs, Uri uri , Context context) {
        boolean fileIsExistInList = false;
        if (songs!= null && songs.size()>0) {
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

            if ( songs!=null && songs.size() == 1) {
                audioTuneView.onUpdateTitleSong(song);
            }
        }
    }

    @Override
    public void refreshPitchSemi() {
        audioTuneView.onUpdateRefreshPitch(12.00f);
    }

    @Override
    public void refreshTempo() {
        audioTuneView.onUpdateTempo(75);
    }

    @Override
    public void refreshRate() {

    }

    @Override
    public void getData() {
        if (isServiceRunning()){
            audioTuneView.onGetDataSuccess(service.getSongList(),service.getPitchSemi(),service.getTempo());
        }
        //            songList.addAll(service.getSongList());
//            songAdapter.notifyDataSetChanged();
//            seekBarPitch.setProgress((int) service.getPitchSemi() + 12);
//            if (service.getPitchSemi() < 0) {
//                tvPitch.setText("Pitch: -" + service.getPitchSemi());
//            } else {
//                tvPitch.setText("Pitch: +" + service.getPitchSemi());
//            }
//
//            seekBarTempo.setProgress(((int) service.getTempo() * 100) + 25);
//            tvTempo.setText("Tempo: " + ((int) (service.getTempo() * 100) + 25) + "%");
//
//            updateTimePlay();
    }
}
