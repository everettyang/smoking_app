package edu.tamu.geoinnovation.fpx.Modules;

/**
 * Created by atharmon on 2/26/2016.
 */
public class GPSValue extends Value {
    public double Latitude;
    public double Longitude;
    public double Elevation;
    public double Bearing;

    public GPSValue(double lat, double lon, float ele, float bearing) {
        this.Latitude = lat;
        this.Longitude = lon;
        this.Elevation = ele;
        this.Bearing = bearing;
    }
}
