package com.bstech.voicechanger.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.adapter.TabAdapter;
import com.bstech.voicechanger.utils.FileUtil;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Statistic;
import com.bstech.voicechanger.utils.Utils;

import static com.bstech.voicechanger.utils.Utils.EMPTY;
import static com.bstech.voicechanger.utils.Utils.LOCAL_SAVE_FILE;
import static com.bstech.voicechanger.utils.Utils.PATH;
import static com.bstech.voicechanger.utils.Utils.SAVE_PATH;

public class MainActivity extends AppCompatActivity implements DirectoryChooserFragment.OnFragmentInteractionListener {

    private static final int INDEX_RECORDER = 0;
    private static final int INDEX_AUDIO_TUNE = 1;
    public Toolbar toolbar;
    private TabAdapter tabAdapter;
    private ViewPager viewPager;

    private DirectoryChooserFragment directoryChooserFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        run();
        addToolbar();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                addTabFragment();
            }
        },500);

    }

    public void run() {
        try {
            System.loadLibrary("libsoundtouch");
        } catch (UnsatisfiedLinkError e) {
            // catch and ignore an already loaded in another classloader
            // exception, as vm already has it loaded
        }

    }


    public void chooseLocalSaveFile() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, Statistic.REQUEST_CODE_OPEN_DIRECTORY);
        } else {
            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .newDirectoryName("BVoiceChanger1")
                    .allowReadOnlyDirectory(true)
                    .allowNewDirectoryNameModification(true)
                    .build();
            directoryChooserFragment = DirectoryChooserFragment.newInstance(config);
            directoryChooserFragment.show(getFragmentManager(), null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Statistic.REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                Uri treeUri = data.getData();
//                String path = FileUtil.getFullPathFromTreeUri(treeUri);
//
//                SharedPrefs.getInstance().put(Utils.TREE_URI, treeUri.toString());
//                SharedPrefs.getInstance().put(Utils.LOCAL_SAVE_FILE, path + "/");
//
//                Log.d("lynah", "lynah: " + path + " " + treeUri.toString());
//                Log.d("lynah", "file path =" + SharedPrefs.getInstance().get(Utils.TREE_URI, String.class, null));
//
//                sendBroadcast(new Intent(Utils.SAVE_PATH).putExtra(Utils.PATH, SharedPrefs.getInstance().get(Utils.LOCAL_SAVE_FILE, String.class, null)));
//                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            }
        }

//        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
//            Log.d(TAG, "reconnect");
//            if (mGoogleApiClient != null) mGoogleApiClient.connect();
//
//        } else if (requestCode == REQUEST_CODE_OPENER && resultCode == Activity.RESULT_OK) {
//            Log.d(TAG, "REQUEST_CODE_OPENER");
//
//            DriveId driveId = (DriveId) data.getParcelableExtra(
//                    OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
//
//            if (driveId.getResourceId() != null) {
//                String oldDriveId = SharedPrefs.getInstance().get(Utils.FOLDER_RES, String.class, null);
//                if (oldDriveId != null && !oldDriveId.equals(driveId.getResourceId())) {
//                }
//
//                Flog.d(TAG, "encode: " + driveId.encodeToString());
//                Log.d(TAG, "tostring: " + driveId.toString());
//                Log.d(TAG, "getResourceId: " + driveId.getResourceId());
//
//                SharedPrefs.getInstance().put(Utils.FOLDER_ENCODE, driveId.encodeToString());
//                SharedPrefs.getInstance().put(Utils.FOLDER_RES, driveId.getResourceId());
//                startService(new Intent(this, DriveService.class).putExtra(Utils.PATH, filePath));
//
//            } else {
//                if (getGoogleApiClient().isConnected()) {
//                    startDirChooser();
//                } else {
//                    connectDrive();
//                }
//            }
//        }

    }

    private void addTabFragment() {

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(tabAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(INDEX_RECORDER).setText(R.string.recorder);
        tabLayout.getTabAt(INDEX_AUDIO_TUNE).setText(R.string.audio_tune);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {

                    case INDEX_RECORDER:
                        toolbar.getMenu().clear();
                        toolbar.inflateMenu(R.menu.menu_recorder);
                        break;

                    case INDEX_AUDIO_TUNE:
                        toolbar.getMenu().clear();
                        toolbar.inflateMenu(R.menu.menu_audio_tune);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    private void addToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_recorder);
        setSupportActionBar(toolbar);

    }


    @Override
    public void onSelectDirectory(@NonNull String path) {
        SharedPrefs.getInstance().put(LOCAL_SAVE_FILE, path + "/");
        sendBroadcast(new Intent(SAVE_PATH).putExtra(PATH, SharedPrefs.getInstance().get(LOCAL_SAVE_FILE, String.class, EMPTY)));
        directoryChooserFragment.dismiss();
    }

    @Override
    public void onCancelChooser() {
        directoryChooserFragment.dismiss();
    }
}
