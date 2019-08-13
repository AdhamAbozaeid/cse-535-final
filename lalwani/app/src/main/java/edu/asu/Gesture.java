package edu.asu;


import java.util.ArrayList;

public class Gesture{
    ArrayList<Float> mGyroX;
    ArrayList<Float> mGyroY;
    ArrayList<Float> mGyroZ;
    ArrayList<Float> mAccelX;
    ArrayList<Float> mAccelY;
    ArrayList<Float> mAccelZ;
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
        mAccelX = new ArrayList<Float>();
        mAccelY = new ArrayList<Float>();
        mAccelZ = new ArrayList<Float>();
    }

    public void clearSamples() {
        mGyroX.clear();
        mGyroY.clear();
        mGyroZ.clear();
        mAccelX.clear();
        mAccelY.clear();
        mAccelZ.clear();
    }
    public void addGyroSample(float x, float y, float z) {
        mGyroX.add(x);
        mGyroY.add(y);
        mGyroZ.add(z);
    }

    public void addAccelSample(float x, float y, float z) {
        mAccelX.add(x);
        mAccelY.add(y);
        mAccelZ.add(z);
    }

    public boolean isAbout(){
        return true;
    }

    public boolean isCop(){
        return true;
    }

    public boolean isHungry(){

        return true;
    }

    public boolean isHeadache(){
        return true;
    }

}
