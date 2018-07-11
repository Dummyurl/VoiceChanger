package com.bstech.voicechanger.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.View;

public abstract class
BaseFragment extends Fragment {
    private Context mContext = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public Context getContext() {
        if (mContext != null) return mContext;


        return super.getContext();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }


    public View findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }

    public abstract void initViews();


}
