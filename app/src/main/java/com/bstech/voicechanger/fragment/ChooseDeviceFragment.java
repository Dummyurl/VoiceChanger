package com.bstech.voicechanger.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.activity.MainActivity;
import com.bstech.voicechanger.adapter.SelectSongAdapter;
import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.utils.DbHandler;
import com.bstech.voicechanger.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giga on 7/19/2018.
 */

public class ChooseDeviceFragment extends BaseFragment implements SelectSongAdapter.OnClick {

    public boolean isActionMode = false;
    public int countItemSelected = 0;
    public boolean isSelectAll = false;
    private RecyclerView rvSong;
    private SelectSongAdapter adapter;
    private List<Song> songList = new ArrayList<>();
    private DbHandler dbHandler;
    private SearchView searchView;
    private android.support.v7.widget.Toolbar toolbar;
    private MainActivity context;
    private ActionMode actionMode = null;
    private List<Song> mListChecked = new ArrayList<>();
    private List<Song> listChecked = new ArrayList<>();

    public static ChooseDeviceFragment newInstance() {

        ChooseDeviceFragment fragment = new ChooseDeviceFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void initViews() {

        dbHandler = DbHandler.getInstance(getContext());

        songList.addAll(Utils.getSongFromDevice(getContext()));
        adapter = new SelectSongAdapter(songList, getContext(), this);

        rvSong = (RecyclerView) findViewById(R.id.rv_song);
        rvSong.setHasFixedSize(true);
        rvSong.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSong.setAdapter(adapter);
        addToolbar();

    }

    private void addToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.device));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(view -> getFragmentManager().popBackStack());
        toolbar.inflateMenu(R.menu.menu_select_multi_song);
        toolbar.findViewById(R.id.item_done).setOnClickListener(v -> getList());
    }

    private void getList() {
        if (listChecked.size() > 0) {
            getFragmentManager().popBackStack();
            getContext().sendBroadcast(new Intent(Utils.UPDATE_SELECT_SONG).putParcelableArrayListExtra(Utils.LIST_SONG, (ArrayList<? extends Parcelable>) listChecked));
        } else {
            Toast.makeText(getContext(), getString(R.string.please_choose_file), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(int index) {

    }

    @Override
    public void onListChecked(List<Song> listChecked) {
        Log.e("xxx", "list device " + listChecked.size());
        this.listChecked.clear();
        this.listChecked.addAll(listChecked);
    }
}
