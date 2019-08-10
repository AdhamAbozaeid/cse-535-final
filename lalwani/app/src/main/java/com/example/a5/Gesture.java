package com.example.a5;


import java.util.ArrayList;

public class Gesture{
    ArrayList<Float> mGyroX;
    ArrayList<Float> mGyroY;
    ArrayList<Float> mGyroZ;
    final static int GESTURE_TYPE_COP = 0;
    final static int GESTURE_TYPE_HUNGRY = 1;
    final static int GESTURE_TYPE_HEADACHE = 2;
    final static int GESTURE_TYPE_ABOUT = 3;
    public int mgestureType;

    public Gesture(int gestureType){
        mgestureType = gestureType;
        mGyroX = new ArrayList<Float>();
        mGyroY = new ArrayList<Float>();
        mGyroZ = new ArrayList<Float>();
    }

    public void clearSamples() {
        mGyroX.clear();
        mGyroY.clear();
        mGyroZ.clear();
    }
    public void addAccelSample(float x, float y, float z) {
        mGyroX.add(x);
        mGyroY.add(y);
        mGyroZ.add(z);
    }
}
