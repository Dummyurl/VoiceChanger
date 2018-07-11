package com.bstech.voicechanger.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.activity.MainActivity;
import com.bstech.voicechanger.custom.visualizer.VisualizerManager;
import com.bstech.voicechanger.databinding.FragmentRecorderBinding;
import com.bstech.voicechanger.service.RecordService;
import com.bstech.voicechanger.utils.Utils;

import static com.bstech.voicechanger.utils.Utils.START_SERVICE;
import static com.bstech.voicechanger.utils.Utils.STOP_SERVICE;


public class RecorderFragment extends BaseFragment {
    private static final String DEFAULT_TIME = "00:00";
    public static Handler handler;

    private FragmentRecorderBinding binding;
    private MainActivity context;

    private boolean isRecording = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            } else {
                switch (intent.getAction()) {
                    case Utils.STOP_RECORD:
                        isRecording = false;
                        binding.ivRecord.setImageResource(R.drawable.ic_pause);
                        binding.ivSave.setVisibility(View.INVISIBLE);
                        binding.tvTime.setText(DEFAULT_TIME);
                        return;

                    default:
                        return;
                }
            }

        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (MainActivity) context;
    }

    @Override
    public void initViews() {
        setHasOptionsMenu(true);
        registerBroadcastReceiver();

        VisualizerManager.getInstance().setupView(binding.visualizerFullview);
        if (binding.visualizerFullview != null) {
            binding.visualizerFullview.refreshChanged();
        }

        binding.ivSave.setOnClickListener(view -> saveRecord());
        binding.ivRecord.setOnClickListener(view -> startRecord());

    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.STOP_RECORD);
        context.registerReceiver(receiver, intentFilter);
    }

    private void startRecord() {
        if (!isRecording) {
            isRecording = true;
            binding.ivSave.setVisibility(View.VISIBLE);
            binding.ivRecord.setImageResource(R.drawable.ic_play);

            File folder = new File(Environment.getExternalStorageDirectory() + "/BVoiceChanger");
            if (!folder.exists()) {
                folder.mkdir();
            }

            context.startService(new Intent(getActivity(), RecordService.class).setAction(START_SERVICE));

            updateTime();
        }
    }

    private void updateTime() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    VisualizerManager.getInstance().update((double[]) msg.obj);
                } else {
                    binding.tvTime.setText(Utils.convertMillisecond(msg.getData().getLong(Utils.TIME)));
                    binding.ivRecord.setImageResource(R.drawable.ic_play);
                    binding.ivSave.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private void saveRecord() {
        binding.ivSave.setVisibility(View.GONE);
        binding.ivRecord.setImageResource(R.drawable.ic_pause);
        context.startService(new Intent(getActivity(), RecordService.class).setAction(STOP_SERVICE));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recorder, container, false);
        binding.setRecoderfragment(this);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_recorder, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
