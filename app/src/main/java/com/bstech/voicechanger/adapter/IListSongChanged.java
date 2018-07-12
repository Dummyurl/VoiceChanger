package com.bstech.voicechanger.adapter;


import com.bstech.voicechanger.model.Song;

import java.util.List;


public interface IListSongChanged {
    void onNoteListChanged(List<Song> songs);
}
