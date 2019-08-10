package edu.asu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static int HR_ARR_LEN = 10;
    public static int MAX_HR = 20;
    public static int SAMPLE_GENERATE_RATE = 1000000;
    private final static int NUM_GESTURE_SAMPLES = 16;
    private final static int MY_PERMISSIONS_REQUEST_VIBRATE = 100;
    long lastSampleTime = 0;


    private boolean isRunning = false;
    GraphView graphX;
    GraphView graphY;
    GraphView graphZ;

    GraphView graphGyroX;
    GraphView graphGyroY;
    GraphView graphGyroZ;

    Gesture currGesture[] = new Gesture[NUM_GESTURE_SAMPLES];
    int gestureIdx = 0;

    TextView txtViewSampleID;
    RadioGroup radioGroup;
    RadioButton radioBtnCop;
    RadioButton radioBtnHungry;
    RadioButton radioBtnAbout;
    RadioButton radioBtnHeadache;
    Button collectButton;

    private SensorManager accelManage;
    private Sensor senseAccel;
    private Sensor senseGyro;
    float accelValuesX[] = new float[HR_ARR_LEN];
    float accelValuesY[] = new float[HR_ARR_LEN];
    float accelValuesZ[] = new float[HR_ARR_LEN];

    long timestamps[] = new long[HR_ARR_LEN];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtViewSampleID = (TextView)findViewById(R.id.txtViewSampleId);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioBtnCop = (RadioButton) findViewById(R.id.radioBtnCop);
        radioBtnHungry = (RadioButton) findViewById(R.id.radioBtnHungry);
        radioBtnAbout = (RadioButton) findViewById(R.id.radioBtnAbout);
        radioBtnHeadache = (RadioButton) findViewById(R.id.radioBtnHeadache);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_VIBRATE);
            }
        }

        // Add Start button onClick listener
        final Button startButton = (Button) findViewById(R.id.startButtonID);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData(v);
            }
        });

        final Button predictButton = (Button) findViewById(R.id.predictBtnID);
        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predict();
            }
        });

        // Add Stop button onClick listener
        final Button nextButton = (Button) findViewById(R.id.btnNext);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSample(v);
            }
        });

        final Button prevButton = (Button) findViewById(R.id.btnPrev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevSample(v);
            }
        });

        // Set maximum x and y axis values for Graph 1
        graphX = (GraphView) findViewById(R.id.graphX);
        graphX.getViewport().setMaxY(MAX_HR);
        graphX.getViewport().setMinY(-1*(MAX_HR));
        graphX.getViewport().setYAxisBoundsManual(true);
        graphX.getViewport().setMaxX(HR_ARR_LEN);
        graphX.getViewport().setXAxisBoundsManual(true);

        // Set maximum x and y axis values for Graph 2
        graphY = (GraphView) findViewById(R.id.graphY);
        graphY.getViewport().setMaxY(MAX_HR);
        graphY.getViewport().setMinY(-1*(MAX_HR));
        graphY.getViewport().setYAxisBoundsManual(true);
        graphY.getViewport().setMaxX(HR_ARR_LEN);
        graphY.getViewport().setXAxisBoundsManual(true);

        // Set maximum x and y axis values for Graph 3
        graphZ = (GraphView) findViewById(R.id.graphZ);
        graphZ.getViewport().setMaxY(MAX_HR);
        graphZ.getViewport().setMinY(-1*(MAX_HR));
        graphZ.getViewport().setYAxisBoundsManual(true);
        graphZ.getViewport().setMaxX(HR_ARR_LEN);
        graphZ.getViewport().setXAxisBoundsManual(true);

        // Set Axis Labels
        graphX.getGridLabelRenderer().setHorizontalAxisTitle("\nTime (sec)");
        graphX.getGridLabelRenderer().setVerticalAxisTitle("X-values");

        //Axis Lavbels for Graph 2 (Timestamp vs Y-axis)
        graphY.getGridLabelRenderer().setHorizontalAxisTitle("\nTime (sec)");
        graphY.getGridLabelRenderer().setVerticalAxisTitle("Y-values");

        //Axis Lavbels for Graph 3 (Timestamp vs Z-axis)
        graphZ.getGridLabelRenderer().setHorizontalAxisTitle("\nTime (sec)");
        graphZ.getGridLabelRenderer().setVerticalAxisTitle("Z-values");

        // Set maximum x and y axis values for Graph 1

        graphGyroX = (GraphView) findViewById(R.id.graphGyroX);
        graphGyroX.getViewport().setMaxY(MAX_HR);
        graphGyroX.getViewport().setMinY(-1*(MAX_HR));
        graphGyroX.getViewport().setYAxisBoundsManual(true);
        graphGyroX.getViewport().setMaxX(HR_ARR_LEN);
        graphGyroX.getViewport().setXAxisBoundsManual(true);

        // Set maximum x and y axis values for Graph 2
        graphGyroY = (GraphView) findViewById(R.id.graphGyroY);
        graphGyroY.getViewport().setMaxY(MAX_HR);
        graphGyroY.getViewport().setMinY(-1*(MAX_HR));
        graphGyroY.getViewport().setYAxisBoundsManual(true);
        graphGyroY.getViewport().setMaxX(HR_ARR_LEN);
        graphGyroY.getViewport().setXAxisBoundsManual(true);

        // Set maximum x and y axis values for Graph 3
        graphGyroZ = (GraphView) findViewById(R.id.graphGyroZ);
        graphGyroZ.getViewport().setMaxY(MAX_HR);
        graphGyroZ.getViewport().setMinY(-1*(MAX_HR));
        graphGyroZ.getViewport().setYAxisBoundsManual(true);
        graphGyroZ.getViewport().setMaxX(HR_ARR_LEN);
        graphGyroZ.getViewport().setXAxisBoundsManual(true);

        // Set Axis Labels
        graphGyroX.getGridLabelRenderer().setHorizontalAxisTitle("\nTime (sec)");
        graphGyroX.getGridLabelRenderer().setVerticalAxisTitle("Gyro X-values");

        //Axis Lavbels for Graph 2 (Timestamp vs Y-axis)
        graphGyroY.getGridLabelRenderer().setHorizontalAxisTitle("\nTime (sec)");
        graphGyroY.getGridLabelRenderer().setVerticalAxisTitle("Gyro Y-values");

        //Axis Lavbels for Graph 3 (Timestamp vs Z-axis)
        graphGyroZ.getGridLabelRenderer().setHorizontalAxisTitle("\nTime (sec)");
        graphGyroZ.getGridLabelRenderer().setVerticalAxisTitle("Gyro Z-values");
        
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senseGyro = accelManage.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void collectData(View view) {

        if(isRunning) {
            stopGraph();

        }
        int gestureType = -1;

        if(radioBtnCop.isSelected())
            gestureType = Gesture.GESTURE_TYPE_COP;
        else if(radioBtnHungry.isSelected())
            gestureType = Gesture.GESTURE_TYPE_HUNGRY;
        else if(radioBtnHeadache.isSelected())
            gestureType = Gesture.GESTURE_TYPE_HEADACHE;
        else if(radioBtnAbout.isSelected())
            gestureType = Gesture.GESTURE_TYPE_ABOUT;

        View selectedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());
        gestureType = radioGroup.indexOfChild(selectedRadioButton);

        if(gestureType == -1) {
            Toast.makeText(MainActivity.this, "Please select gesture type!", Toast.LENGTH_LONG).show();
            return;
        }

        currGesture[gestureIdx] = new Gesture(gestureType);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        accelManage.registerListener(MainActivity.this, senseAccel, /*accelManage.SENSOR_DELAY_NORMAL*/SensorManager.SENSOR_DELAY_NORMAL);
        accelManage.registerListener(MainActivity.this, senseGyro, /*accelManage.SENSOR_DELAY_NORMAL*/SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopGraph() {
        accelManage.unregisterListener(this);

        graphX.removeAllSeries();
        graphY.removeAllSeries();
        graphZ.removeAllSeries();
        showSampleData();
    }

    public void nextSample(View view){
        gestureIdx++;
        if(gestureIdx == NUM_GESTURE_SAMPLES)
            gestureIdx = 0;

        showSampleData();
    }

    public void prevSample(View view){
        gestureIdx--;
        if(gestureIdx <0)
            gestureIdx = NUM_GESTURE_SAMPLES-1;

        showSampleData();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        // TODO Auto-generated method stub
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currTimestampsys = System.currentTimeMillis();
            long currTimestamp = sensorEvent.timestamp;

            if (currTimestampsys - lastSampleTime < 1000)
                return;

            lastSampleTime = currTimestampsys;

  /*          if(index < HR_ARR_LEN) {
                accelValuesX[index] = sensorEvent.values[0];
                accelValuesY[index] = sensorEvent.values[1];
                accelValuesZ[index] = sensorEvent.values[2];
                timestamps[index] = currTimestamp;
                index++;
            }
            else{
                for (int i = 0; i < HR_ARR_LEN - 1; i++){
                    accelValuesX[i] = accelValuesX[i+1];
                    accelValuesY[i] = accelValuesY[i+1];
                    accelValuesZ[i] = accelValuesZ[i+1];
                    timestamps[i] = timestamps[i+1];
                }
                accelValuesX[index-1] = sensorEvent.values[0];
                accelValuesY[index-1] = sensorEvent.values[1];
                accelValuesZ[index-1] = sensorEvent.values[2];
                timestamps[index-1] = currTimestamp;
            }*/
        } else if(mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            currGesture[gestureIdx].addAccelSample(sensorEvent.values[0], sensorEvent.values[1],sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    public void showSampleData() {
        LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>();

        // Clear the graph
        graphGyroX.removeAllSeries();
        graphGyroY.removeAllSeries();
        graphGyroZ.removeAllSeries();

        txtViewSampleID.setText("Sample " + (gestureIdx+1));
        if(currGesture[gestureIdx] == null) {
            radioGroup.clearCheck();
            return;
        }

        switch(currGesture[gestureIdx].mgestureType) {
            case Gesture.GESTURE_TYPE_COP:
                radioBtnCop.setSelected(true);
                break;
            case Gesture.GESTURE_TYPE_HUNGRY:
                radioBtnHungry.setSelected(true);
                break;
            case Gesture.GESTURE_TYPE_ABOUT:
                radioBtnAbout.setSelected(true);
                break;
            case Gesture.GESTURE_TYPE_HEADACHE:
                radioBtnHeadache.setSelected(true);
                break;
        }

        // Build new series for the updated hrValues
        for(int i = 0; i<currGesture[gestureIdx].mGyroX.size(); i++) {
            seriesX.appendData(new DataPoint(i, currGesture[gestureIdx].mGyroX.get(i)), true, currGesture[gestureIdx].mGyroX.size());
            seriesY.appendData(new DataPoint(i, currGesture[gestureIdx].mGyroY.get(i)), true, currGesture[gestureIdx].mGyroY.size());
            seriesZ.appendData(new DataPoint(i, currGesture[gestureIdx].mGyroZ.get(i)), true, currGesture[gestureIdx].mGyroZ.size());
        }

        // Update the X axis range
        graphGyroX.getViewport().setMinX(0);
        graphGyroX.getViewport().setMaxX(currGesture[gestureIdx].mGyroX.size());
        // Add the new series to the graph
        graphGyroX.addSeries(seriesX);

        graphGyroY.getViewport().setMinX(0);
        graphGyroY.getViewport().setMaxX(currGesture[gestureIdx].mGyroY.size());
        // Add the new series to the graph
        graphGyroY.addSeries(seriesY);

        graphGyroZ.getViewport().setMinX(0);
        graphGyroZ.getViewport().setMaxX(currGesture[gestureIdx].mGyroZ.size());
        // Add the new series to the graph
        graphGyroZ.addSeries(seriesZ);
    }

    private void predict(){
        
    }
}

