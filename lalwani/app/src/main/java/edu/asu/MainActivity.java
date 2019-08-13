package edu.asu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.util.Collections;

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

    Gesture gestures[] = new Gesture[NUM_GESTURE_SAMPLES];
    int gestureIdx = 0;

    TextView txtViewSampleID;
    TextView txtViewTruePos;
    TextView txtViewFalsePos;
    RadioGroup radioGroup;
    RadioButton radioBtnCop;
    RadioButton radioBtnHungry;
    RadioButton radioBtnAbout;
    RadioButton radioBtnHeadache;
    Button collectBtn;
    CheckBox chckBoxCop;
    CheckBox chckBoxHungry;
    CheckBox chckBoxAbout;
    CheckBox chckBoxHeadache;


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
        txtViewTruePos = (TextView)findViewById(R.id.txtViewTP);
        txtViewFalsePos = (TextView)findViewById(R.id.txtViewFP);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioBtnCop = (RadioButton) findViewById(R.id.radioBtnCop);
        radioBtnHungry = (RadioButton) findViewById(R.id.radioBtnHungry);
        radioBtnAbout = (RadioButton) findViewById(R.id.radioBtnAbout);
        radioBtnHeadache = (RadioButton) findViewById(R.id.radioBtnHeadache);
        chckBoxCop = (CheckBox) findViewById(R.id.chckBoxCop);
        chckBoxHungry = (CheckBox) findViewById(R.id.chckBoxHungry);
        chckBoxHeadache = (CheckBox) findViewById(R.id.chckBoxHeadache);
        chckBoxAbout = (CheckBox) findViewById(R.id.chckBoxAbout);

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
        collectBtn = (Button) findViewById(R.id.CollectBtnID);
        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCollectTask();
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
        //senseGyro = accelManage.getDefaultSensor(Sensor.TYPE_ROTATION);

        float[] n2 = {1.5f, 3.9f, 4.1f, 3.3f};
        //float[] n2 = {2.1f, 2.45f, 3.673f, 4.32f, 2.05f, 1.93f, 5.67f, 6.01f};
        float[] n1 = {2.1f, 2.45f, 3.673f, 4.32f, 2.05f, 1.93f, 5.67f, 6.01f};
        DTW dtw = new DTW(n1, n2);
        System.out.println(dtw);
    }

    public void startCollectTask(){
        int gestureType = -1;
        View selectedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());
        gestureType = radioGroup.indexOfChild(selectedRadioButton);

        if(gestureType == -1) {
            Toast.makeText(MainActivity.this, "Please select gesture type!", Toast.LENGTH_LONG).show();
            return;
        }

        if(isRunning)
            return;

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CollectTask task = new CollectTask();
                        task.execute(new String[]{""});
                    }
                });
        dlgAlert.setMessage("Please place the phone in the start position in 2 seconds.\n"+
                "When the phone vibrates, start the gesture, and when done, keep still at the start position till the phone vibrates again\n"+
                " YOu have 2 seconds to complete the gesture");
        dlgAlert.setTitle("Instructions");
        dlgAlert.create().show();

        collectBtn.setEnabled(false);
    }

    public void startCollect() {
        int gestureType = -1;
        View selectedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());
        gestureType = radioGroup.indexOfChild(selectedRadioButton);

        isRunning = true;
        gestures[gestureIdx] = new Gesture(gestureType);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        accelManage.registerListener(MainActivity.this, senseAccel, /*accelManage.SENSOR_DELAY_NORMAL*/SensorManager.SENSOR_DELAY_GAME);
        accelManage.registerListener(MainActivity.this, senseGyro, /*accelManage.SENSOR_DELAY_NORMAL*/SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopCollect() {
        accelManage.unregisterListener(this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        isRunning = false;
        collectBtn.setEnabled(true);
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
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gestures[gestureIdx].addAccelSample(sensorEvent.values[0], sensorEvent.values[1],sensorEvent.values[2]);
        } else if(mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gestures[gestureIdx].addGyroSample(sensorEvent.values[0], sensorEvent.values[1],sensorEvent.values[2]);
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
        graphX.removeAllSeries();
        graphY.removeAllSeries();
        graphZ.removeAllSeries();
        graphGyroX.removeAllSeries();
        graphGyroY.removeAllSeries();
        graphGyroZ.removeAllSeries();

        txtViewSampleID.setText("Sample " + (gestureIdx+1));
        if(gestures[gestureIdx] == null) {
            radioGroup.clearCheck();
            chckBoxCop.setChecked(false);
            chckBoxHungry.setChecked(false);
            chckBoxHeadache.setChecked(false);
            chckBoxAbout.setChecked(false);
            return;
        }

        radioGroup.check(radioGroup.getChildAt(gestures[gestureIdx].mgestureType).getId());

        // Build new series for the updated hrValues
        for(int i = 0; i< gestures[gestureIdx].mGyroX.size(); i++) {
            seriesX.appendData(new DataPoint(i, gestures[gestureIdx].mGyroX.get(i)), true, gestures[gestureIdx].mGyroX.size());
            seriesY.appendData(new DataPoint(i, gestures[gestureIdx].mGyroY.get(i)), true, gestures[gestureIdx].mGyroY.size());
            seriesZ.appendData(new DataPoint(i, gestures[gestureIdx].mGyroZ.get(i)), true, gestures[gestureIdx].mGyroZ.size());
        }

        // Update the X axis range
        graphGyroX.getViewport().setMinX(0);
        graphGyroX.getViewport().setMaxX(gestures[gestureIdx].mGyroX.size());
        graphGyroX.getViewport().setMinY(Collections.min(gestures[gestureIdx].mGyroX));
        graphGyroX.getViewport().setMaxY(Collections.max(gestures[gestureIdx].mGyroX));
        // Add the new series to the graph
        graphGyroX.addSeries(seriesX);

        graphGyroY.getViewport().setMinX(0);
        graphGyroY.getViewport().setMaxX(gestures[gestureIdx].mGyroY.size());
        graphGyroY.getViewport().setMinY(Collections.min(gestures[gestureIdx].mGyroY));
        graphGyroY.getViewport().setMaxY(Collections.max(gestures[gestureIdx].mGyroY));
        // Add the new series to the graph
        graphGyroY.addSeries(seriesY);

        graphGyroZ.getViewport().setMinX(0);
        graphGyroZ.getViewport().setMaxX(gestures[gestureIdx].mGyroZ.size());
        graphGyroZ.getViewport().setMinY(Collections.min(gestures[gestureIdx].mGyroZ));
        graphGyroZ.getViewport().setMaxY(Collections.max(gestures[gestureIdx].mGyroZ));
        // Add the new series to the graph
        graphGyroZ.addSeries(seriesZ);

        seriesX = new LineGraphSeries<>();
        seriesY = new LineGraphSeries<>();
        seriesZ = new LineGraphSeries<>();

        // Build new series for the updated hrValues
        for(int i = 0; i< gestures[gestureIdx].mAccelX.size(); i++) {
            seriesX.appendData(new DataPoint(i, gestures[gestureIdx].mAccelX.get(i)), true, gestures[gestureIdx].mAccelX.size());
            seriesY.appendData(new DataPoint(i, gestures[gestureIdx].mAccelY.get(i)), true, gestures[gestureIdx].mAccelY.size());
            seriesZ.appendData(new DataPoint(i, gestures[gestureIdx].mAccelZ.get(i)), true, gestures[gestureIdx].mAccelZ.size());
        }

        // Update the X axis range
        graphX.getViewport().setMinX(0);
        graphX.getViewport().setMaxX(gestures[gestureIdx].mAccelX.size());
        graphX.getViewport().setMinY(Collections.min(gestures[gestureIdx].mAccelX));
        graphX.getViewport().setMaxY(Collections.max(gestures[gestureIdx].mAccelX));
        // Add the new series to the graph
        graphX.addSeries(seriesX);

        graphY.getViewport().setMinX(0);
        graphY.getViewport().setMaxX(gestures[gestureIdx].mAccelY.size());
        graphY.getViewport().setMinY(Collections.min(gestures[gestureIdx].mAccelY));
        graphY.getViewport().setMaxY(Collections.max(gestures[gestureIdx].mAccelY));
        // Add the new series to the graph
        graphY.addSeries(seriesY);

        graphZ.getViewport().setMinX(0);
        graphZ.getViewport().setMaxX(gestures[gestureIdx].mAccelZ.size());
        graphZ.getViewport().setMinY(Collections.min(gestures[gestureIdx].mAccelZ));
        graphZ.getViewport().setMaxY(Collections.max(gestures[gestureIdx].mAccelZ));
        // Add the new series to the graph
        graphZ.addSeries(seriesZ);

        chckBoxCop.setChecked(gestures[gestureIdx].isCop());
        chckBoxHungry.setChecked(gestures[gestureIdx].isHungry());
        chckBoxHeadache.setChecked(gestures[gestureIdx].isHeadache());
        chckBoxAbout.setChecked(gestures[gestureIdx].isAbout());
    }

    private void predict(){
        boolean isPos;
        int selGestureType;
        int total = 0, tp=0, fp=0;

        View selectedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());
        selGestureType = radioGroup.indexOfChild(selectedRadioButton);

        if(selGestureType == -1) {
            Toast.makeText(MainActivity.this, "Please select gesture type!", Toast.LENGTH_LONG).show();
            return;
        }

        for(int i=0; i< NUM_GESTURE_SAMPLES; i++){
            int predictedGesture;
            if(gestures[i] == null)
                continue;
            total++;
            //temporary should be removed
            System.out.println("Size of "+i);
            System.out.println(gestures[i].mAccelX.size());

            switch(selGestureType){
                case Gesture.GESTURE_TYPE_ABOUT:
                    isPos = gestures[i].isAbout();
                    Toast.makeText(MainActivity.this, "Detect About!", Toast.LENGTH_LONG).show();
                    break;
                case Gesture.GESTURE_TYPE_HUNGRY:
                    isPos = gestures[i].isHungry();
                    Toast.makeText(MainActivity.this, "Detect Hungry!", Toast.LENGTH_LONG).show();
                    break;
                case Gesture.GESTURE_TYPE_HEADACHE:
                    isPos = gestures[i].isHeadache();
                    Toast.makeText(MainActivity.this, "Detect Headache!", Toast.LENGTH_LONG).show();
                    break;
                case Gesture.GESTURE_TYPE_COP:
                    isPos = gestures[i].isCop();
                    Toast.makeText(MainActivity.this, "Detect Cop!", Toast.LENGTH_LONG).show();
                    break;
                default :
                    isPos = false;
            }
            if(isPos) {
                if(gestures[i].mgestureType == Gesture.GESTURE_TYPE_ABOUT)
                    tp++;
                else
                    fp++;
            }
        }

        txtViewTruePos.setText("TP: " + String.format("%.2f", tp*100.0/total) + "%");
        txtViewFalsePos.setText("FP: " + String.format("%.2f", fp*100.0/total) + "%");
    }


    private class CollectTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground (String...params) {
            try {
                Thread.sleep(2 * 1000);
                startCollect();
                Thread.sleep(2 * 1000);
                return true;

            } catch (Exception e) {
            }
            return true;
        }
        @Override
        protected void onPostExecute (Boolean param) {
            super.onPostExecute(param);
            stopCollect();
        }
    }
}
