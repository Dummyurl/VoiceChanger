package com.bstech.voicechanger.fragment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bstech.voicechanger.BuildConfig;
import com.bstech.voicechanger.R;
import com.bstech.voicechanger.activity.MainActivity;
import com.bstech.voicechanger.adapter.RecordAdapter;
import com.bstech.voicechanger.databinding.FragmentStudioBinding;
import com.bstech.voicechanger.model.Record;
import com.bstech.voicechanger.utils.DbHandler;
import com.bstech.voicechanger.utils.Flog;
import com.bstech.voicechanger.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudioFragment extends BaseFragment implements RecordAdapter.OnClick {
    private static final String TAG = StudioFragment.class.getName();
    public boolean isActionMode = false;
    public int countItemSelected = 0;
    public boolean isSelectAll = false;
    private RecyclerView rvRecord;
    private DbHandler dbHandler;
    private List<Record> recordList;
    private RecordAdapter adapter;
    private FragmentStudioBinding binding;
    private MainActivity context;
    private BottomSheetDialog bottomSheetDialog;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private int indexOption;
    private EditText edtRename;
    private SearchView searchView;
    private ActionMode actionMode = null;
    private List<Record> mListChecked = new ArrayList<>();

    public static StudioFragment newInstance() {
        Bundle args = new Bundle();
        StudioFragment fragment = new StudioFragment();
        fragment.setArguments(args);
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_studio, container, false);
        binding.setStudiofragment(this);
        return binding.getRoot();
    }

    @Override
    public void initViews() {
        initToolbar();
        initView();
        searchAudio();
    }

    private void initView() {

        dbHandler = DbHandler.getInstance(getContext());
        recordList = dbHandler.getRecords();

        Collections.reverse(recordList);

        adapter = new RecordAdapter(recordList, this, this);

        binding.rvRecord.setHasFixedSize(true);
        binding.rvRecord.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecord.setAdapter(adapter);

    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        binding.toolbar.setNavigationOnClickListener(view -> onBackFragment());
        binding.toolbar.inflateMenu(R.menu.menu_search);
    }

    private void actionSearch(String s) {
        recordList = Utils.filterAudioEntity(dbHandler.getRecords(), s);
        adapter.setFilter(recordList);
    }

    private void searchAudio() {

        MenuItem menuItem = binding.toolbar.getMenu().findItem(R.id.item_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                actionSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                actionSearch(s);
                return true;
            }
        });
    }

    private void onBackFragment() {
        if (getFragmentManager() == null) {
            return;
        } else {
            Utils.closeKeyboard(getActivity());
            getFragmentManager().popBackStack();
        }
    }

    private void settingDeleteRecord() {
        if (mListChecked.size() != Utils.N_ZERO) {
            for (Record record : mListChecked) {

                dbHandler.deleteRecord(record.getId());
                if (record.getUriFile() != null) {
                    if (!record.getUriFile().equals("")) {
                        DocumentFile fileuri = DocumentFile.fromTreeUri(getContext(), Uri.parse(record.getUriFile()));
                        DocumentFile file = fileuri.findFile(new File(record.getFilePath()).getName());
                        if (file != null) {
                            file.delete();
                        } else {
                            File f = new File(record.getFilePath());
                            f.delete();
                        }

                    } else {
                        File file = new File(record.getFilePath());
                        file.delete();
                    }
                } else {
                    File file = new File(record.getFilePath());
                    file.delete();
                }
                countItemSelected = countItemSelected - Utils.N_ONE;
            }
            updateData();
            updateCountItemSelected();
            getContext().sendBroadcast(new Intent(Utils.TOTAL_UPATE));
            adapter.notifyDataSetChanged();
        }
    }

    public void updateCountItemSelected() {
        if (countItemSelected == 0)
            actionMode.setTitle("0");
        else
            actionMode.setTitle(countItemSelected + Utils.EMPTY);
    }

    private void actionModeDelete(final ActionMode mode) {
        if (mListChecked.size() != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.delete_this_record));
            builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    settingDeleteRecord();
                    isActionMode = false;
                    isSelectAll = false;
                    adapter.notifyDataSetChanged();
                    mode.finish();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void selectItem() {
        if (!isSelectAll) {
            countItemSelected = recordList.size();
            isSelectAll = true;
            for (Record record : recordList) {
                record.setCheck(true);
            }
            adapter.notifyDataSetChanged();
            actionMode.setTitle(countItemSelected + Utils.EMPTY);
            mListChecked.clear();
            mListChecked.addAll(recordList);
        } else {
            isSelectAll = false;
            for (Record record : recordList) {
                record.setCheck(false);
            }
            mListChecked.clear();
            countItemSelected = 0;
            actionMode.setTitle(countItemSelected + " ");
            actionMode.finish();
        }
    }

    public void createAction() {
        context.startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                isActionMode = true;
                for (Record record : recordList) {
                    record.setCheck(false);
                }
                adapter.notifyDataSetChanged();
                actionMode = mode;
                actionMode.setTitle(Utils.ZERO);
                context.getMenuInflater().inflate(R.menu.setting_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_delete:
                        actionModeDelete(mode);
                        break;
                    case R.id.item_check_all:
                        selectItem();
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isActionMode = false;
                isSelectAll = false;
                countItemSelected = 0;
                if (mListChecked != null) {
                    mListChecked.clear();
                }
                for (Record record : recordList) {
                    record.setCheck(false);
                    Log.e("xxx", record.getCheck() + "___");
                }
                adapter.notifyDataSetChanged();
                mode.finish();
            }
        });
    }

    @Override
    public void onClick(int index, boolean check) {
//        context.sendBroadcast(new Intent(Utils.OPEN_LIST_FILE).putExtra(Utils.INDEX, index));
//        if (getFragmentManager() != null) {
//            getFragmentManager().popBackStack();
//        }
        indexOption = index;
        openFileRecord();
    }

    public void prepareSelection(View view, int i) {
        if (((CheckBox) view).isChecked()) {
            if (!mListChecked.contains(recordList.get(i))) {
                mListChecked.add(recordList.get(i));
                countItemSelected = countItemSelected + Utils.N_ONE;
                updateCountItemSelected();
            }
        } else {
            if (mListChecked.contains(recordList.get(i))) {
                mListChecked.remove(recordList.get(i));
                countItemSelected = countItemSelected - Utils.N_ONE;
                updateCountItemSelected();
            }
        }
    }

    @Override
    public void onOptionClick(int index) {
        indexOption = index;
        showBottomSheet(index);
    }

    @Override
    public boolean onLongClick(int index, boolean check) {
        if (!check) {
            createAction();
        }
        return true;
    }

    private void showBottomSheet(int index) {
        View view = getLayoutInflater().inflate(R.layout.dialog_option_bottom, null);
        TextView tvTitle = view.findViewById(R.id.btn_title);
        tvTitle.setText(recordList.get(index).getTitle());
        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareRecord());
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> deleteRecord());
        view.findViewById(R.id.btn_detail).setOnClickListener(v -> detailRecord());
        view.findViewById(R.id.btn_rename).setOnClickListener(v -> renameRecord());
        view.findViewById(R.id.btn_open_file).setOnClickListener(v -> openFileRecord());
        view.findViewById(R.id.btn_set_as).setOnClickListener(v -> setAsRecord(recordList.get(index)));
        view.findViewById(R.id.btn_cut_audio).setOnClickListener(v -> cutFileRecord());
        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void updateData() {
        recordList.clear();
        recordList.addAll(dbHandler.getRecords());
        Collections.reverse(recordList);
        adapter.notifyDataSetChanged();
    }

    private void allowRename(Record record) {
        String fileName = edtRename.getText().toString().trim();
        String tailFile = null;

        if (fileName.equals("")) {
            Toast.makeText(getContext(), getResources().getString(R.string.input_name_record_again), Toast.LENGTH_SHORT).show();
        } else {
            File file = new File(record.getFilePath());
            if (file.getName().contains(Utils.FORMAT_MP3)) {
                tailFile = Utils.FORMAT_MP3;
            } else if (file.getName().contains(Utils.FORMAT_M4A)) {
                tailFile = Utils.FORMAT_M4A;
            } else if (file.getName().contains(Utils.FORMAT_OGG)) {
                tailFile = Utils.FORMAT_OGG;
            }

            File currentFile, newFile;
            boolean checkSame = false;

            List<Record> recordList = new ArrayList<>();
            recordList.clear();
            recordList.addAll(dbHandler.getRecords());

            for (Record save : recordList) {
                //   String nameFile = save.getTitle();
                File f = new File(save.getFilePath());
                String nameFile = f.getName();
                // Log.d("lynah", "file name :" + nameFile);
                if (nameFile.equals(edtRename.getText().toString().trim() + tailFile)) {

                    checkSame = true;
                }
            }

            if (!record.getUriFile().equals("")) {

                DocumentFile fileuri = DocumentFile.fromTreeUri(getContext(), Uri.parse(record.getUriFile()));
                DocumentFile f = fileuri.findFile(new File(record.getFilePath()).getName());

                newFile = new File(record.getFilePath().replace(record.getTitle(), "") + edtRename.getText().toString().trim() + tailFile);
                if (f != null) {
                    if (!checkSame) {
                        f.renameTo(edtRename.getText().toString().trim() + tailFile);
                        rename(edtRename.getText().toString().trim(), tailFile, record);
                        dbHandler.updateTitle(edtRename.getText().toString().trim() + tailFile, record.getId());
                        dbHandler.updateFilePath(newFile.getPath(), record.getId());
                        updateData();
                        Toast.makeText(getContext(), getString(R.string.rename_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.same_name_file), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    currentFile = new File(record.getFilePath());
                    newFile = new File(record.getFilePath().replace(record.getTitle(), "") + edtRename.getText().toString().trim() + tailFile);
                    if (!checkSame) {
                        updateRename(currentFile, newFile, tailFile, record);
                        updateData();
                        Toast.makeText(getContext(), getString(R.string.rename_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.same_name_file), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                currentFile = new File(record.getFilePath());
                newFile = new File(record.getFilePath().replace(record.getTitle(), "") + edtRename.getText().toString().trim() + tailFile);

                if (!checkSame) {
                    updateRename(currentFile, newFile, tailFile, record);
                    updateData();
                    Toast.makeText(getContext(), getString(R.string.rename_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.same_name_file), Toast.LENGTH_SHORT).show();
                }
            }
        }
        alertDialog.dismiss();

    }


    private void rename(String newName, String tailFile, Record record) {
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + " = ?", new String[]{record.getFilePath()}, null);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.TITLE, newName);
        values.put(MediaStore.Audio.Media.DATA, record.getFilePath().replace(new File(record.getFilePath()).getName(), "") + newName + tailFile);
        try {
            int result = contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media.DATA + " = ?", new String[]{record.getFilePath()});
            Flog.d("LOADDDDDDDDDDDĐ rename file " + result + " " + newName);
        } catch (Exception e) {
            Flog.d("LOADDDDDDDDDDDĐ erro " + e.getMessage());
            Toast.makeText(getContext(), getString(R.string.fail_rename), Toast.LENGTH_SHORT).show();
        }
        if (null != cursor) {
            cursor.close();
        }
    }

    private void updateRename(File currentFile, File newFile, String tailFile, Record record) {
        rename1(currentFile, newFile);
        rename(edtRename.getText().toString().trim(), tailFile, record);
        dbHandler.updateTitle(edtRename.getText().toString().trim() + tailFile, recordList.get(indexOption).getId());
        dbHandler.updateFilePath(newFile.getPath(), recordList.get(indexOption).getId());
        getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + newFile.getPath())));
    }

    private boolean rename1(File from, File to) {
        return from.getParentFile().exists() && from.exists() && from.renameTo(to);
    }


    private void cutFileRecord() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra("file_name", recordList.get(indexOption).getFilePath());
        intent.setClassName(
                "bs.com.voicechanger",
                "com.bsoft.ringdroid.RingdroidEditActivity");
        startActivityForResult(intent, 2);

        bottomSheetDialog.dismiss();
    }

    private void setAsRecord(Record record) {
        bottomSheetDialog.dismiss();

        File file = new File(record.getFilePath());

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DURATION, record.getDuration());
        values.put(MediaStore.Audio.Media.TITLE, record.getTitle());
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.SIZE, file.length());
        values.put(MediaStore.Audio.Media.ARTIST, "<unknow>");
        values.put(MediaStore.Audio.Media.DATA, record.getFilePath());
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(file
                .getAbsolutePath());
        getContext().getContentResolver().delete(
                uri,
                MediaStore.MediaColumns.DATA + "=\""
                        + file.getAbsolutePath() + "\"", null);
        Uri newUri = getContext().getContentResolver().insert(uri, values);

        try {
            RingtoneManager.setActualDefaultRingtoneUri(
                    getContext(), RingtoneManager.TYPE_RINGTONE,
                    newUri);
        } catch (Throwable t) {

        }

        Toast.makeText(context, getString(R.string.set_ring_tone_success), Toast.LENGTH_SHORT).show();
    }

    private void openFileRecord() {

        Uri uri;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(recordList.get(indexOption).getFilePath()));
        } else {
            uri = Uri.fromFile(new File(recordList.get(indexOption).getFilePath()));
        }
        intent.setDataAndType((uri), "audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);

        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
        }
    }

    private void renameRecord() {
        bottomSheetDialog.dismiss();

        View view = getLayoutInflater().inflate(R.layout.dialog_rename, null);
        createDialog(view);
        edtRename = view.findViewById(R.id.edtRename);
        view.findViewById(R.id.btnYes).setOnClickListener(v -> allowRename(recordList.get(indexOption)));
        view.findViewById(R.id.btnNo).setOnClickListener(v -> cancelRename());
    }

    private void cancelRename() {
        alertDialog.dismiss();
    }

    private void createDialog(View view) {
        builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void detailRecord() {
        bottomSheetDialog.dismiss();

        View view = getLayoutInflater().inflate(R.layout.dialog_detail, null);
        createDialog(view);

        TextView tvTitle, tvFilePath, tvDuration, tvSize, tvDateTime;
        tvTitle = view.findViewById(R.id.tvTitle);
        tvFilePath = view.findViewById(R.id.tvFilePath);
        tvDuration = view.findViewById(R.id.tvDuaration);
        tvSize = view.findViewById(R.id.tvSize);
        tvDateTime = view.findViewById(R.id.tvDateTime);
        view.findViewById(R.id.btn_yes_detail).setOnClickListener(v -> dismissDetail());
        tvTitle.setText(getResources().getString(R.string.title_audio) + ": " + recordList.get(indexOption).getTitle());
        tvFilePath.setText(getResources().getString(R.string.path) + ": " + recordList.get(indexOption).getFilePath());
        tvDuration.setText(getResources().getString(R.string.duration) + ": " + Utils.convertMillisecond(recordList.get(indexOption).getDuration()));
        tvSize.setText(getResources().getString(R.string.size) + ": " + recordList.get(indexOption).getSize());
        tvDateTime.setText(getResources().getString(R.string.date_time) + ": "
                + new SimpleDateFormat("HH:mm, dd/MM/yyyy")
                .format(recordList.get(indexOption).getDateTime()));
    }

    private void dismissDetail() {
        alertDialog.dismiss();
    }

    private void deleteRecord() {
        bottomSheetDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.sure_delete_audio));
        builder.setPositiveButton(android.R.string.yes, (dialog, id) -> {
            Record record = recordList.get(indexOption);
            if (!record.getUriFile().equals("")) {
                DocumentFile fileuri = DocumentFile.fromTreeUri(getContext(), Uri.parse(record.getUriFile()));
                DocumentFile file = fileuri.findFile(new File(record.getFilePath()).getName());
                if (file != null) {
                    dbHandler.deleteRecord(record.getId());
                    Utils.deleteAudio(getContext(), record.getFilePath());
                    file.delete();
                    Toast.makeText(getContext(), getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                } else {
                    dbHandler.deleteRecord(record.getId());
                    Utils.deleteAudio(getContext(), record.getFilePath());
                    File f = new File(record.getFilePath());
                    f.delete();
                }
            } else {
                dbHandler.deleteRecord(record.getId());
                Utils.deleteAudio(getContext(), record.getFilePath());
                File file = new File(record.getFilePath());
                file.delete();
                Toast.makeText(getContext(), getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
            }
            updateData();
            //getContext().sendBroadcast(new Intent(Contans.UPDATE_DATA));
            if (searchView != null) {
                searchView.setQuery("", true);
                searchView.clearFocus();
            }
            bottomSheetDialog.dismiss();
            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openFile() {
        Uri uri;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(recordList.get(indexOption).getFilePath()));
        } else {
            uri = Uri.fromFile(new File(recordList.get(indexOption).getFilePath()));
        }
        intent.setDataAndType((uri), "audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void shareRecord() {
        String sharePath = "file://" + recordList.get(indexOption);
        Uri uri = Uri.parse(sharePath);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("audio/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Share audio to.."));
    }
}
