package com.bstech.voicechanger.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.databinding.FragmentSettingBinding;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Utils;

public class SettingFragment extends BaseFragment {
    private FragmentSettingBinding binding;

    public static SettingFragment newInstance() {
        Bundle args = new Bundle();
        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false);
        binding.setSetting(this);
        return binding.getRoot();
    }

    @Override
    public void initViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        binding.toolbar.setNavigationOnClickListener(view -> getFragmentManager().popBackStack());

        binding.viewLinkPitchTempo.setOnClickListener(view -> setStateCompine());
        binding.viewKeepScreenOn.setOnClickListener(view -> setStateKeepScreenOn());
        binding.viewStatusBar.setOnClickListener(view -> setStateStatusBar());
        binding.viewLocalSaveFile.setOnClickListener(view -> setLocalSaveFile());
        binding.viewChooseFileFormat.setOnClickListener(view -> setFileFormat());
        binding.viewRecorderEncording.setOnClickListener(view->setRecorderEncording());


        String localSaveFile = SharedPrefs.getInstance().get(Utils.LOCAL_SAVE_FILE, String.class, null);
        if (localSaveFile == null) {
            binding.tvDisplayLocalSaveFile.setText(Environment.getExternalStorageDirectory() + "/BVoiceChanger");
        } else {
            binding.tvDisplayLocalSaveFile.setText(localSaveFile);
        }

    }

    private void setRecorderEncording() {

    }

    private void setLocalSaveFile() {
    }

    private void setFileFormat() {
    }

    private void chooseLocalSaveFile() {
    }

    private void setStateStatusBar() {

    }

    private void setStateKeepScreenOn() {
    }

    private void setStateCompine() {

    }

}
