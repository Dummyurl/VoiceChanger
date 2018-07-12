package com.bstech.voicechanger.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import com.bstech.voicechanger.model.Record;
import com.bstech.voicechanger.model.Song;
import com.bstech.voicechanger.service.MusicService;
import com.bstech.voicechanger.utils.DbHandler;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bstech.voicechanger.service.MusicService.NO_REPEAT;
import static com.bstech.voicechanger.service.MusicService.REPEAT_ALL;
import static com.bstech.voicechanger.service.MusicService.REPEAT_ONE;


public class AudioTuneFragment extends BaseFragment implements SlidingUpPanelLayout.PanelSlideListener, View.OnClickListener, IListSongChanged, SongAdapter.OnClickItem, SongAdapter.OnStartDragListener {
    private static final int PITCH = 0;
    private static final int TEMPO = 1;
    private static final int RATE = 2;
    public static float pitchSemi = 0.0f;
    public static float tempo = 1.0f;
    boolean isStarted = false;
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
    private AlertDialog alertDialog;
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

    Runnable updateUIPlay = new Runnable() {
        @Override
        public void run() {
            if (service != null && service.mPlayer != null) {
                seekBarTimePlay.setMax(service.getDuration() * 1000);
                long curDuration = service.mPlayer.getPlayedDuration() / 1000;
                tvStartDuration.setText(Utils.convertMillisecond(curDuration));
                seekBarTimePlay.setProgress((int) service.mPlayer.getPlayedDuration());
                service.setOnComplete();
                handler.postDelayed(this, 1000);
            }
        }
    };
    private ConstraintLayout viewRate, viewPitchTempo;
    private ImageView ivOpenRate, ivCloseRate;
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
                        break;

                    case Utils.UI_PLAY_SONG:
                        updateTimePlay();
                        break;

                    case Utils.STOP_MUSIC:
                        stopMusic();
                        break;
                }
            }
        }
    };
    private ItemTouchHelper.Callback callback;
    private ItemTouchHelper itemTouchHelper;
    private List<Song> songListAfterChange;

//    public String getRealPathFromURI(Uri contentUri) {
//        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getContext().getContentResolver(contentUri, proj, null, null, null);
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        return cursor.getString(column_index);
//    }

    private void stopMusic() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        ivPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        tvStartDuration.setText("00:00");
        seekBarTimePlay.setProgress(0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_tune, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public String getPathFromUri(Uri uri) {

        //final String id = DocumentsContract.getDocumentId(uri);
//        final Uri contentUri = ContentUris.withAppendedId(
//                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public String getPathImage(Uri uri) {
        Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        String[] projection = {MediaStore.Audio.Media.ALBUM_ID};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
        cursor.moveToFirst();
        return ContentUris.withAppendedId(ART_CONTENT_URI, cursor.getLong(column_index)).toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Statistic.REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Uri treeUri = data.getData();

                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

                mediaMetadataRetriever.setDataSource(getContext(), Uri.parse(getPathFromUri(treeUri)));
                String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                long duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                InputStream inputStream = null;

                if (mediaMetadataRetriever.getEmbeddedPicture() != null) {
                    inputStream = new ByteArrayInputStream(mediaMetadataRetriever.getEmbeddedPicture());
                }
                mediaMetadataRetriever.release();

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (artist == null) {
                    artist = "Unknow";
                }

                File file = new File(getPathFromUri(treeUri));

                Song song = new Song();
                song.setNameSong(title);
                song.setNameArtist(artist);
                song.setDuration(duration);
                song.setPath(getPathFromUri(treeUri));
                song.setUriImage(Utils.getArtUriFromMusicFile(new File(getPathFromUri(treeUri)), getContext()));

                songList.add(song);
                service.setSongList(songList);
                songAdapter.notifyDataSetChanged();

                if (songList.size() == 1) {
                    tvNameSong.setText(title);
                    tvNameArtist.setText(artist);
                }

                Log.e("xxx", getPathFromUri(treeUri));
            }
        }
    }

    private void updateTimePlay() {
        if (service != null && service.isPlaying()) {
            ivPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
        } else {
            ivPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }

        seekBarTimePlay.setMax(service.getDuration() * 1000000);
        tvNameSong.setText(service.getNameSong());
        tvNameArtist.setText(service.nameArtist());
        tvEndDuration.setText(Utils.convertMillisecond(service.getDuration()));

        if (handler == null) {
            handler = new Handler();
        } else {
            handler.removeCallbacksAndMessages(null);
        }
        handler.postDelayed(updateUIPlay, 100);

    }


    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void initViews() {
        setHasOptionsMenu(true);

        IntentFilter it = new IntentFilter();
        it.addAction(Utils.OPEN_LIST_FILE);
        it.addAction(Utils.UI_PLAY_SONG);
        it.addAction(Utils.STOP_MUSIC);
        getContext().registerReceiver(receiver, it);

        dbHandler = DbHandler.getInstance(getContext());
        service = ((MyApplication) getContext().getApplicationContext()).getService();

        songList = new ArrayList<>();
        songAdapter = new SongAdapter(songList, getContext(), this, this, this);
        rvSong = (RecyclerView) findViewById(R.id.rv_song);
        rvSong.setHasFixedSize(true);
        rvSong.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSong.setAdapter(songAdapter);

        callback = new SimpleItemTouchHelperCallback(songAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvSong);

        ivAddSong = (ImageView) findViewById(R.id.iv_add_song);
        ivAddSong.setOnClickListener(view -> addSongToList());

        tvPitch = (TextView) findViewById(R.id.tv_pitch);
        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvTempo = (TextView) findViewById(R.id.tv_tempo);
        tvNameArtist = (TextView) findViewById(R.id.tv_name_artist);
        tvNameSong = (TextView) findViewById(R.id.tv_name_song);

        ivRefreshPitch = (ImageView) findViewById(R.id.iv_refresh_pitch);
        ivRefreshTempo = (ImageView) findViewById(R.id.iv_refresh_tempo);
        ivRefreshRate = (ImageView) findViewById(R.id.iv_refresh_rate);

        viewRate = (ConstraintLayout) findViewById(R.id.view_rate);
        viewPitchTempo = (ConstraintLayout) findViewById(R.id.view_pitch_tempo);
        ivOpenRate = (ImageView) findViewById(R.id.iv_open_close_rate);
        ivCloseRate = (ImageView) findViewById(R.id.iv_close_rate);
        ivOpenRate.setOnClickListener(view -> openCloseRate());
        ivCloseRate.setOnClickListener(view -> openCloseRate());

        ivRefreshTempo.setOnClickListener(this);
        ivRefreshRate.setOnClickListener(this);
        ivRefreshPitch.setOnClickListener(this);

        tvStartDuration = (TextView) findViewById(R.id.tv_run_start);
        tvEndDuration = (TextView) findViewById(R.id.tv_run_end);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        ivRepeat = (ImageView) findViewById(R.id.iv_repeat);
        ivShuffle = (ImageView) findViewById(R.id.iv_shuffle);
        ivPrevious = (ImageView) findViewById(R.id.iv_previous);
        ivFastNext = (ImageView) findViewById(R.id.iv_fast_next);
        ivFastPrevious = (ImageView) findViewById(R.id.iv_fast_previous);
        ivStateSlidingPanel = (ImageView) findViewById(R.id.iv_state_sliding);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setDragView(findViewById(R.id.v));
        slidingUpPanelLayout.setShadowHeight(0);

        ivPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivRepeat.setOnClickListener(this);
        ivShuffle.setOnClickListener(this);
        ivFastNext.setOnClickListener(this);
        ivPrevious.setOnClickListener(this);
        ivFastPrevious.setOnClickListener(this);
        ivStateSlidingPanel.setOnClickListener(this);
        slidingUpPanelLayout.addPanelSlideListener(this);

        findViewById(R.id.iv_input_pitch).setOnClickListener(view -> createDialogInputValue(PITCH));
        findViewById(R.id.iv_input_rate).setOnClickListener(view -> createDialogInputValue(RATE));
        findViewById(R.id.iv_input_tempo).setOnClickListener(view -> createDialogInputValue(TEMPO));

        seekBarPitch = (SeekBar) findViewById(R.id.seekbar_pitch);
        seekBarTempo = (SeekBar) findViewById(R.id.seekbar_tempo);
        seekBarRate = (SeekBar) findViewById(R.id.seekbar_rate);
        seekBarTimePlay = (SeekBar) findViewById(R.id.seekbar_time_play);

        seekBarTimePlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (service != null && service.mPlayer != null) {
                    Log.e("xxx", "time play" + seekBar.getProgress() + "___" + service.mPlayer.getDuration());
                    service.seek(seekBar.getProgress());
                }
            }
        });

        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean iSFromUser) {
                if (iSFromUser) {
                    pitchSemi = progress - 12;
                    tvPitch.setText("Pitch: " + (pitchSemi >= 0 ? "+" + pitchSemi : pitchSemi));
                    if (true) {
                        if (service != null && service.mPlayer != null) {
                            service.mPlayer.setPitchSemi(pitchSemi);
                        }
                    } else {
                        player.setPitchSemi(pitchSemi);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                Log.e("xxx", "fdfsfs");
                tempo = (float) (progress + 25) / 100f;
                tvTempo.setText("Tempo: " + (progress + 25) + "%");

                if (isServiceRunning()) {
                    service.mPlayer.setTempo(tempo);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        //setupPlayer();
    }

    private void openCloseRate() {
        //visible = !visible;
        //text.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (viewPitchTempo.getVisibility() == View.GONE) {
            viewRate.setVisibility(View.GONE);
            viewPitchTempo.setVisibility(View.VISIBLE);
        } else {
            viewPitchTempo.setVisibility(View.GONE);
            viewRate.setVisibility(View.VISIBLE);
        }
    }

    private void addSongToList() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            startActivityForResult(intent, Statistic.REQUEST_CODE_SELECT_FILE);
        } else {
            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .newDirectoryName("AudioRecord")
                    .allowReadOnlyDirectory(true)
                    .allowNewDirectoryNameModification(true)
                    .build();
            //mDialog = DirectoryChooserFragment.newInstance(config);
            //mDialog.show(getFragmentManager(), null);
        }
    }

    private void refreshValuePitch() {
        pitchSemi = 12.00f;
        seekBarPitch.setProgress((int) 12.00f);
        tvPitch.setText("Pitch: +0.0");
//        if (player != null) {
//            player.setPitchSemi(pitchSemi);
//        }
        if (isServiceRunning()) {
            service.mPlayer.setPitchSemi(0);
        }
    }

    private void refreshValueTempo() {
        tempo = 75;
        seekBarTempo.setProgress(75);
        tvTempo.setText("Tempo: 100%");
//        if (player != null) {
//            player.setTempo(75);
//        }
        if (isServiceRunning()) {
            service.mPlayer.setTempo(1);
        }
    }

    private void refreshValueRate() {

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
                if (getFragmentManager() == null) {
                    break;
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.animation_left_to_right, R.anim.animation_right_to_left, R.anim.animation_left_to_right, R.anim.animation_right_to_left).replace(R.id.container, StudioFragment.newInstance(), RecorderFragment.class.getName()).addToBackStack(null).commit();
                }
                break;

            case R.id.item_setting:

                break;

            case R.id.item_save:
                saveFile();
                break;

            case R.id.item_setting1:
                addFragmentSetting();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFragmentSetting() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.animation_left_to_right, R.anim.animation_right_to_left, R.anim.animation_left_to_right, R.anim.animation_right_to_left).replace(R.id.container, SettingFragment.newInstance(), RecorderFragment.class.getName()).addToBackStack(null).commit();
    }

    private void saveFile() {
        try {

            progressDialog.show();
            ivPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);

            SoundStreamFileWriter writer = new SoundStreamFileWriter(0, "/storage/emulated/0/motphut.flac", "/storage/emulated/0/Download/vvvv.mp3", tempo, pitchSemi, getContext());
            writer.setFileWritingListener(new SoundStreamFileWriter.FileWritingListener() {
                @Override
                public void onFinishedWriting(boolean success) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), getString(R.string.save_file_success), Toast.LENGTH_SHORT).show();
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

        TextView tvTitle = view.findViewById(R.id.tv_title2);
        TextView tvValue = view.findViewById(R.id.tv_value);
        edtInput = view.findViewById(R.id.edt_input);
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
                applyInputTempo();
                break;

            case PITCH:
                applyInputPitch();
                break;

            case RATE:
                applyInputRate();
                break;
        }
    }

    private void applyInputRate() {
        if (isTextEmpty()) {
            Toast.makeText(getContext(), getString(R.string.please_input_value), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyInputPitch() {
        if (isTextEmpty()) {
            Toast.makeText(getContext(), getString(R.string.please_input_value), Toast.LENGTH_SHORT).show();
        } else {

            try {
                pitchSemi = Float.parseFloat(edtInput.getText().toString().trim());

                if (pitchSemi >= 24.00f) {
                    pitchSemi = 24.00f;
                } else if (pitchSemi < -12.00f) {
                    pitchSemi = -12.00f;
                }

                seekBarPitch.setProgress((int) pitchSemi + 12);
                tvPitch.setText("Pitch: +" + pitchSemi);

//                if (player != null) {
//                    player.setPitchSemi(pitchSemi);
//                }
                if (isServiceRunning()) {
                    service.mPlayer.setPitchSemi(pitchSemi);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private boolean isTextEmpty() {
        if (edtInput != null && edtInput.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    private void applyInputTempo() {
        if (isTextEmpty()) {
            Toast.makeText(getContext(), getString(R.string.please_input_value), Toast.LENGTH_SHORT).show();
        } else {
//            try {
            tempo = Float.parseFloat(edtInput.getText().toString().trim());

            if (tempo > 225) {
                tempo = 225;
            } else if (tempo <= 25) {
                tempo = 25;
            }

            seekBarTempo.setProgress((int) tempo);
            tvTempo.setText((int) tempo + " %xxxxxxx");

//                if (player != null) {
//                    player.setTempo(tempo);
//                }
            if (isServiceRunning()) {
                service.mPlayer.setTempo(tempo);
            }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }


        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case EXPANDED:
                ivStateSlidingPanel.setImageResource(R.drawable.slide_down);
                break;
            case COLLAPSED:
                ivStateSlidingPanel.setImageResource(R.drawable.slide_up);
                break;
        }
    }

    private boolean isServiceRunning() {
        if (service != null && service.mPlayer != null) {
            return true;
        }
        return false;
    }

    private void playNextSong() {
        if (isServiceRunning()) {
            service.playNext();
        }
    }

    private void playPreviousSong() {
        if (isServiceRunning()) {
            service.playPreviousSong();
        }
    }

    /* Set state shuffle */
    private void doShuffle() {
        if (!service.getShuffle()) {
            service.setShuffle(true);
            service.addSongShuffle();
            ivShuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);

        } else {
            service.setShuffle(false);
            ivShuffle.setImageResource(R.drawable.no_shuffle);
        }
    }

    /* Set state repeat */
    private void doRepeat() {
        if (service.getStateRepeat() == 0) {
            service.setStateRepeat(REPEAT_ALL);
            ivRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);

        } else if (service.getStateRepeat() == REPEAT_ONE) {

            service.setStateRepeat(NO_REPEAT);
            ivRepeat.setImageResource(R.drawable.ic_no_repeat);

        } else if (service.getStateRepeat() == REPEAT_ALL) {

            service.setStateRepeat(REPEAT_ONE);
            ivRepeat.setImageResource(R.drawable.ic_repeat_one_black_24dp);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_touch:
                break;

            case R.id.iv_play:
                playSong();
                break;

            case R.id.iv_previous:
                playPreviousSong();
                break;

            case R.id.iv_next:
                playNextSong();
                break;

            case R.id.iv_fast_next:
                skipFastNext();
                break;

            case R.id.iv_fast_previous:
                skipFastPrevious();
                break;

            case R.id.iv_repeat:
                doRepeat();
                break;

            case R.id.iv_shuffle:
                doShuffle();
                break;

            case R.id.iv_refresh_pitch:
                refreshValuePitch();
                break;

            case R.id.iv_refresh_rate:

                break;

            case R.id.iv_refresh_tempo:
                refreshValueTempo();
                break;
        }
    }

    private void skipFastPrevious() {
        if (isServiceRunning()) {
            service.fastPrevious();
        }
    }

    private void skipFastNext() {
        if (isServiceRunning()) {
            service.fastNext();
        }
    }

    private void playSong() {
        if (songList.size() > 0) {
            if (service != null && !service.isStartPlay) {
                service.setSongList(songList);
                service.setIndexPlay(positionPlay);
                service.playAudioEntity();
                updateTimePlay();
                ivPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);

            } else {
                if (service != null && service.isPlaying()) {
                    service.pausePlayer();
                    updateTimePlay();
                    ivPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                } else {
                    ivPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                    service.startPlayer();
                    updateTimePlay();
                }
            }
        }
    }


    // listen action sort or dissmis item song
    @Override
    public void onNoteListChanged(List<Song> songs) {
        if (songs != null) {
            Log.e("xxx", songs.size() + "_______" + songList.size());
//            List<Song> songSortList = new ArrayList<>();
//            songSortList.clear();
//            songSortList.addAll(songs);

//            String songPath = null;
//            if (service != null && service.getSongList() != null) {
//                songPath = service.getPathSong();
//            }

//            if (songList == null) {
//                songList = new ArrayList<>();
//            } else {
//                songList.clear();
//            }

//            songList.addAll(songs);

            if (service != null) {
                service.setSongList(songList);
            }

            // update index song play after sort
//            if (songPath != null) {
//                for (int i = 0; i < service.getSongList().size() - 1; i++) {
//                    if (songPath.equals(service.getSongList().get(i).getPath())) {
//                        service.setIndexPlay(i);
//                    }
//                }
//            }
        }
    }

    @Override
    public void onClick(int index, View view) {

    }

    @Override
    public void onLongClick(int index, View view) {

    }

    @Override
    public void onOptionClick(int index, View view) {

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
