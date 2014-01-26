package org.loutr.randroid;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.commonsware.android.StockPreferenceFragment;

import java.util.List;

/**
 * Created by romain on 1/26/14.
 */

public class Preferences extends SherlockPreferenceActivity {
    private boolean needResource=false;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (needResource || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.general_prefs);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target) {
        //Are we on a large screen device ?
        if (onIsHidingHeaders() || !onIsMultiPane()) {
            //No, let's skip the headers and display the prefs directly
            needResource=true;
        }
        else {
            //Yes, let's display the headers in the left pane
            loadHeadersFromResource(R.xml.preference_headers, target);
        }
    }


    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (StockPreferenceFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

}

