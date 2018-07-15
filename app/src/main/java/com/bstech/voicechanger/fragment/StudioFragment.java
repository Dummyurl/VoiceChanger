package com.bstech.voicechanger.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.adapter.RecordAdapter;
import com.bstech.voicechanger.databinding.FragmentStudioBinding;
import com.bstech.voicechanger.model.Record;
import com.bstech.voicechanger.utils.DbHandler;
import com.bstech.voicechanger.utils.Utils;

import java.util.Collections;
import java.util.List;

public class StudioFragment extends BaseFragment implements RecordAdapter.OnClick {
    private static final String TAG = StudioFragment.class.getName();
    private RecyclerView rvRecord;
    private DbHandler dbHandler;
    private List<Record> recordList;
    private RecordAdapter adapter;
    private FragmentStudioBinding binding;
    private Context context;

    public static StudioFragment newInstance() {
        Bundle args = new Bundle();
        StudioFragment fragment = new StudioFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
    }

    private void initView() {

        dbHandler = DbHandler.getInstance(getContext());
        recordList = dbHandler.getRecords();

        Collections.reverse(recordList);

        adapter = new RecordAdapter(recordList, getContext(), this);

        binding.rvRecord.setHasFixedSize(true);
        binding.rvRecord.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecord.setAdapter(adapter);

    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        binding.toolbar.setNavigationOnClickListener(view -> onBackFragment());
    }

    private void onBackFragment() {
        if (getFragmentManager() == null) {
            return;
        } else {
            getFragmentManager().popBackStack();
        }
    }


    @Override
    public void onClick(int index) {
        context.sendBroadcast(new Intent(Utils.OPEN_LIST_FILE).putExtra(Utils.INDEX, index));
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onOptionClick(int index) {

    }
}
