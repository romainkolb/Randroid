package org.loutr.randroid;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.commonsware.android.mapsv2.sherlock.AbstractMapActivity;
import org.loutr.randroid.model.Rando;
import org.loutr.randroid.model.RandoManagerFragment;

import java.util.Calendar;

public class MainActivity extends AbstractMapActivity implements RandoManagerFragment.Contract, ActionBar.OnNavigationListener, LoaderManager.LoaderCallbacks<Cursor> {

    private CursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (readyToGo()) {
            setContentView(R.layout.activity_main);
        }

        if (getRandoManagerFragment() == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, new RandoManagerFragment()).commit();
        }

        Context context = getSupportActionBar().getThemedContext();
        adapter =  new SimpleCursorAdapter(
                context,
                android.R.layout.simple_list_item_1,
                null,
                new String[] { "TITLE" },
                new int[] { android.R.id.text1 },
                0);

        getSupportLoaderManager().initLoader(0,null, this);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(adapter, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        RandoManagerFragment randoManagerFragment = getRandoManagerFragment();
        if (randoManagerFragment != null) {
            randoManagerFragment.getRando(Calendar.getInstance(), 1);
        }
    }

    private RandoManagerFragment getRandoManagerFragment(){
        return (RandoManagerFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
    }



    @Override
    public void showRando(Rando rando) {
        if (rando != null) {
            Log.d(((Object) this).getClass().getSimpleName(), rando.getDate().toString());
            ((RandoMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).drawRando(rando);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        return true;
    }

    /*
    Cursor loader callbacks
    */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
