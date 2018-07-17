package com.bstech.voicechanger.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.activity.MainActivity;
import com.bstech.voicechanger.adapter.IListSongChanged;
import com.bstech.voicechanger.adapter.SimpleItemTouchHelperCallback;
import com.bstech.voicechanger.adapter.SongAdapter;
import com.bstech.voicechanger.application.MyApplication;
import com.bstech.voicechanger.databinding.FragmentAudioTuneBinding;
import com.bstech.voicechanger.model.Record;
import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.service.MusicService;
import com.bstech.voicechanger.utils.DbHandler;
import com.bstech.voicechanger.utils.FileUtil;
import com.bstech.voicechanger.utils.Flog;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Statistic;
import com.bstech.voicechanger.utils.Utils;
import com.smp.soundtouchandroid.SoundStreamAudioPlayer;
import com.smp.soundtouchandroid.SoundStreamFileWriter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.bstech.voicechanger.utils.Utils.LOCAL_SAVE_FILE;


public class AudioTuneFragment extends BaseFragment implements SlidingUpPanelLayout.PanelSlideListener, AudioTuneView, View.OnClickListener, AudioTuneView.Shuffle, AudioTuneView.Repeat, IListSongChanged, SongAdapter.OnClickItem, SongAdapter.OnStartDragListener, SeekBar.OnSeekBarChangeListener, AudioTuneInteractor.StateCompine {
    public static final int PITCH = 0;
    public static final int TEMPO = 1;
    public static final int RATE = 2;
    boolean isStarted = false;
    private float pitchSemi = 0.0f;
    private float tempo = 1.0f;
    private int positionPlay = 0;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MainActivity mainActivity;
    private ImageView ivStateSlidingPanel, ivPlay, ivNext, ivPrevious, ivFastPrevious, ivFastNext, ivShuffle, ivRepeat;
    private TextView tvStartDuration, tvEndDuration;
    private SoundStreamAudioPlayer player;
    private ProgressDialog progressDialog;
    private String durationStr;
    private ImageView ivRefreshPitch, ivRefreshTempo, ivRefreshRate;
    private SeekBar seekBarPitch, seekBarTempo, seekBarRate, seekBarTimePlay;
    private boolean isPlayCompleted = false;
    private TextView tvPitch, tvRate, tvTempo;
    private AlertDialog.Builder alertDialogBuilder;
    private RecyclerView rvSong;
    private SongAdapter songAdapter;
    private List<Song> songList;
    private ImageView ivAddSong;
    private DbHandler dbHandler;
    private MusicService service;
    private EditText edtInput;
    private TextView tvNameSong, tvNameArtist;
    private Handler handler;
    private ItemTouchHelper.Callback callback;
    private ItemTouchHelper itemTouchHelper;
    private AlertDialog alertDialog;
    private String mFileName;
    private String mFilePath;
    private String mFileNomedia;
    private FragmentAudioTuneBinding binding;
    private AudioTunePresenterImpl presenter;
    private View viewShowName;
    private TextView tvNoFile;
    private ConstraintLayout viewRate, viewPitchTempo;
    private ImageView ivOpenRate, ivCloseRate;
    private Runnable updateUIPlay = new Runnable() {
        @Override
        public void run() {
            if (service != null && service.mPlayer != null) {
                seekBarTimePlay.setMax(service.getDuration());
                long curDuration = service.mPlayer.getPlayedDuration() / 1000;
                tvStartDuration.setText(Utils.convertMillisecond(curDuration));
                seekBarTimePlay.setProgress((int) service.mPlayer.getPlayedDuration() / 1000);
                service.setOnComplete();
                handler.postDelayed(this, 1000);
            }
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Utils.OPEN_LIST_FILE:
                        songList.clear();
                        for (Record record : dbHandler.getRecords()) {
                            songList.add(new Song(record.getTitle(), Utils.ARTIST_UNKNOW, record.getDuration(), record.getFilePath()));
                            Collections.reverse(songList);
                        }
                        songAdapter.notifyDataSetChanged();

                        positionPlay = intent.getIntExtra(Utils.INDEX, 0);
                        tvNameSong.setText(songList.get(positionPlay).getNameSong());
                        tvNameArtist.setText(songList.get(positionPlay).getNameArtist());
                        stateNoFileSelected();
                        break;

                    case Utils.UI_PLAY_SONG:
                        updateTimePlay();
                        songAdapter.notifyDataSetChanged();
                        break;

                    case Utils.STOP_MUSIC:
                        stopMusic();
                        break;

                    case Utils.UPDATE_COMPINE_PITCH_TEMPO:
                        updateCompinePitchTempo();
                        break;
                }
            }
        }
    };

    private void updateCompinePitchTempo() {
        presenter.checkStateCompinePitchTempo(true);
    }

    private void stopMusic() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        ivPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        tvStartDuration.setText("00:00");
        seekBarTimePlay.setProgress(0);
        songAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_tune, container, false);
        binding.setAudiotunefragment(this);
        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Statistic.REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Uri treeUri = data.getData();
                Log.e("path", treeUri.toString() + "xxxxx");

                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

                if (null == FileUtil.getRealPathFromURI_API19(getContext(), treeUri)) {
                    Toast.makeText(getContext(), getString(R.string.file_fail), Toast.LENGTH_SHORT).show();
                } else {
                    mediaMetadataRetriever.setDataSource(getContext(), Uri.parse(FileUtil.getRealPathFromURI_API19(getContext(), treeUri)));
                    String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    long duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                    InputStream inputStream = null;

                    if (mediaMetadataRetriever.getEmbeddedPicture() != null) {
                        inputStream = new ByteArrayInputStream(mediaMetadataRetriever.getEmbeddedPicture());
                    }
                    mediaMetadataRetriever.release();

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    File file = new File(FileUtil.getRealPathFromURI_API19(getContext(), treeUri));

                    if (artist == null) {
                        artist = "Unknow";
                    }

                    if (title == null) {
                        title = file.getName();
                    }

                    Song song = new Song();
                    song.setNameSong(title);
                    song.setNameArtist(artist);
                    song.setDuration(duration);
                    song.setPath(FileUtil.getRealPathFromURI_API19(getContext(), treeUri));
                    song.setUriImage(Utils.getArtUriFromMusicFile(new File(FileUtil.getRealPathFromURI_API19(getContext(), treeUri)), getContext()));

                    presenter.onAddSongToListPlay(song, songList, treeUri, getContext());


                }

            }
        }
    }

    private void updateTimePlay() {
        if (service.isPlaying()) {
            ivPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
        } else {
            ivPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }

        tvNameSong.setText(service.getNameSong());
        tvNameArtist.setText(service.nameArtist());
        tvEndDuration.setText(Utils.convertMillisecond(service.getDuration()));

        if (handler == null) {
            handler = new Handler();
        } else {
            handler.removeCallbacksAndMessages(null);
        }
        handler.postDelayed(updateUIPlay, 100);
        songAdapter.notifyDataSetChanged();

    }


    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void initViews() {

        setHasOptionsMenu(true);

        service = ((MyApplication) getContext().getApplicationContext()).getService();

        presenter = new AudioTunePresenterImpl(this, service, this, this
                , new AudioTuneInteratorImpl(), this);

        dbHandler = DbHandler.getInstance(getContext());


        songList = new ArrayList<>();
        songAdapter = new SongAdapter(songList, getContext(), this, this, this);

        rvSong = (RecyclerView) findViewById(R.id.rv_song);
        rvSong.setHasFixedSize(true);
        rvSong.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSong.setAdapter(songAdapter);

        callback = new SimpleItemTouchHelperCallback(songAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvSong);

        viewShowName = findViewById(R.id.view);
        viewRate = (ConstraintLayout) findViewById(R.id.view_rate);

        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvTempo = (TextView) findViewById(R.id.tv_tempo);
        tvPitch = (TextView) findViewById(R.id.tv_pitch);
        tvNoFile = (TextView) findViewById(R.id.tv_no_song);
        tvNameSong = (TextView) findViewById(R.id.tv_name_song);
        tvEndDuration = (TextView) findViewById(R.id.tv_run_end);
        tvStartDuration = (TextView) findViewById(R.id.tv_run_start);
        tvNameArtist = (TextView) findViewById(R.id.tv_name_artist);

        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        ivRepeat = (ImageView) findViewById(R.id.iv_repeat);
        ivShuffle = (ImageView) findViewById(R.id.iv_shuffle);
        ivAddSong = (ImageView) findViewById(R.id.iv_add_song);
        ivPrevious = (ImageView) findViewById(R.id.iv_previous);
        ivFastNext = (ImageView) findViewById(R.id.iv_fast_next);
        ivCloseRate = (ImageView) findViewById(R.id.iv_close_rate);
        ivOpenRate = (ImageView) findViewById(R.id.iv_open_close_rate);
        ivRefreshRate = (ImageView) findViewById(R.id.iv_refresh_rate);
        ivRefreshPitch = (ImageView) findViewById(R.id.iv_refresh_pitch);
        ivRefreshTempo = (ImageView) findViewById(R.id.iv_refresh_tempo);
        ivFastPrevious = (ImageView) findViewById(R.id.iv_fast_previous);
        ivStateSlidingPanel = (ImageView) findViewById(R.id.iv_state_sliding);
        viewPitchTempo = (ConstraintLayout) findViewById(R.id.view_pitch_tempo);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setDragView(findViewById(R.id.v));
        slidingUpPanelLayout.setShadowHeight(0);

        seekBarPitch = (SeekBar) findViewById(R.id.seekbar_pitch);
        seekBarTempo = (SeekBar) findViewById(R.id.seekbar_tempo);
        seekBarRate = (SeekBar) findViewById(R.id.seekbar_rate);
        seekBarTimePlay = (SeekBar) findViewById(R.id.seekbar_time_play);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        addActions();
        registerReceiver();
        stateNoFileSelected();

        presenter.getData();
        presenter.checkStateCompinePitchTempo(false);

    }

    private void registerReceiver() {
        IntentFilter it = new IntentFilter();
        it.addAction(Utils.OPEN_LIST_FILE);
        it.addAction(Utils.UI_PLAY_SONG);
        it.addAction(Utils.STOP_MUSIC);
        it.addAction(Utils.UPDATE_COMPINE_PITCH_TEMPO);
        getContext().registerReceiver(receiver, it);
    }

    private void addActions() {
        ivStateSlidingPanel.setOnClickListener(this);
        slidingUpPanelLayout.addPanelSlideListener(this);
        seekBarTimePlay.setOnSeekBarChangeListener(this);
        seekBarPitch.setOnSeekBarChangeListener(this);
        seekBarTempo.setOnSeekBarChangeListener(this);
        seekBarRate.setOnSeekBarChangeListener(this);

        ivAddSong.setOnClickListener(view -> addSongToList());
        ivNext.setOnClickListener(view -> presenter.onPlayNext());
        ivRepeat.setOnClickListener(view -> presenter.onRepeat());
        ivShuffle.setOnClickListener(view -> presenter.onShuffle());
        ivFastNext.setOnClickListener(view -> presenter.onFastNextSong());
        ivPrevious.setOnClickListener(view -> presenter.onPlayPrevious());
        ivRefreshRate.setOnClickListener(view -> presenter.refreshRate());
        ivRefreshTempo.setOnClickListener(view -> presenter.refreshTempo());
        ivRefreshPitch.setOnClickListener(view -> presenter.refreshPitchSemi());
        ivFastPrevious.setOnClickListener(view -> presenter.onFastPreviousSong());
        ivPlay.setOnClickListener(view -> presenter.playAudio(songList, positionPlay, false));
        ivOpenRate.setOnClickListener(view -> presenter.compinePitchTempo(viewPitchTempo, getContext()));
        ivCloseRate.setOnClickListener(view -> presenter.compinePitchTempo(viewPitchTempo, getContext()));

        findViewById(R.id.iv_input_pitch).setOnClickListener(view -> createDialogInputValue(PITCH));
        findViewById(R.id.iv_input_rate).setOnClickListener(view -> createDialogInputValue(RATE));
        findViewById(R.id.iv_input_tempo).setOnClickListener(view -> createDialogInputValue(TEMPO));
    }

    /* Show status if no file selected */
    private void stateNoFileSelected() {
        if (songList.size() == 0) {
            tvNoFile.setVisibility(View.VISIBLE);
            viewShowName.setVisibility(View.INVISIBLE);
            slidingUpPanelLayout.setEnabled(false);
        } else {
            tvNoFile.setVisibility(View.INVISIBLE);
            viewShowName.setVisibility(View.VISIBLE);
            slidingUpPanelLayout.setEnabled(true);
        }
    }


    private void addSongToList() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            startActivityForResult(intent, Statistic.REQUEST_CODE_SELECT_FILE);
        } else {
            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .newDirectoryName("BVoiceChanger")
                    .allowReadOnlyDirectory(true)
                    .allowNewDirectoryNameModification(true)
                    .build();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_audio_tune, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_studio:

                break;

            case R.id.item_save:
                saveFile();
                break;

            case R.id.item_setting1:
                addFragmentSetting();
                break;

            case R.id.item_more_app:

                break;

            case R.id.item_about:
                break;

            case R.id.item_rate_app:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFragmentSetting() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.animation_left_to_right, R.anim.animation_right_to_left
                        , R.anim.animation_left_to_right, R.anim.animation_right_to_left)
                .replace(R.id.container, SettingFragment.newInstance(), RecorderFragment.class.getName())
                .addToBackStack(null).commit();
    }

    public void setFileNameAndPath() {
        long timeAddRecord = System.currentTimeMillis();
        Date date = new Date(timeAddRecord);
        String lasmod = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(date);

        File f;
        do {
            String filePath = SharedPrefs.getInstance().get(LOCAL_SAVE_FILE, String.class, null);

            mFileName = lasmod + ".mp3";
            if (filePath == null) {

                mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                mFileNomedia = mFilePath + Statistic.FOLDER_APP;
                mFilePath += Statistic.FOLDER_APP + mFileName;

            } else {
                mFileNomedia = filePath;
                mFilePath = filePath + mFileName;
            }

            f = new File(mFilePath);

        } while (f.exists() && !f.isDirectory());
    }

    private void saveFile() {
        if (isServiceRunning()) {
            presenter.playAudio(songList, positionPlay, true);
            setFileNameAndPath();

            try {
                SoundStreamFileWriter writer;
                progressDialog.show();
                ivPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                if (viewPitchTempo.getVisibility() == View.GONE) {
                    writer = new SoundStreamFileWriter(0, service.getPathSong(), mFilePath, service.getTempo(), service.getPitchSemi(), getContext());
                } else {
                    writer = new SoundStreamFileWriter(0, service.getPathSong(), mFilePath, service.getTempo(), service.getPitchSemi(), service.getRate(), getContext());
                }

                writer.setFileWritingListener(new SoundStreamFileWriter.FileWritingListener() {
                    @Override
                    public void onFinishedWriting(boolean success) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), getString(R.string.saved_file) + " : " + mFilePath, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgressChanged(int track, double currentPercentage, long position) {
                        progressDialog.setProgress((int) (currentPercentage * 100));
                    }

                    @Override
                    public void onTrackEnd(int track) {

                    }

                    @Override
                    public void onExceptionThrown(String string) {
                        progressDialog.dismiss();
                    }
                });
                new Thread(writer).start();
                writer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param style is check create dialog input pitch, tempo or rate
     **/
    private void createDialogInputValue(int style) {
        int s;
        s = style;
        View view = getLayoutInflater().inflate(R.layout.dialog_input_value, null);

        alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(view);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        edtInput = view.findViewById(R.id.edt_input);

        TextView tvTitle = view.findViewById(R.id.tv_title2);
        TextView tvValue = view.findViewById(R.id.tv_value);

        view.findViewById(R.id.tv_cancel).setOnClickListener(v -> cancelInputValue(s));
        view.findViewById(R.id.tv_ok).setOnClickListener(v -> applyInputValue(s));

        switch (style) {
            case TEMPO:
                tvTitle.setText(getString(R.string.enter_new_tempo));
                tvValue.setText("%");
                break;

            case PITCH:
                tvTitle.setText(getString(R.string.enter_new_pitch));
                tvValue.setText("semi - tones");
                break;

            case RATE:
                tvTitle.setText(getString(R.string.enter_new_rate));
                tvValue.setText("%");
                break;
        }
    }

    private void cancelInputValue(int style) {
        alertDialog.dismiss();
    }

    /**
     * @param style check dialog input is showing
     **/
    private void applyInputValue(int style) {
        alertDialog.dismiss();

        switch (style) {
            case TEMPO:
                presenter.inputValue(edtInput.getText().toString().trim(), TEMPO);
                break;

            case PITCH:
                presenter.inputValue(edtInput.getText().toString().trim(), PITCH);
                break;

            case RATE:
                presenter.inputValue(edtInput.getText().toString().trim(), RATE);
                break;
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        if (songList != null && songList.size() > 0) {
            switch (newState) {
                case EXPANDED:
                    ivStateSlidingPanel.setImageResource(R.drawable.slide_down);
                    break;
                case COLLAPSED:
                    ivStateSlidingPanel.setImageResource(R.drawable.slide_up);
                    break;
            }
        }
    }

    private boolean isServiceRunning() {
        if (service != null && service.mPlayer != null) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_touch:
                break;
        }
    }

    /* Listen action list sorted or dismissed item */
    @Override
    public void onNoteListChanged(List<Song> songs) {
        presenter.onSortedList(songs);
    }

    @Override
    public void onClick(int index, View view) {
        positionPlay = index;
        presenter.onPlayIndexAudio(songList, index);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void offShuffle() {
        ivShuffle.setImageResource(R.drawable.no_shuffle);
    }

    @Override
    public void onShuffle() {
        ivShuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
    }

    @Override
    public void onNoRepeat() {
        ivRepeat.setImageResource(R.drawable.ic_no_repeat);
    }

    @Override
    public void onRepeatOne() {
        ivRepeat.setImageResource(R.drawable.ic_repeat_one_black_24dp);
    }

    @Override
    public void onRepeatAll() {
        ivRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);
    }

    @Override
    public void onUpdatePlay(boolean isPlaying) {
        updateTimePlay();
    }

    @Override
    public void onServiceNull() {
        Flog.e("xxx", "nullllllllllllllll");
    }

    @Override
    public void onUpdatePitchSemi(float pitchSemi, boolean isRefresh) {
        if (isRefresh) {
            seekBarPitch.setProgress(12);
            tvPitch.setText("Pitch: +0.0 ");
        } else {
            tvPitch.setText("Pitch: " + (pitchSemi >= 0 ? "+" + pitchSemi : pitchSemi));
        }
    }

    @Override
    public void onUpdateTempo(float tempo, boolean isRefresh) {
        if (isRefresh) {
            seekBarTempo.setProgress(75);
            tvTempo.setText("Tempo: 100%");
        } else {
            tvTempo.setText("Tempo: " + (int) tempo + "%");
        }
    }

    @Override
    public void onUpdateRate(float rate, boolean isRefresh) {
        if (isRefresh) {
            seekBarRate.setProgress(75);
            tvRate.setText("Rate: 100%");
        } else {
            tvRate.setText("Rate: " + (int) rate + "%");
        }
    }


    @Override
    public void onAddSongSuccess(Song song) {
        songList.add(song);
        service.setSongList(songList);
        songAdapter.notifyDataSetChanged();
        stateNoFileSelected();
    }

    @Override
    public void onAddSongFail() {
        Toast.makeText(getContext(), getString(R.string.file_is_exist), Toast.LENGTH_SHORT).show();
    }

    /* If size list songs == 1 update current songs in bottom status bar */
    @Override
    public void onUpdateTitleSong(Song song) {
        tvNameSong.setText(song.getNameSong());
        tvNameArtist.setText(song.getNameArtist());
    }

    @Override
    public void onGetDataSuccess(List<Song> songs, float pitSemi, float tempo, float rate) {
        songList.clear();
        songList.addAll(songs);
        songAdapter.notifyDataSetChanged();

        if (viewPitchTempo.getVisibility() == View.GONE) {

            seekBarRate.setProgress((int) (rate * 100) - 25);
            tvRate.setText("Rate: " + (int) (rate * 100) + " %");

        } else {
            seekBarPitch.setProgress((int) pitSemi + 12);
            seekBarTempo.setProgress(((int) (tempo * 100) - 25));

            tvTempo.setText("Tempo: " + ((int) ((tempo * 100)) + " %"));

            if (service.getPitchSemi() < 0) {
                tvPitch.setText("Pitch: " + (pitSemi));
            } else {
                tvPitch.setText("Pitch: + " + pitSemi);
            }
        }

        updateTimePlay();
        stateNoFileSelected();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFailedGetData() {
        seekBarTempo.setProgress(75);
        seekBarRate.setProgress(75);
        seekBarPitch.setProgress(12);
        tvRate.setText("Rate: 100%");
        tvTempo.setText("Tempo: 100%");
        tvPitch.setText("Pitch: +0.0");

    }

    @Override
    public void onShowInputSuccess(float value, int type) {
        if (type == PITCH) {
            seekBarPitch.setProgress((int) value + 12);
            if (service.getPitchSemi() < 0) {
                tvPitch.setText("Pitch: -" + value);
            } else {
                tvPitch.setText("Pitch: +" + value);
            }
        } else if (type == TEMPO) {
            seekBarTempo.setProgress((int) (value - 25));
            tvTempo.setText("Tempo: " + ((int) (value)) + "%");
        } else if (type == RATE) {
            seekBarRate.setProgress((int) (value - 25));
            tvRate.setText("Rate: " + value + "%");
        }
    }

    @Override
    public void onShowInputFail() {
        Toast.makeText(getContext(), getString(R.string.please_input_value), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShowSetRate() {
        Log.e("xxx","wwtf");
        viewPitchTempo.setVisibility(View.GONE);
        viewRate.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHideSetRate() {
        Log.e("xxx","wwsssssssssssstf");

        viewPitchTempo.setVisibility(View.VISIBLE);
        viewRate.setVisibility(View.GONE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.seekbar_time_play:
                if (isServiceRunning()) {
                    service.seek(seekBar.getProgress() * 1000);
                }
                break;

            case R.id.seekbar_pitch:
                presenter.onSetPitchSemi(seekBar.getProgress());
                break;

            case R.id.seekbar_rate:
                presenter.onSetRate(seekBar.getProgress());
                break;

            case R.id.seekbar_tempo:
                presenter.onSetTempo(seekBar.getProgress());
                break;
        }
    }

    @Override
    public void onTurnOffCompine(boolean isRefresh) {
        Log.e("xxx", "turn offf");

//        if (isRefresh) {
//            presenter.refreshRate();
//        }

        viewRate.setVisibility(View.GONE);
        viewPitchTempo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTurnOnCompine(boolean isRefresh) {
        Log.e("xxx", "turn on");
//        if (isRefresh) {
//            presenter.refreshPitchSemi();
//            presenter.refreshTempo();
//        }

        viewRate.setVisibility(View.VISIBLE);
        viewPitchTempo.setVisibility(View.GONE);
    }
}
