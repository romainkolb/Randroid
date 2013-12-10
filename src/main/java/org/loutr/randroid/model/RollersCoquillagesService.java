package org.loutr.randroid.model;

import org.loutr.randroid.model.Rando;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by romain on 12/8/13.
 */
public interface RollersCoquillagesService {
    //http://www.rollers-coquillages.org/parcours/kml/20101003/10
    @GET("/parcours/kml/{date}/{nbpos}")
    void getRando(@Path("date") String date, @Path("nbpos") Integer nbPos, Callback<Rando> cb);
}
