package com.bstech.voicechanger.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.model.Song;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Giga on 7/4/2018.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private List<Song> songList;
    private Context context;

    public SongAdapter(List<Song> songList, Context context) {
        this.songList = songList;
        this.context = context;
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
        Glide.with(context).load(song.getPathImage()).into(holder.ivThumb);
    }

    @Override
    public int getItemCount() {
        return songList.size();
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
