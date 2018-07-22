package com.bstech.voicechanger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.adapter.TabMusicAdapter;

import static com.bstech.voicechanger.adapter.TabMusicAdapter.INDEX_DEVICE;
import static com.bstech.voicechanger.adapter.TabMusicAdapter.INDEX_MY_STUDIO;

public class SelectSongFragment extends BaseFragment {

    public static Toolbar toolbar;
    private TabMusicAdapter tabAdapter;
    private ViewPager viewPager;

    public static SelectSongFragment newInstance() {
        Bundle args = new Bundle();
        SelectSongFragment fragment = new SelectSongFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_song, container, false);
    }


    @Override
    public void initViews() {
        addToolbar();
        addTabFragment();
    }

    private void addToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.select_song));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(view -> getFragmentManager().popBackStack());
        toolbar.inflateMenu(R.menu.menu_select_multi_song);
    }

    private void addTabFragment() {

        tabAdapter = new TabMusicAdapter(getChildFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(tabAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(INDEX_MY_STUDIO).setText(R.string.my_studio);
        tabLayout.getTabAt(INDEX_DEVICE).setText(R.string.device);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {

                    case INDEX_MY_STUDIO:

                        break;

                    case INDEX_DEVICE:

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
}
