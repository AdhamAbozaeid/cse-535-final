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
        int minThresh = -10, maxThresh = 6, zeroLimit = 2, halfMarkIndex = 0;

        float firstLow, secondLow;
        ArrayList<Float> temp = new ArrayList<>();
        temp.addAll(mAccelZ);

        for(int i = 1; i<temp.size();i++){
            if(temp.get(i-1) < 0 && temp.get(i) > 0)
                zeroLimit -= 1;
            if(temp.get(i-1) > 0 && temp.get(i) < 0)
                zeroLimit -= 1;
            if(zeroLimit == 0){
                halfMarkIndex = i;
                break;
            }
        }
        temp.clear();
        for(int i =0; i < halfMarkIndex;i++){
            temp.add(mAccelZ.get(i));
        }

        firstLow = Collections.min(temp);
        temp.clear();

        for(int i =halfMarkIndex; i < mAccelZ.size();i++){
            temp.add(mAccelZ.get(i));
        }

        secondLow = Collections.min(temp);
        temp.clear();

        //System.out.println("fl: "+firstLow+" sl:"+secondLow);

        if(zeroCrossing >=3){
            if(((Collections.max(mAccelZ) >= maxThresh  &&  (firstLow < 0 && secondLow < 0))
                    || (Collections.max(mAccelZ) >= maxThresh  &&  (firstLow < minThresh && secondLow < minThresh))) && (getZeroCrossing(mRotZ)==0)){
                return true;
            }
        }
        return false;
    }

    public boolean isHungry() {
        float avgX = getAverage(mRotX);
        float avgY = getAverage(mRotY);
        float avgZ = getAverage(mRotZ);
        System.out.println("Average is "+avgX);
        System.out.println("Average is "+avgY);
        System.out.println("Average is "+avgZ);
        System.out.println("------------------");
        if((avgX > 0.45 && avgX < 0.55) && (avgY > -0.55 && avgY < -0.45)) {
            System.out.println("Hunger Detected");
            return true;
        }
        //System.out.println("round of "+Math.round(avgX));
        //else if((avgX>0.45 && avgX<0.55) && (avgX>-0.55 && avgX<-0.45))
        return false;
    }
    private float getAverage(ArrayList<Float> samples){
        float sum = 0;
        float average = 0;
        for(int i=0; i<samples.size(); i++)
            sum = sum + samples.get(i);
        average = sum/samples.size();
        return average;
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
