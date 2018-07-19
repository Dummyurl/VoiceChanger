package com.bstech.voicechanger.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.model.Song;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giga on 7/19/2018.
 */

public class SelectSongAdapter extends RecyclerView.Adapter<SelectSongAdapter.ViewHolder> {
    private List<Song> songList;
    private Context context;
    private OnClick callback;
    private List<Song> listSongChecked = new ArrayList<>();

    public SelectSongAdapter(List<Song> songs, Context context, OnClick callback) {
        this.songList = songs;
        this.callback = callback;
        this.context = context;

    }

    public void setFilter(List<Song> list) {
        songList = new ArrayList<>();
        songList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectSongAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_select, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        Glide.with(context).load(song.getUriImage()).placeholder(R.drawable.ic_music).into(holder.ivSong);
        holder.tvArtist.setText(song.getNameArtist());
        holder.tvNameSong.setText(song.getNameSong());
        holder.checkBox.setChecked(song.isCheck());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public interface OnClick {
        void onClick(int index);

        void onListChecked(List<Song> listChecked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivSong;
        private TextView tvNameSong, tvArtist;
        private CheckBox checkBox;


        public ViewHolder(View itemView) {
            super(itemView);
            ivSong = itemView.findViewById(R.id.image_song);
            tvNameSong = itemView.findViewById(R.id.name_song);
            tvArtist = itemView.findViewById(R.id.name_artist);
            checkBox = itemView.findViewById(R.id.checkbox);

            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                boolean isExist = false;
                for (Song song : listSongChecked) {
                    if (song.getPath().equals(songList.get(getAdapterPosition()).getPath())) {
                        isExist = true;
                    }
                }

                if (compoundButton.isChecked() && !isExist) {
                    songList.get(getAdapterPosition()).setCheck(true);
                    listSongChecked.add(songList.get(getAdapterPosition()));

                } else if (!compoundButton.isChecked() && isExist) {
                    songList.get(getAdapterPosition()).setCheck(false);
                    listSongChecked.remove(songList.get(getAdapterPosition()));
                }

                callback.onListChecked(listSongChecked);

            });

            itemView.setOnClickListener(view -> {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
            });
        }
    }
}
