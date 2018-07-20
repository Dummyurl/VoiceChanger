package com.bstech.voicechanger.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.bsoft.librate.AppRate;
import com.bstech.voicechanger.R;
import com.bstech.voicechanger.adapter.TabAdapter;
import com.bstech.voicechanger.custom.CustomViewPager;
import com.bstech.voicechanger.fragment.StudioFragment;
import com.bstech.voicechanger.service.RecordService;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wave.lib_crs.CrsDialogFragment;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import static com.bstech.voicechanger.utils.Utils.EMPTY;
import static com.bstech.voicechanger.utils.Utils.LOCAL_SAVE_FILE;
import static com.bstech.voicechanger.utils.Utils.PATH;
import static com.bstech.voicechanger.utils.Utils.SAVE_PATH;
import static com.bstech.voicechanger.utils.Utils.STATE_KEEP_SCREEN;
import static com.bstech.voicechanger.utils.Utils.STATE_OFF;
import static com.bstech.voicechanger.utils.Utils.STATE_ON;

public class MainActivity extends AppCompatActivity implements DirectoryChooserFragment.OnFragmentInteractionListener {

    private static final int INDEX_RECORDER = 0;
    private static final int INDEX_AUDIO_TUNE = 1;
    public Toolbar toolbar;
    private TabAdapter tabAdapter;
    private CustomViewPager viewPager;

    private DirectoryChooserFragment directoryChooserFragment;
    private AlertDialog.Builder builder;
    private AlertDialog aleartDialog;

    private BroadcastReceiver receiver;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addToolbar();
        //adView();

        Handler handler = new Handler();
        handler.postDelayed(() -> addTabFragment(), 1000);


    }
    private void adView() {
        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                mAdView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPrefs.getInstance().get(STATE_KEEP_SCREEN, Integer.class, STATE_OFF) == STATE_ON) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                moreApp();
                break;

            case R.id.item_rate_app:
                appRate();
                break;

            case R.id.item_about:
                about();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void rateApp() {

        Utils.buildAppRate(this, which -> MainActivity.this.finish());

        if (AppRate.showRateDialogIfMeetsConditions(this)) {
            //do nothing
        } else {
            CrsDialogFragment crsDialogFragment = new CrsDialogFragment().setOnYesListener(new CrsDialogFragment.OnYesListener() {
                @Override
                public void onYesClickListener() {
                    MainActivity.this.finish();
                }
            });
            crsDialogFragment.setCancelable(false);
            crsDialogFragment.show(getSupportFragmentManager(), "CrsDialogFragment");
        }
    }


    private void moreApp() {
        CrsDialogFragment crsDialogFragment = new CrsDialogFragment();
        crsDialogFragment.show(getSupportFragmentManager(), "CrsDialogFragment");
    }

    private void appRate() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }


    private void about() {
        try {
            String message = getString(R.string.version) + ": " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.name_app));
            builder.setMessage(message);
            builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> dialog.dismiss());
            aleartDialog = builder.create();
            aleartDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_recorder);
        setSupportActionBar(toolbar);

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() >= 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            rateApp();
        }
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
