package com.bstech.voicechanger.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.activity.MainActivity;
import com.bstech.voicechanger.databinding.FragmentSettingBinding;
import com.bstech.voicechanger.utils.FileUtil;
import com.bstech.voicechanger.utils.SharedPrefs;
import com.bstech.voicechanger.utils.Statistic;
import com.bstech.voicechanger.utils.Utils;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import static com.bstech.voicechanger.utils.Utils.BITRATE_128_;
import static com.bstech.voicechanger.utils.Utils.BITRATE_160_;
import static com.bstech.voicechanger.utils.Utils.BITRATE_192_;
import static com.bstech.voicechanger.utils.Utils.BITRATE_256_;
import static com.bstech.voicechanger.utils.Utils.BITRATE_320_;
import static com.bstech.voicechanger.utils.Utils.BITRATE_MP3;
import static com.bstech.voicechanger.utils.Utils.BITRATE_OGG;
import static com.bstech.voicechanger.utils.Utils.COMPINE;
import static com.bstech.voicechanger.utils.Utils.M4A;
import static com.bstech.voicechanger.utils.Utils.MP3;
import static com.bstech.voicechanger.utils.Utils.MP3_128;
import static com.bstech.voicechanger.utils.Utils.MP3_160;
import static com.bstech.voicechanger.utils.Utils.MP3_192;
import static com.bstech.voicechanger.utils.Utils.MP3_256;
import static com.bstech.voicechanger.utils.Utils.MP3_320;
import static com.bstech.voicechanger.utils.Utils.OGG;
import static com.bstech.voicechanger.utils.Utils.STATE_KEEP_SCREEN;
import static com.bstech.voicechanger.utils.Utils.STATE_OFF;
import static com.bstech.voicechanger.utils.Utils.STATE_ON;
import static com.bstech.voicechanger.utils.Utils.STATUS_BAR;
import static com.bstech.voicechanger.utils.Utils._M4A;
import static com.bstech.voicechanger.utils.Utils._MP3;
import static com.bstech.voicechanger.utils.Utils._OGG;

public class SettingFragment extends BaseFragment {
    private static final String FORMAT = "Format";
    private static final String F_MP3 = ".mp3";
    private static final String F_M4A = ".m4a";
    private static final String F_OGG = ".ogg";
    private static final int BIT_128 = 128;
    private static final int BIT_160 = 160;
    private static final int BIT_192 = 192;
    private static final int BIT_256 = 256;
    private static final int BIT_320 = 320;
    private static final String BIT_RATE_M4A = "FormatM4A";
    private FragmentSettingBinding binding;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private DirectoryChooserFragment mDialog;
    private ImageView ivSet;
    private MainActivity activity;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Utils.UPDATE_SETTING_COMPINE:
                        updateStateCompinePitchTempo();
                        break;
                }
            }
        }
    };

    public static SettingFragment newInstance() {
        Bundle args = new Bundle();
        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    private void updateStateCompinePitchTempo() {
        int state = SharedPrefs.getInstance().get(Utils.COMPINE, Integer.class, Utils.STATE_OFF);
        if (state == STATE_ON) {
            binding.ivStateCompine.setImageResource(R.drawable.ic_radio_button_checked_black_24dp);
        } else {
            binding.ivStateCompine.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
        }
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

        binding.viewLinkPitchTempo.setOnClickListener(view -> setState(true, COMPINE));
        binding.viewKeepScreenOn.setOnClickListener(view -> setState(true, STATE_KEEP_SCREEN));
        binding.viewStatusBar.setOnClickListener(view -> setState(true, STATUS_BAR));
        binding.viewLocalSaveFile.setOnClickListener(view -> setLocalSaveFile());
        binding.viewChooseFileFormat.setOnClickListener(view -> setFileFormat());
        binding.viewRecorderEncording.setOnClickListener(view -> setRecorderEncording());

        setState(false, COMPINE);
        setState(false, STATUS_BAR);
        setState(false, STATE_KEEP_SCREEN);

        registerReceiver();
        showCurrentBitrate();
        showCurrentLocalSaveFile();
    }

    private void showCurrentLocalSaveFile(){
        String localSaveFile = SharedPrefs.getInstance().get(Utils.LOCAL_SAVE_FILE, String.class, null);
        if (localSaveFile == null) {
            binding.tvShowLocalSaveFile.setText(Environment.getExternalStorageDirectory() + "/BVoiceChanger");
        } else {
            binding.tvShowLocalSaveFile.setText(localSaveFile);
        }
    }
    private void showCurrentBitrate() {
        int bitrate = 128;
        String fileFormat = SharedPrefs.getInstance().get(FORMAT, String.class, F_MP3);

        if (fileFormat.equals(F_MP3)) {
            bitrate = SharedPrefs.getInstance().get(BITRATE_MP3, Integer.class, 128);
        } else if (fileFormat.equals(F_M4A)) {
            bitrate = SharedPrefs.getInstance().get(BIT_RATE_M4A, Integer.class, 128);
        } else if (fileFormat.equals(F_OGG)) {
            bitrate = SharedPrefs.getInstance().get(BITRATE_OGG, Integer.class, 128);
        }

        binding.tvShowChooseEncording.setText(bitrate + " kpbs");
    }

    private void registerReceiver() {
        IntentFilter it = new IntentFilter();
        it.addAction(Utils.UPDATE_SETTING_COMPINE);
        activity.registerReceiver(receiver, it);
    }

    @Override
    public void onDetach() {
        activity.unregisterReceiver(receiver);
        super.onDetach();
    }

    private int checkFormatM4A() {
        int bitrate = SharedPrefs.getInstance().get(BIT_RATE_M4A, Integer.class);
        int bitRate[] = new int[]{0, 1, 2};
        return bitRate[bitrate];
    }

    private int checkFormatMp3Ogg(int bitrate) {
        int bitRate[] = new int[]{MP3_128, MP3_160, MP3_192, MP3_256, MP3_320};
        return bitRate[bitrate];
    }

    private void setRecorderEncording() {
        String format = SharedPrefs.getInstance().get(FORMAT, String.class);

        final String bitrate[];

        builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);

        if (format.equals(Utils.FORMAT_M4A)) {
            bitrate = new String[]{BITRATE_128_, BITRATE_192_, BITRATE_256_};

            final int[] values = new int[]{128, 192, 256};

            builder.setSingleChoiceItems(bitrate, checkFormatM4A(), (dialog, which) -> {
                binding.tvShowChooseEncording.setText(bitrate[which]);
                alertDialog.dismiss();
                SharedPrefs.getInstance().put(BIT_RATE_M4A, values[which]);
                Toast.makeText(getContext(), getResources().getString(R.string.set_bitrate) + bitrate[which], Toast.LENGTH_SHORT).show();

            });

        } else if (format.equals(Utils.FORMAT_OGG)) {
            bitrate = new String[]{BITRATE_128_, BITRATE_160_, BITRATE_192_, BITRATE_256_, BITRATE_320_};

            final int[] values = new int[]{128, 160, 192, 256, 320};

            int realBitrate = SharedPrefs.getInstance().get(Utils.BITRATE_OGG, Integer.class);

            builder.setSingleChoiceItems(bitrate, checkFormatMp3Ogg(realBitrate), (dialog, which) -> {
                binding.tvShowChooseEncording.setText(bitrate[which]);
                alertDialog.dismiss();
                SharedPrefs.getInstance().put(Utils.BITRATE_OGG, values[which]);
                Toast.makeText(getContext(), getResources().getString(R.string.set_bitrate) + bitrate[which], Toast.LENGTH_SHORT).show();
            });

        } else {
            bitrate = new String[]{BITRATE_128_, BITRATE_160_, BITRATE_192_, BITRATE_256_, BITRATE_320_};

            final int[] values = new int[]{128, 160, 192, 256, 320};

            int realBitrate = SharedPrefs.getInstance().get(Utils.BITRATE_MP3, Integer.class);

            builder.setSingleChoiceItems(bitrate, checkFormatMp3Ogg(realBitrate), (dialog, which) -> {
                SharedPrefs.getInstance().put(Utils.BITRATE_MP3, values[which]);
                Toast.makeText(getContext(), getResources().getString(R.string.set_bitrate) + bitrate[which], Toast.LENGTH_SHORT).show();
                binding.tvShowChooseEncording.setText(bitrate[which]);
                alertDialog.dismiss();
            });
        }

        //cancelDialog();
        alertDialog = builder.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void setFileFormat() {
        String format = SharedPrefs.getInstance().get(FORMAT, String.class);
        int itemSelected = 0;
        switch (format) {
            case F_MP3:
                itemSelected = _MP3;
                break;
            case F_M4A:
                itemSelected = _M4A;
                break;
            case F_OGG:
                itemSelected = _OGG;
                break;
            default:
                itemSelected = _MP3;
                break;
        }

        final String formatD[] = new String[]{F_MP3, F_M4A, F_OGG};
        final String formatS[] = new String[]{MP3, M4A, OGG};

        builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
        builder.setSingleChoiceItems(formatS, itemSelected, (dialog, which) -> {

            SharedPrefs.getInstance().put(FORMAT, formatD[which]);

            Toast.makeText(getContext(), getResources()
                    .getString(R.string.set_type_file) + formatS[which], Toast.LENGTH_SHORT).show();

            binding.tvShowChooseFileFormat.setText(formatS[which]);

            switch (formatS[which]) {
                case MP3:
                    binding.tvShowChooseEncording.setText(SharedPrefs.getInstance()
                            .get(BITRATE_MP3, Integer.class, 128) + " kpbs");
                    break;

                case M4A:
                    binding.tvShowChooseEncording.setText(SharedPrefs.getInstance()
                            .get(BIT_RATE_M4A, Integer.class, 128) + " kpbs");
                    break;

                case OGG:
                    binding.tvShowChooseEncording.setText(SharedPrefs.getInstance()
                            .get(BITRATE_OGG, Integer.class, 128) + " kpbs");
                    break;
            }
            alertDialog.dismiss();
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void setLocalSaveFile() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, Statistic.REQUEST_CODE_OPEN_DIRECTORY);
        } else {
            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .newDirectoryName(Statistic.FOLDER_STORE)
                    .allowReadOnlyDirectory(true)
                    .allowNewDirectoryNameModification(true)
                    .build();
            mDialog = DirectoryChooserFragment.newInstance(config);
            mDialog.show(getActivity().getFragmentManager(), null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Statistic.REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Uri treeUri = data.getData();
                String path = FileUtil.getFullPathFromTreeUri(treeUri);

                SharedPrefs.getInstance().put(Utils.TREE_URI, treeUri.toString());
                SharedPrefs.getInstance().put(Utils.LOCAL_SAVE_FILE, path + "/");

                binding.tvShowLocalSaveFile.setText(path + "/");

                activity.sendBroadcast(new Intent(Utils.SAVE_PATH).putExtra(Utils.PATH, SharedPrefs.getInstance().get(Utils.LOCAL_SAVE_FILE, String.class, null)));
                activity.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            }
        }
    }

    /**
     * set and show states compine, keep screen on or status bar
     *
     * @param isSet   if true set state command, if false show current state command
     * @param command is check state of COMPINE, STATE KEEP SCREEN ON or STATUS BAR
     **/
    private void setState(boolean isSet, String command) {
        int state = STATE_OFF;

        switch (command) {
            case COMPINE:
                state = SharedPrefs.getInstance().get(COMPINE, Integer.class, STATE_OFF);
                ivSet = binding.ivStateCompine;
                break;

            case STATE_KEEP_SCREEN:
                state = SharedPrefs.getInstance().get(STATE_KEEP_SCREEN, Integer.class, STATE_OFF);
                ivSet = binding.ivStateKeepScreenOn;
                break;

            case STATUS_BAR:
                state = SharedPrefs.getInstance().get(STATUS_BAR, Integer.class, STATE_OFF);
                ivSet = binding.ivStateStatusBar;
                break;
        }

        if (isSet) {
            if (state == STATE_OFF) {
                SharedPrefs.getInstance().put(command, STATE_ON);

                ivSet.setImageResource(R.drawable.ic_radio_button_checked_black_24dp);
                ivSet.setImageResource(R.drawable.ic_radio_button_checked_black_24dp);

                if (command.equals(STATUS_BAR)) {
                    binding.tvChooseStatusBar.setText(getString(R.string.enable));
                }
            } else {
                SharedPrefs.getInstance().put(command, STATE_OFF);

                ivSet.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
                ivSet.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);

                if (command.equals(STATUS_BAR)) {
                    binding.tvChooseStatusBar.setText(getString(R.string.disable));
                }
            }

            switch (command) {
                case COMPINE:
                    if (command.equals(COMPINE)) {
                        activity.sendBroadcast(new Intent(Utils.UPDATE_COMPINE_PITCH_TEMPO));
                    }
                    break;

                case STATUS_BAR:

                    break;
            }


        } else {
            if (state == STATE_ON) {
                ivSet.setImageResource(R.drawable.ic_radio_button_checked_black_24dp);
                if (command.equals(STATUS_BAR)) {
                    binding.tvChooseStatusBar.setText(getString(R.string.enable));
                }
            } else {
                ivSet.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
                if (command.equals(STATUS_BAR)) {
                    binding.tvChooseStatusBar.setText(getString(R.string.disable));
                }
            }
        }
    }
}
