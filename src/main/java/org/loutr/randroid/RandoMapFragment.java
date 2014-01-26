/***
 Based on code from _The Busy Coder's Guide to Android Development_
 http://commonsware.com/Android
 */

package org.loutr.randroid;

import android.os.Bundle;
import android.util.TypedValue;
import com.actionbarsherlock.app.SherlockMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import org.loutr.randroid.model.Rando;

import java.util.List;

public class RandoMapFragment extends SherlockMapFragment {

    private LatLng paris;

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
            initMap();
        }
    }

    @Override
    public void onPause() {
        getMap().setMyLocationEnabled(false);
        super.onPause();
    }

    public void setMyLocationEnabled(boolean enabled){
        getMap().setMyLocationEnabled(enabled);
    }

    private void initCoords() {
        float lat, lng;
        TypedValue tv = new TypedValue();

        getResources().getValue(R.dimen.paris_lat, tv, true);
        lat = tv.getFloat();
        getResources().getValue(R.dimen.paris_lng, tv, true);
        lng = tv.getFloat();
        paris = new LatLng(lat, lng);
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
        if (startingLocation != null) startingLocation.remove();
        if (pauseLocation != null) pauseLocation.remove();
        if (randoLocation != null) randoLocation.remove();

        currentRando = rando;

        maxLat = Double.MIN_VALUE;
        minLat = Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = Double.MIN_VALUE;

        currentAller = drawSegment(rando.getAller(), 0xff67c547);
        currentRetour = drawSegment(rando.getRetour(), 0xffc03639);

        //Draw POIs
        if (currentAller != null && currentAller.getPoints().size() > 0) {
            startingLocation = getMap().addMarker(new MarkerOptions().position(currentAller.getPoints().get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_start)).title(getString(R.string.startingLocation)));
        }
        if (currentRetour != null && currentRetour.getPoints().size() > 0) {
            StringBuilder title = new StringBuilder(getString(R.string.pauseLocation));
            if (rando.getPauseThoroughfare() != null && rando.getPauseThoroughfare().length() > 0) {
                title.append(" : ");
                title.append(rando.getPauseThoroughfare());
            }
            pauseLocation = getMap().addMarker(new MarkerOptions().position(currentRetour.getPoints().get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_pause)).title(title.toString()));
        }

        if (rando.getLastRandoPosition() != null) {
            randoLocation = getMap().addMarker(new MarkerOptions().position(rando.getLastRandoPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_rando_position)).title(getString(R.string.randoLocation)));
        }

        //Zoom to rando
        LatLng maxBound = new LatLng(maxLat + 0.005, maxLon + 0.005);
        LatLng minBound = new LatLng(minLat - 0.005, minLon - 0.005);
        LatLngBounds randoBounds = LatLngBounds.builder().include(maxBound).include(minBound).build();
        getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(randoBounds, 0));
    }

    private Polyline drawSegment(List<LatLng> segment, int color) {
        if (segment != null && segment.size() > 0) {
            LatLng depart;
            LatLng arrivee;
            PolylineOptions po = new PolylineOptions().width(7).color(color);

            depart = segment.get(0);
            for (int i = 1; i < segment.size(); i++) {

                if (depart.latitude > maxLat) maxLat = depart.latitude;
                if (depart.latitude < minLat) minLat = depart.latitude;
                if (depart.longitude > maxLon) maxLon = depart.longitude;
                if (depart.longitude < minLon) minLon = depart.longitude;

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