package com.bstech.voicechanger.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.application.MyApplication;
import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.service.MusicService;
import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

/**
 * Created by Giga on 7/4/2018.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> implements IItemTouchHelperAdapter {
    private List<Song> songList;
    private Context context;
    private OnStartDragListener callback;
    private OnClickItem onClickItem;
    private IListSongChanged iListSongChanged;
    private MusicService service;

    public SongAdapter(List<Song> songList, Context context, OnStartDragListener callback, OnClickItem onClick, IListSongChanged iListSongChanged) {
        this.songList = songList;
        this.context = context;
        this.callback = callback;
        this.onClickItem = onClick;
        this.iListSongChanged = iListSongChanged;
        this.service = ((MyApplication) context.getApplicationContext()).getService();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);

        holder.tvNameSong.setText(song.getNameSong());
        holder.tvNameArtist.setText(song.getNameArtist());
        // holder.tvIndex.setText(position + 1 + "");
        Glide.with(context).load(song.getUriImage()).placeholder(R.drawable.ic_music).into(holder.ivThumb);
        //if (position > 3) {
        holder.ivRemove.setOnTouchListener((view, motionEvent) -> {
            if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                callback.onStartDrag(holder);
            }
            return false;
        });
        // }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    // check dismiss
    @Override
    public void onItemDismiss(int position) {
        if (service.getIndexPlay() != position) {
            songList.remove(position);
            iListSongChanged.onNoteListChanged(songList);
            notifyItemRemoved(position);
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(songList, fromPosition, toPosition);
        iListSongChanged.onNoteListChanged(songList);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnClickItem {
        void onClick(int index, View view);

        void onLongClick(int index, View view);

        void onOptionClick(int index, View view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRemove, ivThumb;
        private TextView tvNameSong, tvNameArtist, tvIndex;

        public ViewHolder(View itemView) {
            super(itemView);
            ivRemove = itemView.findViewById(R.id.iv_remove);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            tvNameSong = itemView.findViewById(R.id.tv_name_song);
            tvNameArtist = itemView.findViewById(R.id.tv_name_artist);
        }
    }
}
