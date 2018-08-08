package com.mathieu.paf;

/**
 * Created by Mathieu on 17/06/2015.
 */
public class PointCarte {

    private double latitude;
    private double longitude;
    private float precision;
    private int RSSI;
    private float SNR;

    PointCarte(double latitude, double longitude, float precision, int RSSI, float SNR) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.precision = precision;
        this.RSSI = RSSI;
        this.SNR = SNR;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getPrecision() {
        return precision;
    }

    public int getRSSI() {
        return RSSI;
    }

    public float getSNR() {
        return SNR;
    }
}
