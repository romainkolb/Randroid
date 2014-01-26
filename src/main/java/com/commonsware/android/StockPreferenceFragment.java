package com.commonsware.android;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by romain on 1/26/14.
 */
@TargetApi(11)
public class StockPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int res = getActivity().getResources().getIdentifier(getArguments().getString("resource"), "xml", getActivity().getPackageName());
        addPreferencesFromResource(res);
    }
}
