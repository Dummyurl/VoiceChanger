package com.bstech.voicechanger.utils;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;

import com.bstech.voicechanger.adapter.RecordAdapter;

/**
 * Created by Giga on 7/9/2018.
 */

public class BindingUtils {
    @BindingAdapter({ "setAdapter"})
    public static void setRecordAdapter(RecyclerView view, RecordAdapter adapter) {
        view.setAdapter(adapter);
    }
}
