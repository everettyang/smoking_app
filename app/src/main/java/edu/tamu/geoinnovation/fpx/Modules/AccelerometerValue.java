package edu.tamu.geoinnovation.fpx.Modules;

/**
 * Created by atharmon on 2/26/2016.
 */
public class AccelerometerValue extends Value {
    public float X;
    public float Y;
    public float Z;

    public AccelerometerValue() {

    }

    public AccelerometerValue(float x, float y, float z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }
}