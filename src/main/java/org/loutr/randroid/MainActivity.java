package org.loutr.randroid;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.commonsware.android.mapsv2.sherlock.AbstractMapActivity;
import org.loutr.randroid.data.RandoContract;
import org.loutr.randroid.model.Rando;
import org.loutr.randroid.model.RandoManagerFragment;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AbstractMapActivity implements RandoManagerFragment.Contract, ActionBar.OnNavigationListener {

    private SimpleCursorAdapter adapter;
    private MenuItem refreshItem;

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
        adapter = new SimpleCursorAdapter(
                context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{RandoContract.Rando.COLUMN_NAME_DATE},
                new int[]{android.R.id.text1},
                0);

        adapter.setViewBinder(new RandoViewBinder());

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(adapter, this);

    }

    @Override
    public void onResume(){
        super.onResume();

        RandoManagerFragment randoManagerFragment = getRandoManagerFragment();

        if (randoManagerFragment != null) {
            if(getRandoMapFragment() != null) {
                getRandoMapFragment().setMyLocationEnabled(getRandoManagerFragment().isDisplayGPSOverlay());
            }

            if (getSupportActionBar().getNavigationItemCount() == 0) {
                //Rando list not initialized yet
                toggleRefresh(true);
                if(randoManagerFragment.isRefreshOnStartup()){
                    randoManagerFragment.resetRandos();
                }else{
                    //initialize navigation dropdown
                    randoManagerFragment.initRandoList();
                }
            }else{
                //Refresh rando list if the user has modified the nbRandos pref
                if(getRandoManagerFragment().getNbRandos() != getSupportActionBar().getNavigationItemCount()){
                    toggleRefresh(true);
                    randoManagerFragment.resetRandos();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        //Grab a reference to refresh menu item
        refreshItem = menu.findItem(R.id.refresh_rando);
        return result;
    }

    @Override
    public void onDestroy() {
        adapter.swapCursor(null);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset_randos) {
            resetRandos();
        } else if (item.getItemId() == R.id.refresh_rando) {
            refreshCurrentRando();
        } else if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, Preferences.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void drawRando(Rando rando) {
        if (rando != null) {
            getRandoMapFragment().drawRando(rando);
        }
        toggleRefresh(false);
    }

    @Override
    public void updateRandoCursor(Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        toggleRefresh(true);

        Cursor c = adapter.getCursor();
        c.moveToPosition(itemPosition);
        long dateMs = c.getLong(c.getColumnIndexOrThrow(RandoContract.Rando.COLUMN_NAME_DATE));

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dateMs);

        getRandoManagerFragment().getRandoFromDb(date);

        return true;
    }


    private void resetRandos() {
        toggleRefresh(true);
        getSupportActionBar().setSelectedNavigationItem(0);
        getRandoManagerFragment().resetRandos();
    }

    private void refreshCurrentRando() {
        Rando rando = getRandoMapFragment().getCurrentRando();
        if (rando != null) {
            toggleRefresh(true);
            getRandoManagerFragment().getRandoFromWs(rando.getDate());
        }
    }

    private void toggleRefresh(boolean enable) {
        if (refreshItem != null) {
            if (enable && refreshItem.getActionView() == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

                Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
                rotation.setRepeatCount(Animation.INFINITE);
                iv.startAnimation(rotation);

                refreshItem.setActionView(iv);
            } else {
                if (refreshItem.getActionView() != null) {
                    refreshItem.getActionView().clearAnimation();
                    refreshItem.setActionView(null);
                }
            }
        }
    }


    private RandoManagerFragment getRandoManagerFragment() {
        return (RandoManagerFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
    }

    private RandoMapFragment getRandoMapFragment() {
        return (RandoMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private static final DateFormat df = DateFormat.getDateInstance();

    private class RandoViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndex(RandoContract.Rando.COLUMN_NAME_DATE)) {
                long dateMs = cursor.getLong(columnIndex);

                String dateStr = df.format(dateMs);

                ((TextView) view).setText(dateStr);

                return true;
            }

            return false;
        }
    }

}
