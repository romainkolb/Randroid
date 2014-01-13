package org.loutr.randroid.model;

import java.util.Calendar;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Rando {
    private Long id;
    private Calendar date;
    private List<LatLng> aller;
    private List<LatLng> retour;

    private LatLng lastRandoPosition;

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;

        // Set time fields to zero
        this.date.set(Calendar.HOUR_OF_DAY, 0);
        this.date.set(Calendar.MINUTE, 0);
        this.date.set(Calendar.SECOND, 0);
        this.date.set(Calendar.MILLISECOND, 0);
    }

    public List<LatLng> getAller() {
        return aller;
    }

    public void setAller(List<LatLng> aller) {
        this.aller = aller;
    }

    public List<LatLng> getRetour() {
        return retour;
    }

    public void setRetour(List<LatLng> retour) {
        this.retour = retour;
    }

    public LatLng getLastRandoPosition() {
        return lastRandoPosition;
    }

    public void setLastRandoPosition(LatLng lastRandoPosition) {
        this.lastRandoPosition = lastRandoPosition;
    }

    @Override
    public String toString() {
        return "Rando [date=" + date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.MONTH) + "/" + date.get(Calendar.YEAR) + ", aller=" + aller + ", retour="
                + retour + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rando)) {
            return false;
        }
        Rando other = (Rando) o;

        return other.getDate() != null && other.getDate().equals(this.getDate());
    }
}
