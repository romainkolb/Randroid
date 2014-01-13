/***
 Copyright (c) 2013 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Android Development_
 http://commonsware.com/Android
 */

package org.loutr.randroid;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import com.actionbarsherlock.app.SherlockMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import org.loutr.randroid.data.RandoContract;
import org.loutr.randroid.model.Rando;

import java.util.List;

public class RandoMapFragment extends SherlockMapFragment {

    private LatLng paris;
    //private LatLng nomades;

    private Polyline currentAller;
    private Polyline currentRetour;
    private Rando currentRando;

    private Marker startingLocation;
    private Marker pauseLocation;
    private Marker randoLocation;

    private double maxLat;
    private double minLat;
    private double maxLon;
    private double minLon;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getMap() != null) {
            getMap().setMyLocationEnabled(true);
            initMap();
        }
    }

    private void initCoords() {
        float lat, lng;
        TypedValue tv = new TypedValue();

        getResources().getValue(R.dimen.paris_lat, tv, true);
        lat = tv.getFloat();
        getResources().getValue(R.dimen.paris_lng, tv, true);
        lng = tv.getFloat();
        paris = new LatLng(lat, lng);

        /*getResources().getValue(R.dimen.nomades_lat, tv, true);
        lat = tv.getFloat();
        getResources().getValue(R.dimen.nomades_lng, tv, true);
        lng = tv.getFloat();
        nomades = new LatLng(lat, lng);*/
    }

    private void initMap() {
        if (paris == null) {
            initCoords();
        }

        // Move the camera instantly to Paris with a zoom of 5.
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 5));

        // Zoom in, animating the camera.
        getMap().animateCamera(CameraUpdateFactory.zoomTo(11), 2000, null);

    }

    public void drawRando(Rando rando) {
        if (currentAller != null) currentAller.remove();
        if (currentRetour != null) currentRetour.remove();
        if(startingLocation != null) startingLocation.remove();
        if(pauseLocation != null) pauseLocation.remove();

        currentRando = rando;

        maxLat = Double.MIN_VALUE;
        minLat = Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = Double.MIN_VALUE;

        currentAller = drawSegment(rando.getAller(), 0xff67c547);
        currentRetour = drawSegment(rando.getRetour(), 0xffc03639);

        //Draw POIs
        if(currentAller != null && currentAller.getPoints().size()>0){
            startingLocation = getMap().addMarker(new MarkerOptions().position(currentAller.getPoints().get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_start)).title(getString(R.string.startingLocation)));
        }
        if(currentRetour != null && currentRetour.getPoints().size()>0){
            String title = getString(R.string.pauseLocation)+" : "+ rando.getPauseThoroughfare();
            pauseLocation = getMap().addMarker(new MarkerOptions().position(currentRetour.getPoints().get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_pause)).title(title));
        }

        if(rando.getLastRandoPosition() != null){
             randoLocation = getMap().addMarker(new MarkerOptions().position(rando.getLastRandoPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_rando_position)).title(getString(R.string.randoLocation)));
        }

        //Zoom to rando
        LatLng maxBound = new LatLng(maxLat+0.005,maxLon+0.005);
        LatLng minBound = new LatLng(minLat-0.005,minLon-0.005);
        LatLngBounds randoBounds = LatLngBounds.builder().include(maxBound).include(minBound).build();
        getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(randoBounds, 0));
    }

    private Polyline drawSegment(List<LatLng> segment, int color) {
        if (segment != null && segment.size()>0) {
            LatLng depart;
            LatLng arrivee;
            PolylineOptions po = new PolylineOptions().width(7).color(color);

            depart = segment.get(0);
            for (int i = 1; i < segment.size(); i++) {

                if(depart.latitude > maxLat) maxLat = depart.latitude;
                if(depart.latitude < minLat) minLat = depart.latitude;
                if(depart.longitude > maxLon) maxLon = depart.longitude;
                if(depart.longitude < minLon) minLon = depart.longitude;

                arrivee = segment.get(i);

                po.add(depart, arrivee);

                depart = arrivee;
            }
            return getMap().addPolyline(po);
        }
        return null;
    }

    public Rando getCurrentRando() {
        return currentRando;
    }
}