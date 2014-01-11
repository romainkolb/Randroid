package org.loutr.randroid.model;

import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.commonsware.android.retrofit.ContractFragment;
import org.loutr.randroid.R;
import org.loutr.randroid.data.RandoDbHelper;
import org.loutr.randroid.data.Utils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

        return null;
    }

    public void getRandoFromWs(Calendar date, Integer nbPos) {
        String textDate = DateFormat.format("yyyyMMdd", date).toString();
        rc.getRando(textDate, nbPos, randoGetter);
    }

    public void getRandoFromDb(Calendar date){
        db.getRandoAsync(date,this);
    }

    public void initRandoList(){
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
            if(rando.getAller() != null && rando.getAller().size()>0){
            getContract().drawRando(rando);
            }else{
                //We haven't downloaded this rando yet, let's do it
                //TODO : get nbpos from prefs
                getRandoFromWs(rando.getDate(), 1);
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

    public interface Contract {
        void drawRando(Rando rando);

        void updateRandoCursor(Cursor cursor);
    }

    private class RandoGetter implements Callback<Rando> {
        @Override
        public void success(Rando rando, Response response) {
            saveRando(rando);
            getContract().drawRando(rando);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(((Object) this).getClass().getSimpleName(), "Exception from Retrofit request to Roller&Coquillages", retrofitError);
        }
    }

    private class RandoInitializer implements Callback<Rando> {

        @Override
        public void success(Rando rando, Response response) {
            //TODO : get nb randos pref
            List<Rando> randos = Utils.getPreviousRandos(rando.getDate(), 5);
            randos.add(0,rando);
            saveRandos(randos);
            getContract().drawRando(rando);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(((Object) this).getClass().getSimpleName(), "Exception from Retrofit request to Roller&Coquillages", retrofitError);
        }
    }
}
