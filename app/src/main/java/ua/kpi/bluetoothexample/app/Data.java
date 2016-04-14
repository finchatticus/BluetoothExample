package ua.kpi.bluetoothexample.app;

import android.util.Log;

import java.math.BigDecimal;

/**
 * Created by cooper on 13.04.16.
 */
public class Data {
    private byte[] data;
    private byte type;
    private double distance;
    private double azimuth;
    private double inclination;
    private double roll;

    public Data(byte[] data) {
        this.data = data;
        this.type = data[0];
        calc();
    }

    private void calc() {
        int distance0 = data[1] + (data[2] << 8) + ((type & 0x40) << 10);
        Log.v("dist",String.valueOf(distance0));
        distance = round(distance0 * 0.001, 3);

        int azimuth0 = toInt(data[3], data[4]);
        Log.v("azim",String.valueOf(azimuth0));
        azimuth = round ((azimuth0 * 180) / Math.pow(2,15), 1);

        int inclination0 = toInt(data[5], data[6]);
        Log.v("incl",String.valueOf(inclination0));
        inclination = round ((inclination0 * 180) / Math.pow(2,15), 1);

        int roll0 = toInt(data[7],(byte) 0);
        Log.v("roll",String.valueOf(roll0));
        roll = round((roll0 * 180) / Math.pow(2,7),1);
    }

    private int toInt(byte lb, byte hb)
    {
        return ((int)hb << 8) | ((int)lb & 0xFF);
    }

    public static double round(double unrounded, int precision)
    {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision,  BigDecimal.ROUND_HALF_UP);
        return rounded.doubleValue();
    }

    public byte getType() {
        return type;
    }

    public double getDistance() {
        return distance;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getInclination() {
        return inclination;
    }

    public double getRoll() {
        return roll;
    }

}
