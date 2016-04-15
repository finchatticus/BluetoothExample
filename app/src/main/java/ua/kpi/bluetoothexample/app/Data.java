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
    private int distance0;
    private int azimuth0;
    private int inclination0;
    private int roll0;

    public Data(byte[] data) {
        this.data = data;
        this.type = data[0];
        calc();
    }

    private void calc() {
        distance0 = data[1] + (data[2] << 8) + ((type & 0x40) << 10);
        Log.v("dist",String.valueOf(distance0));
        distance = round(distance0 * 0.001, 3);

        azimuth0 = toInt(data[3], data[4]);
        Log.v("azim",String.valueOf(azimuth0));
        azimuth = round ((azimuth0 * 180) / Math.pow(2,15), 1);

        inclination0 = toInt(data[5], data[6]);
        Log.v("incl",String.valueOf(inclination0));
        inclination = round ((inclination0 * 180) / Math.pow(2,15), 1);

        roll0 = toInt(data[7],(byte) 0);
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

    public byte[] getData() {
        return data;
    }

    public int getDistance0() {
        return distance0;
    }

    public int getAzimuth0() {
        return azimuth0;
    }

    public int getInclination0() {
        return inclination0;
    }

    public int getRoll0() {
        return roll0;
    }

    public String getBites(int i) {
        if(i < 8) {
            byte b1 = data[i];
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            Byte l = b1;
            int k = l.intValue();
            return s1 + " " + String.valueOf(k);
        }
        else
            return "null";
    }
}
