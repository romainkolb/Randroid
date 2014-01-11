package org.loutr.randroid.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.loutr.randroid.model.Rando;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RandoDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database
    // version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Rando.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_RANDOS = "CREATE TABLE "
            + RandoContract.Rando.TABLE_NAME + " ("
            + RandoContract.Rando._ID + " INTEGER PRIMARY KEY,"
            + RandoContract.Rando.COLUMN_NAME_DATE + INTEGER_TYPE + " UNIQUE"
            + " )";

    private static final String SQL_CREATE_LATLNG = "CREATE TABLE "
            + RandoContract.CheckPoint.TABLE_NAME + " ("
            + RandoContract.CheckPoint._ID + " INTEGER PRIMARY KEY,"
            + RandoContract.CheckPoint.COLUMN_NAME_RANDO_ID + INTEGER_TYPE + COMMA_SEP
            + RandoContract.CheckPoint.COLUMN_NAME_SEGMENT + INTEGER_TYPE + COMMA_SEP
            + RandoContract.CheckPoint.COLUMN_NAME_POSITION + INTEGER_TYPE + COMMA_SEP
            + RandoContract.CheckPoint.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP
            + RandoContract.CheckPoint.COLUMN_NAME_LONGITUDE + REAL_TYPE + COMMA_SEP
            + " FOREIGN KEY ( " + RandoContract.CheckPoint.COLUMN_NAME_RANDO_ID + " ) REFERENCES " + RandoContract.Rando.TABLE_NAME + "(" + RandoContract.Rando._ID + ") ON DELETE CASCADE"
            + " )";

    private static final String SQL_DELETE_RANDOS = "DROP TABLE IF EXISTS "
            + RandoContract.Rando.TABLE_NAME;

    private static final String SQL_DELETE_LATLNG = "DROP TABLE IF EXISTS "
            + RandoContract.CheckPoint.TABLE_NAME;

    public RandoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RANDOS);
        db.execSQL(SQL_CREATE_LATLNG);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy
        // is to simply to discard the data and start over
        db.execSQL(SQL_DELETE_LATLNG);
        db.execSQL(SQL_DELETE_RANDOS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);

    }

    public interface RandoListener {
        /**
         * Called after the Rando has been retrieved from DB
         *
         * @param rando
         */
        void setRando(Rando rando);

        /**
         * Called after all Randos have been deleted from DB
         */
        void resetComplete();

        /**
         * Called after saving one or several randos
         */
        void randosSaved();

        void updateCursor(Cursor cursor);
    }

    private class GetRandoTask extends AsyncTask<Calendar, Void, Rando> {
        private RandoListener listener = null;

        GetRandoTask(RandoListener listener) {
            this.listener = listener;
        }

        @Override
        protected Rando doInBackground(Calendar... params) {
            Calendar date = params[0];

            String[] args = {Long.toString(date.getTimeInMillis())};
            Cursor c =
                    getReadableDatabase().rawQuery("SELECT " + RandoContract.Rando._ID + " FROM " + RandoContract.Rando.TABLE_NAME + " WHERE " + RandoContract.Rando.COLUMN_NAME_DATE + "=?", args);
            c.moveToFirst();
            if (c.isAfterLast()) {
                return null;
            }

            Long randoId = c.getLong(0);
            c.close();

            Rando rando = new Rando();
            rando.setDate(date);
            rando.setId(randoId);

            rando.setAller(getCheckPointsByRandoAndSegment(rando, 1));
            rando.setRetour(getCheckPointsByRandoAndSegment(rando, 2));

            return rando;
        }

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        private final String[] GET_CHECKPOINTS_PROJECTION = {
                RandoContract.CheckPoint.COLUMN_NAME_LATITUDE,
                RandoContract.CheckPoint.COLUMN_NAME_LONGITUDE
        };

        // How you want the results sorted in the resulting Cursor
        private static final String GET_CHECKPOINTS_SORT_ORDER = RandoContract.CheckPoint.COLUMN_NAME_POSITION + " ASC";

        private static final String GET_CHECKPOINTS_BY_RANDO_AND_SEGMENT_SELECTION = RandoContract.CheckPoint.COLUMN_NAME_RANDO_ID + " = ? AND " +
                RandoContract.CheckPoint.COLUMN_NAME_SEGMENT + " = ?";

        private List<LatLng> getCheckPointsByRandoAndSegment(Rando rando, long segment) {
            List<LatLng> checkpoints = new ArrayList<LatLng>();

            String[] selectionCriterias = {
                    rando.getId().toString(),
                    Long.toString(segment)
            };

            Cursor c = getReadableDatabase().query(
                    RandoContract.CheckPoint.TABLE_NAME,
                    GET_CHECKPOINTS_PROJECTION,
                    GET_CHECKPOINTS_BY_RANDO_AND_SEGMENT_SELECTION,
                    selectionCriterias,
                    null,
                    null,
                    GET_CHECKPOINTS_SORT_ORDER
            );

            c.moveToFirst();
            while (!c.isAfterLast()) {
                double latitude = c.getDouble(c.getColumnIndexOrThrow(RandoContract.CheckPoint.COLUMN_NAME_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndexOrThrow(RandoContract.CheckPoint.COLUMN_NAME_LONGITUDE));

                LatLng checkpoint = new LatLng(latitude, longitude);

                checkpoints.add(checkpoint);
                c.moveToNext();
            }
            c.close();
            return checkpoints;
        }

        @Override
        public void onPostExecute(Rando rando) {
            listener.setRando(rando);
        }
    }

    public void getRandoAsync(Calendar date, RandoListener randoListener) {
        Utils.executeAsyncTask(new GetRandoTask(randoListener), date);
    }

    private class SaveRandosTask extends AsyncTask<Rando, Void, Void> {
        private RandoListener listener;

        private SQLiteDatabase db;

        SaveRandosTask(RandoListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Rando... randos) {
            try {

                if (randos == null) {
                    return null;
                }


                db = getWritableDatabase();

                db.beginTransaction();

                for (Rando rando : randos) {

                    String[] args = new String[]{Long.toString(rando.getDate().getTimeInMillis())};

                    Cursor c =
                            getReadableDatabase().rawQuery("SELECT " + RandoContract.Rando._ID + " FROM " + RandoContract.Rando.TABLE_NAME + " WHERE " + RandoContract.Rando.COLUMN_NAME_DATE + "=?", args);
                    c.moveToFirst();

                    if (c.isAfterLast()) {
                        //Rando not found in DB, lets create it
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(RandoContract.Rando.COLUMN_NAME_DATE, rando.getDate()
                                .getTimeInMillis());

                        long newRowId = getWritableDatabase().insert(RandoContract.Rando.TABLE_NAME, null, values);
                        rando.setId(newRowId);
                    } else {
                        //Rando found in DB, let's drop the coords to refresh them
                        rando.setId(c.getLong(c.getColumnIndexOrThrow(RandoContract.Rando._ID)));
                        db.delete(RandoContract.CheckPoint.TABLE_NAME, RandoContract.CheckPoint.COLUMN_NAME_RANDO_ID + "=?", new String[]{rando.getId().toString()});
                    }

                    if (rando.getAller() != null) {
                        long position = 1;
                        for (LatLng cp : rando.getAller()) {
                            createLatLng(rando, 1, position, cp);
                            position++;
                        }
                    }

                    if(rando.getRetour() != null){
                        long position = 1;

                        for (LatLng cp : rando.getRetour()) {
                            createLatLng(rando, 2, position, cp);
                            position++;
                        }
                    }

                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error while saving Randos", e);
            } finally {
                db.endTransaction();
            }
            return null;
        }

        private long createLatLng(Rando rando, long segment, long position,
                                  com.google.android.gms.maps.model.LatLng latlng) {
            long newRowId;

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(RandoContract.CheckPoint.COLUMN_NAME_RANDO_ID, rando.getId());
            values.put(RandoContract.CheckPoint.COLUMN_NAME_SEGMENT, segment);
            values.put(RandoContract.CheckPoint.COLUMN_NAME_POSITION, position);
            values.put(RandoContract.CheckPoint.COLUMN_NAME_LATITUDE,
                    latlng.latitude);
            values.put(RandoContract.CheckPoint.COLUMN_NAME_LONGITUDE,
                    latlng.longitude);

            newRowId = db.insert(RandoContract.CheckPoint.TABLE_NAME, null, values);
            return newRowId;
        }

        @Override
        public void onPostExecute(Void result) {
            listener.randosSaved();
        }
    }

    public void saveRandosAsync(List<Rando> randos, RandoListener listener) {
        Rando[] randoTab = randos.toArray(new Rando[randos.size()]);
        Utils.executeAsyncTask(new SaveRandosTask(listener), randoTab);
    }

    private class DeleteAllRandosTask extends AsyncTask<Void, Void, Void> {
        private RandoListener listener = null;

        DeleteAllRandosTask(RandoListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            getWritableDatabase().delete(RandoContract.Rando.TABLE_NAME, null, null);
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            listener.resetComplete();
        }
    }

    public void deleteAllRandosAsync(RandoListener randoListener) {
        Utils.executeAsyncTask(new DeleteAllRandosTask(randoListener));
    }


    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    private static final String[] GET_RANDO_PROJECTION = {
            RandoContract.Rando._ID,
            RandoContract.Rando.COLUMN_NAME_DATE};

    // How you want the results sorted in the resulting Cursor
    private static final String GET_ALL_RANDOS_SORT_ORDER = RandoContract.Rando.COLUMN_NAME_DATE + " DESC";


    private class GetAllRandosTask extends AsyncTask<Rando, Void, Void> {
        private RandoListener listener;

        private Cursor cursor;

        GetAllRandosTask(RandoListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Rando... randos) {
            try {
                cursor = getReadableDatabase().query(
                        RandoContract.Rando.TABLE_NAME,    // The table to query
                        GET_RANDO_PROJECTION,               // The columns to return
                        null,                                // The columns for the WHERE clause
                        null,                                // The values for the WHERE clause
                        null,                                // don't group the rows
                        null,                                // don't filter by row groups
                        GET_ALL_RANDOS_SORT_ORDER            // The sort order
                );

            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error creating cursor", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            if(cursor.getCount()==0){
                //no randos found in DB, let's trigger a reset
                listener.resetComplete();
            }else{
                listener.updateCursor(cursor);
            }
        }
    }

    public void getAllRandosAsync(RandoListener listener) {
        Utils.executeAsyncTask(new GetAllRandosTask(listener));
    }
}
