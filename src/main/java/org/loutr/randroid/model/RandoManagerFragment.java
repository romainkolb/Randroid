package org.loutr.randroid.model;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.commonsware.android.retrofit.ContractFragment;
import com.google.android.gms.maps.model.LatLng;
import org.loutr.randroid.R;
import org.loutr.randroid.data.RandoDbHelper;
import org.loutr.randroid.data.Utils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by romain on 12/8/13.
 */
public class RandoManagerFragment extends ContractFragment<RandoManagerFragment.Contract> implements RandoDbHelper.RandoListener {
    private RollersCoquillagesService rc;
    private RandoDbHelper db;

    private final RandoGetter randoGetter = new RandoGetter();
    private final RandoInitializer randoInitializer = new RandoInitializer();

    private static Geocoder geocoder;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        RestAdapter restAdapter =
                new RestAdapter.Builder().setServer(getString(R.string.rollers_coquillages_server)).setConverter(new RandoConverter())
                        .build();
        rc = restAdapter.create(RollersCoquillagesService.class);

        db = new RandoDbHelper(getActivity().getApplicationContext());

        geocoder = new Geocoder(getActivity());

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefs.getAll();

        return null;
    }

    public void getRandoFromWs(Calendar date) {
        Integer nbPos = getNbPositions();
        String textDate = DateFormat.format("yyyyMMdd", date).toString();
        rc.getRando(textDate, nbPos, randoGetter);
    }

    public void getRandoFromDb(Calendar date) {
        db.getRandoAsync(date, this);
    }

    public void initRandoList() {
        db.getAllRandosAsync(this);
    }

    public void resetRandos() {
        //delete all Randos. resetComplete() will then be called to init the Randos
        db.deleteAllRandosAsync(this);
    }

    private void saveRando(Rando rando) {
        List<Rando> randos = new ArrayList<Rando>(1);
        randos.add(rando);
        saveRandos(randos);
    }

    private void saveRandos(List<Rando> randos) {
        db.saveRandosAsync(randos, this);
    }

    @Override
    public void setRando(Rando rando) {
        if (rando != null) {
            if (rando.getAller() != null && rando.getAller().size() > 0) {
                getContract().drawRando(rando);
            } else {
                //We haven't downloaded this rando yet, let's do it
                getRandoFromWs(rando.getDate());
            }
        }
    }

    @Override
    public void resetComplete() {
        //Initialize DB with most recent Rando
        rc.getRando("", 0, randoInitializer);
    }

    @Override
    public void randosSaved() {
        db.getAllRandosAsync(this);
    }

    @Override
    public void updateCursor(Cursor cursor) {
        getContract().updateRandoCursor(cursor);
    }

    /**
     * Must be implemented by the Activity
     */
    public interface Contract {
        void drawRando(Rando rando);

        void updateRandoCursor(Cursor cursor);

        void cancelProgress();
    }

    private class RandoGetter implements Callback<Rando> {
        @Override
        public void success(Rando rando, Response response) {
            RandoProcessor randoProcessor = new RandoGetterProcessor();
            Utils.executeAsyncTask(new GeocodeAsync(randoProcessor), rando);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(((Object) this).getClass().getSimpleName(), "Exception from Retrofit request to Roller&Coquillages", retrofitError);
            processRetrofitError(retrofitError);
        }
    }

    private class RandoInitializer implements Callback<Rando> {

        @Override
        public void success(Rando rando, Response response) {
            RandoProcessor randoProcessor = new RandoInitializerProcessor();
            Utils.executeAsyncTask(new GeocodeAsync(randoProcessor), rando);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(((Object) this).getClass().getSimpleName(), "Exception from Retrofit request to Roller&Coquillages", retrofitError);
            processRetrofitError(retrofitError);
        }
    }

    private interface RandoProcessor {
        void processRando(Rando rando);
    }

    private class RandoGetterProcessor implements RandoProcessor {
        @Override
        public void processRando(Rando rando) {
            saveRando(rando);
            getContract().drawRando(rando);
        }
    }

    private class RandoInitializerProcessor implements RandoProcessor {
        @Override
        public void processRando(Rando rando) {
            //We decrement nb randos because we already have downloaded one Rando
            List<Rando> randos = Utils.getPreviousRandos(rando.getDate(), getNbRandos()-1);
            randos.add(0, rando);
            saveRandos(randos);
            getContract().drawRando(rando);
        }
    }


    private class GeocodeAsync extends AsyncTask<Rando, Void, Rando> {
        private RandoProcessor randoProcessor;

        GeocodeAsync(RandoProcessor randoProcessor) {
            this.randoProcessor = randoProcessor;
        }

        @Override
        protected Rando doInBackground(Rando... params) {
            Rando rando = params[0];

            if (rando != null && rando.getRetour() != null && rando.getRetour().size() > 0) {
                LatLng pause = rando.getRetour().get(0);
                try {
                    List<Address> pauseAddress = geocoder.getFromLocation(pause.latitude, pause.longitude, 1);
                    if (pauseAddress != null && pauseAddress.size() > 0) {
                        String thoroughfare = pauseAddress.get(0).getThoroughfare();
                        rando.setPauseThoroughfare(thoroughfare);
                    }
                } catch (IOException e) {
                    Log.e(this.getClass().getSimpleName(), "error while geocoding pause address", e);
                }
            }


            return rando;
        }

        @Override
        public void onPostExecute(Rando rando) {
            randoProcessor.processRando(rando);
        }
    }

    public int getNbRandos() {
        int def = getResources().getInteger(R.integer.defaultNbRandos);
        return  Integer.parseInt(prefs.getString(getString(R.string.prefKeyNbRandos), String.valueOf(def)));
    }

    public int getNbPositions() {
        int def = getResources().getInteger(R.integer.defaultNbPositions);
        //Not user configurable for now
        return def;
    }

    public boolean isRefreshOnStartup() {
        boolean def = getResources().getBoolean(R.bool.defaultRefreshOnStartup);
        return prefs.getBoolean(getString(R.string.prefKeyRefreshOnStartup), def);
    }

    public boolean isDisplayGPSOverlay(){
        boolean def = getResources().getBoolean(R.bool.defaultGPSOverlay);
        return prefs.getBoolean(getString(R.string.prefKeyGPSOverlay),def);
    }


    private void processRetrofitError(RetrofitError retrofitError){
        if(retrofitError != null){
            String errorMessage;
        if(retrofitError.isNetworkError()){
            errorMessage = getResources().getString(R.string.network_error);
        }else{
            errorMessage = String.format(getResources().getString(R.string.read_error),retrofitError.getLocalizedMessage());
        }
             Toast.makeText(getActivity(),errorMessage,Toast.LENGTH_SHORT).show();
        }

        getContract().cancelProgress();
    }
}
