package com.bstech.voicechanger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.adapter.TabAdapter;
import com.bstech.voicechanger.custom.CustomViewPager;
import com.bstech.voicechanger.fragment.StudioFragment;
import com.bstech.voicechanger.service.RecordService;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Statistic;
import com.bstech.voicechanger.utils.Utils;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import static com.bstech.voicechanger.utils.Utils.EMPTY;
import static com.bstech.voicechanger.utils.Utils.LOCAL_SAVE_FILE;
import static com.bstech.voicechanger.utils.Utils.PATH;
import static com.bstech.voicechanger.utils.Utils.SAVE_PATH;

public class MainActivity extends AppCompatActivity implements DirectoryChooserFragment.OnFragmentInteractionListener {

    private static final int INDEX_RECORDER = 0;
    private static final int INDEX_AUDIO_TUNE = 1;
    public Toolbar toolbar;
    private TabAdapter tabAdapter;
    private CustomViewPager viewPager;

    private DirectoryChooserFragment directoryChooserFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addToolbar();


        Handler handler = new Handler();
        handler.postDelayed(() -> addTabFragment(), 500);

    }

    private void addTabFragment() {

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setPagingEnabled(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_studio:
                if (!Utils.isMyServiceRunning(RecordService.class, this)) {
                    if (getFragmentManager() == null) {
                        break;
                    } else {
                        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.animation_left_to_right, R.anim.animation_right_to_left, R.anim.animation_left_to_right, R.anim.animation_right_to_left).replace(R.id.container, StudioFragment.newInstance(), MainActivity.class.getName()).addToBackStack(null).commit();
                    }
                }
                break;

            case R.id.item_more_app:
                break;

            case R.id.item_rate_app:
                break;

            case R.id.item_about:
                break;
        }
        return super.onOptionsItemSelected(item);
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
