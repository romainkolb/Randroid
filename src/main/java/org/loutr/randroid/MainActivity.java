package org.loutr.randroid;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.commonsware.android.mapsv2.sherlock.AbstractMapActivity;
import org.loutr.randroid.data.RandoContract;
import org.loutr.randroid.model.Rando;
import org.loutr.randroid.model.RandoManagerFragment;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AbstractMapActivity implements RandoManagerFragment.Contract, ActionBar.OnNavigationListener, LocationListener {

    private SimpleCursorAdapter adapter;
    private LocationManager locationManager=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        RandoManagerFragment randoManagerFragment = getRandoManagerFragment();
        if (randoManagerFragment != null) {
            if (getSupportActionBar().getNavigationItemCount() == 0) {
                //initialize navigation dropdown
                setSupportProgressBarIndeterminateVisibility(true);
                randoManagerFragment.initRandoList();
            }
        }
    }

    @Override
    public void onDestroy(){
        adapter.swapCursor(null);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset_randos) {
            setSupportProgressBarIndeterminateVisibility(true);
            getSupportActionBar().setSelectedNavigationItem(0);
            getRandoManagerFragment().resetRandos();
        } else if (item.getItemId() == R.id.getLocation){
            //Request location immediately
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void drawRando(Rando rando) {
        //Loading done, let's hide the loading indicator
        setSupportProgressBarIndeterminateVisibility(false);
        if (rando != null) {
            getRandoMapFragment().drawRando(rando);
        }
    }

    @Override
    public void updateRandoCursor(Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Cursor c = adapter.getCursor();
        c.moveToPosition(itemPosition);
        long dateMs = c.getLong(c.getColumnIndexOrThrow(RandoContract.Rando.COLUMN_NAME_DATE));

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dateMs);

        getRandoManagerFragment().getRandoFromDb(date);

        return true;
    }


    private RandoManagerFragment getRandoManagerFragment() {
        return (RandoManagerFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
    }

    private RandoMapFragment getRandoMapFragment() {
        return (RandoMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private static final DateFormat df = DateFormat.getDateInstance();

    @Override
    public void onLocationChanged(Location location) {
        getRandoMapFragment().updateLocation(location);
        //We no longer need location updates
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // required for interface, not used
    }

    @Override
    public void onProviderEnabled(String provider) {
        // required for interface, not used
    }

    @Override
    public void onProviderDisabled(String provider) {
        // required for interface, not used
    }

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
