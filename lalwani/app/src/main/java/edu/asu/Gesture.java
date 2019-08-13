package edu.asu;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class Gesture{
    ArrayList<Float> mGyroX;
    ArrayList<Float> mGyroY;
    ArrayList<Float> mGyroZ;
    ArrayList<Float> mAccelX;
    ArrayList<Float> mAccelY;
    ArrayList<Float> mAccelZ;
    ArrayList<Float> mRotX;
    ArrayList<Float> mRotY;
    ArrayList<Float> mRotZ;
    final static int GESTURE_TYPE_ABOUT = 0;
    final static int GESTURE_TYPE_HUNGRY = 1;
    final static int GESTURE_TYPE_HEADACHE = 2;
    final static int GESTURE_TYPE_COP = 3;
    public int mgestureType;

    public Gesture(int gestureType){
        mgestureType = gestureType;
        mGyroX = new ArrayList<Float>();
        mGyroY = new ArrayList<Float>();
        mGyroZ = new ArrayList<Float>();
        mAccelX = new ArrayList<Float>();
        mAccelY = new ArrayList<Float>();
        mAccelZ = new ArrayList<Float>();
        mRotX = new ArrayList<Float>();
        mRotY = new ArrayList<Float>();
        mRotZ = new ArrayList<Float>();
    }

    public void clearSamples() {
        mGyroX.clear();
        mGyroY.clear();
        mGyroZ.clear();
        mAccelX.clear();
        mAccelY.clear();
        mAccelZ.clear();
        mRotX.clear();
        mRotY.clear();
        mRotZ.clear();
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

    public void addRotSample(float x, float y, float z) {
        mRotX.add(x);
        mRotY.add(y);
        mRotZ.add(z);
    }

    public boolean isAbout(){
        int zeroCrossing = getZeroCrossing(mRotY);
        if(zeroCrossing >= 2) {
            float gyroPeak2Peak = getPeak2Peak(mGyroY);
            Log.d("About Predict", "zerocrossing: "+zeroCrossing+" gyro Peak2Peak: " + gyroPeak2Peak);
            if ( gyroPeak2Peak < 15 && gyroPeak2Peak > 2)
                return true;
        }
        Log.d("About Predict", "zerocrossing: "+zeroCrossing);
        return false;
    }

    public boolean isCop(){
        int zeroCrossing = getZeroCrossing(mAccelZ);
        int minThresh = -10, maxThresh = 7;
        float firstLow, secondLow;
        ArrayList<Float> temp = new ArrayList<>();
        temp.addAll(mAccelZ);
        Collections.sort(temp);
        firstLow = temp.get(0);
        secondLow = temp.get(1);

        if(zeroCrossing >=3){
            if((Collections.max(mAccelZ) > maxThresh  &&  Collections.min(mAccelZ) < minThresh) && (getZeroCrossing(mRotZ)==0) && (firstLow < minThresh && secondLow < minThresh)){
                return true;
            }
        }
        return false;
    }

    public boolean isHungry() {
        return false;
    }

    public boolean isHeadache(){
        int zeroCrossing = getZeroCrossing(mRotY);
        if (zeroCrossing >= 2) {
            float gyroPeak2Peak = getPeak2Peak(mGyroY);
            Log.d("Hungry Predict", "zerocrossing: " + zeroCrossing + " gyro Peak2Peak: " + gyroPeak2Peak);
            if (gyroPeak2Peak >15)
                return true;
        }
        Log.d("Hungry Predict", "zerocrossing: " + zeroCrossing);
        return false;
    }

    private int getZeroCrossing(ArrayList<Float> samples){
        int zeroCrossingUp = 0, zeroCrossingDown = 0;

        for(int i=1; i < samples.size(); i++)
        {
            if(samples.get(i-1) < 0 && samples.get(i) > 0)
                zeroCrossingUp++;
            if(samples.get(i-1) > 0 && samples.get(i) < 0)
                zeroCrossingDown++;
        }
        return zeroCrossingUp + zeroCrossingDown;
    }
    private float getPeak2Peak(ArrayList<Float> samples) {
        return Collections.max(samples) - Collections.min(samples);
    }
}
