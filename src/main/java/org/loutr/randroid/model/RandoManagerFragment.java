package org.loutr.randroid.model;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.commonsware.android.retrofit.ContractFragment;
import org.loutr.randroid.R;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.Calendar;

/**
 * Created by romain on 12/8/13.
 */
public class RandoManagerFragment extends ContractFragment<RandoManagerFragment.Contract> implements Callback<Rando> {
    private RollersCoquillagesService rc;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        RestAdapter restAdapter =
                new RestAdapter.Builder().setServer(getString(R.string.rollers_coquillages_server)).setConverter(new RandoConverter())
                        .build();
        rc = restAdapter.create(RollersCoquillagesService.class);

        return null;
    }


    public void getRando(Calendar date, Integer nbPos) {
        String textDate = DateFormat.format("yyyyMMdd", date).toString();
        rc.getRando(textDate, nbPos, this);
    }

    @Override
    public void success(Rando rando, Response response) {
        getContract().showRando(rando);
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.e(((Object) this).getClass().getSimpleName(), "Exception from Retrofit request to Roller&Coquillages", retrofitError);
    }

    public interface Contract {
        void showRando(Rando rando);
    }
}
