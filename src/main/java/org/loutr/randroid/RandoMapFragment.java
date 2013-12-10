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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.app.SherlockMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import org.loutr.randroid.model.Rando;

import java.util.List;

public class RandoMapFragment extends SherlockMapFragment {

    private static final LatLng PARIS = new LatLng(48.8567, 2.3508);
    private static final LatLng NOMADES = new LatLng(48.852175, 2.367853);

        private Polyline currentAller;
    private Polyline currentRetour;
    private Rando currentRando;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getMap() != null) {
            Log.d(((Object) this).getClass().getSimpleName(), "Map ready for use!");
            initMap();
        }
    }

    private void initMap() {

        getMap().addMarker(new MarkerOptions().position(NOMADES).title("Nomades"));

        // Move the camera instantly to Paris with a zoom of 5.
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(PARIS, 5));

        // Zoom in, animating the camera.
        getMap().animateCamera(CameraUpdateFactory.zoomTo(11), 2000, null);


    }

    public void drawRando(Rando rando) {
        if (currentAller != null) {
            currentAller.remove();
        }

        if (currentRetour != null) {
            currentRetour.remove();
        }

        currentRando = rando;

        currentAller = drawSegment(rando.getAller(), 0xc000cc00);
        currentRetour = drawSegment(rando.getRetour(), 0xc0FF0000);

    }

    private Polyline drawSegment(List<LatLng> segment, int color) {
        if (segment != null) {
            LatLng depart;
            LatLng arrivee;
            PolylineOptions po = new PolylineOptions().width(7).color(color);

            depart = segment.get(0);
            for (int i = 1; i < segment.size(); i++) {
                arrivee = segment.get(i);

                po.add(depart, arrivee);

                depart = arrivee;
            }

            return getMap().addPolyline(po);

        }
        return null;
    }

}