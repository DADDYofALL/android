package com.example.centralside;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class dashboard extends AppCompatActivity implements View.OnClickListener{

    private BluetoothDevice mBluetoothDevice;
    private BluetoothGattCharacteristic mGattChar;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<ScanFilter> filters = new ArrayList<>();
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mGatt;

    private static final UUID SERVICE_UUID = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID DUMMY_UUID = UUID.fromString("00002A21-0000-1000-8000-00805f9b34fb");

/////////////////////////////////////   Callback    /////////////////////////////////////

    private BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //.onConnectionStateChange(gatt, status, newState);
            //String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //intentAction = ACTION_GATT_CONNECTED;
                //mConnectionState = STATE_CONNECTED;
                //broadcastUpdate(intentAction);
                //Log.i(TAG, "Connected to GATT server.");
                //Log.i(TAG, "Attempting to start service discovery:" +
                //        mBluetoothGatt.discoverServices());
                //xText.setText("Connected");
                text1 = "Connected";
                setTxt();
                gatt.discoverServices();
                //Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_LONG).show();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //intentAction = ACTION_GATT_DISCONNECTED;
                //mConnectionState = STATE_DISCONNECTED;
                //Log.i(TAG, "Disconnected from GATT server.");
                //broadcastUpdate(intentAction);
                Toast.makeText(dashboard.this, "Disconnected", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Toast.makeText(MainActivity.this,"Discovered",Toast.LENGTH_LONG).show();
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                mGattChar = gatt.getService(SERVICE_UUID).getCharacteristic(DUMMY_UUID);
                gatt.setCharacteristicNotification(mGattChar, true);
                BluetoothGattDescriptor descriptor = mGattChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                //mGatt.writeDescriptor(descriptor);
                //pressText.setText("Discovered");
                text1 = "Discovered and subscripted";
                setTxt();
            } else {
                Toast.makeText(dashboard.this, "discoverError", Toast.LENGTH_LONG).show();
                //Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //super.onCharacteristicChanged(gatt, characteristic);
            //zText.setText("Changed");
            //Z = Float.toString(ByteBuffer.wrap(characteristic.getValue()).getFloat(4));
            //Press = Float.toString(ByteBuffer.wrap(characteristic.getValue()).getFloat(0));
            //X = Float.toString(ByteBuffer.wrap(characteristic.getValue()).getFloat(8));
            try {
                currentPress = ByteBuffer.wrap(characteristic.getValue()).getFloat(0);
                currentX = ByteBuffer.wrap(characteristic.getValue()).getFloat(8);
                currentZ = ByteBuffer.wrap(characteristic.getValue()).getFloat(4);
                currentLat = ByteBuffer.wrap(characteristic.getValue()).getFloat(12);
                currentLon = ByteBuffer.wrap(characteristic.getValue()).getFloat(16);
                loopForShow();
            }catch (Exception e) {
                //text1 = "error - " + e;
                text2 = "length = "+characteristic.getValue().length;
                setTxt();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //text1 = "Lat :"+currentLat+" Lon :"+currentLon;
                    //textView1.setText(text1);
                    showCompassAnimation();
                }
            });

            //X = "Notified";
            /*text1 = "North Calibrated";
            if(calibrate==0)
                text1 = "connected";
            if(currentZ-calibrate>=0)
                text2 = "Expect Horizontal :"+Hangle+" Current Horizontal :"+(currentZ-calibrate);//Horizontal
            else
                text2 = "Expect Horizontal :"+Hangle+" Current Horizontal :"+(currentZ-calibrate+360);
            text3 = "Expect Vertical :"+Vangle+" Current Vertical :"+(currentX);//Vertical
            setTxt();*/
            //Toast.makeText(dashboard.this, "Char Change", Toast.LENGTH_SHORT).show();
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //super.onScanResult(callbackType, result);
            if (result == null
                    || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName()))
                return;
            try {
                mBluetoothDevice = result.getDevice();
                connect();
                //xText.setText(mBluetoothDevice.getName());
                text1 = mBluetoothDevice.getName();
                setTxt();
            } catch (Exception e) {
                Toast.makeText(dashboard.this, "ScanCallbackError", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //super.onBatchScanResults(results);
        }
    };

/////////////////////////////////////   Helper Method   /////////////////////////////////////

    public void connect() {
        try {
            mGatt = mBluetoothDevice.connectGatt(this, false, mGattCallBack);
            //mGatt = mBluetoothManager.getConnectedDevices(0).get(0).connectGatt(result.getDevice() ,true, mGattCallBack);
            //mGatt = result.getDevice().connectGatt(this,true,mGattCallBack);
            //mGatt.connect();
            //pressText.setText("....");
            //zText.setText(mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT_SERVER).size());
            //mGatt.discoverServices();
        } catch (Exception e) {
            Toast.makeText(dashboard.this, "connectError", Toast.LENGTH_LONG).show();
        }
    }

    public void setTxt() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //textView1.setText(text1);
                textView2.setText(text2);
                textView3.setText(text3);
            }
        });
    }

    /*public void subscript(){
        try {
            //mGatt.setCharacteristicNotification(mGatt.getService(SERVICE_UUID).getCharacteristics().get(0), true);
            mGatt.setCharacteristicNotification(mGatt.getService(SERVICE_UUID).getCharacteristic(DUMMY_UUID),true);
            //BluetoothGattDescriptor descriptor = mGatt.getService(SERVICE_UUID).getCharacteristics().get(0).getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
            BluetoothGattDescriptor descriptor = mGatt.getService(SERVICE_UUID).getCharacteristic(DUMMY_UUID).getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor);
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this,"subscriptError",Toast.LENGTH_LONG).show();
        }
    }*/

    public boolean startScan() {
        try {
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                    .build();
            filters.add(scanFilter);
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
            }, 1000);
        } catch (Exception e) {
            Toast.makeText(this, "ScanError", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /////////////////////////////////////   UI SECTION  /////////////////////////////////////

    private Handler mHandler = new Handler();

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private String text1;
    private String text2;
    private String text3;
    private WebView webView;
    private Button debugBtn;
    private ImageView imageH;
    private ImageView imageV;
    private EditText editText;
    private float tempDeg = 0;
    private float tempV = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        webView = (WebView) findViewById(R.id.webView);
        imageH = (ImageView) findViewById(R.id.imageView);
        imageV = (ImageView) findViewById(R.id.imageArrow);
        debugBtn = (Button) findViewById(R.id.debugBtn);
        editText = (EditText) findViewById(R.id.editText);

        debugBtn.setOnClickListener(this);
        //textView1.setVisibility(View.INVISIBLE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        //webView.loadUrl("http://192.168.1.20/login.cgi");
        //webView.loadUrl("https://www.google.co.th");

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }
        });

        Intent intent = getIntent();
        destLat = intent.getDoubleExtra("destLat",0);
        destLong = intent.getDoubleExtra("destLon",0);
        destPress = intent.getDoubleExtra("destPress",0);

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        try {
            text1 = "destLat : " + destLat;
            text2 = "destLon : " + destLong;
            text3 = "destPress : " + destPress;
            setTxt();
        }catch (Exception e){
            Toast.makeText(dashboard.this,"error - "+e,Toast.LENGTH_LONG).show();
        }
        startScan();


    }

    @Override
    public void onClick(View view) {
        //subscript();
            //distance = meterDistanceBetweenPoints(destLat,destLong,currentLocation.getLatitude(),currentLocation.getLongitude());
        /*if(calibrate==-1000)
            calibrate = currentZ;*/
        /*new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loopForShow();
            }
        }, 0, 1000);*/
        //loopForShow();
        //editText.getText().toString();
        String u = "http://"+editText.getText().toString();
        webView.loadUrl(u);
    }

    public void loopForShow(){
        distance = meterDistanceBetweenPoints(destLat,destLong,currentLat,currentLon);
        height = Double.parseDouble(String.format("%.2f", altpress(currentPress, destPress)));
        Vangle = Math.toDegrees(Math.atan2(height, distance));
        calculateHAngle();

        //if(currentZ-calibrate>=0)
        text2 = "Expect angle :"+String.format("%.2f",Hangle)+"°\nCurrent angle :"+(currentZ)+"°";//Horizontal
        //else
        //    text2 = "Expect Horizontal :"+String.format("%.2f",Hangle)+" Current Horizontal :"+(currentZ-calibrate+360);
        text3 = "Expect angle :"+String.format("%.2f",Vangle)+"°\nCurrent angle :"+(-currentX)+"°";//Vertical
        setTxt();
        //showCompassAnimation();
    }



    public void showCompassAnimation()
    {
        // create a rotation animation (reverse turn degree degrees)
        final RotateAnimation ra = new RotateAnimation(tempDeg,(float) currentZ, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // how long the animation will take place
        ra.setDuration(300);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        // Start the animation
        final RotateAnimation rb = new RotateAnimation(tempV,(float) -currentX, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // how long the animation will take place
        rb.setDuration(300);
        // set the animation after the end of the reservation status
        rb.setFillAfter(true);
        // Start the animation
        imageH.startAnimation(ra);
        imageV.startAnimation(rb);

        tempDeg = (float) currentZ;
        tempV = (float) -currentX;
    }

    ////////////////////////////// Calculation ///////////////////////////////////////
    private double destPress;
    private double destLat;
    private double destLong;
    private double distance;
    private double height;
    private double currentX,currentZ,currentPress,currentLat,currentLon;
    private double Hangle=0;
    private double calibrate=-1000;
    private double Vangle=0;

    public double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = 180.0/Math.PI;
        double a1 = Math.toRadians(lat_a);
        double b1 = Math.toRadians(lat_b);
        double dq= (lat_b-lat_a) /pk;
        double dl = (lng_b-lng_a) / pk;
        double a = Math.sin(dq / 2)* Math.sin(dq / 2)+Math.cos(a1)*Math.cos(b1)*Math.sin(dl / 2)*Math.sin(dl / 2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        return 6371e3*c;  // m
    }

    private double altpress(double pressure, double pressureDes){
        double feetTometer = 0.3048;
        double pstd = 1013.25;
        double altpress;
        double altpressDes;

        altpress =  (1 - Math.pow((pressure/pstd), 0.190284)) * 145366.45*feetTometer;
        altpressDes =  (1 - Math.pow((pressureDes/pstd), 0.190284)) * 145366.45*feetTometer;
        return Math.abs(altpress-altpressDes);
    }

    public void calculateHAngle()
    {
        //double[] positionA = {currentLocation.getLatitude(),currentLocation.getLongitude()};
        double[] positionA = {currentLat,currentLon};
        double[] positionB = {destLat,destLong};
        double theta = calculateTheta(positionA, positionB);
        Hangle = calculateDegree(positionA,positionB,theta);
        /*angle = calDegree(positionA[0],positionB[0],positionA[1],positionB[1],theta);
        if(angle >= 0) {
            ((TextView) findViewById(R.id.angleSuggest)).setText("Rotate the Antenna " + String.format("%.1f", angle)+"°\nclockwise");
        }
        else
            ((TextView) findViewById(R.id.angleSuggest)).setText("Rotate the Antenna " + String.format("%.1f", angle)+"°\ncounter-clockwise");*/

    }

    public double calculateTheta(double[] positionA,double[] positionB)
    {
        double difY = positionB[0] - positionA[0];
        double difX = positionB[1] - positionA[1];
        double rotAng = Math.toDegrees(Math.atan2(difY, difX));
        System.out.println(rotAng);
        return rotAng;
    }

    public double calculateDegree(double[] positionA,double[] positionB,double theta)
    {
        double a=0;
        if(positionA[0] > positionB[0] && positionA[1] > positionB[1])
        {
            //System.out.println("a right top, b left bot"); Q3
            //a = -(90 + theta);
            a = 360+theta;
        }
        else if(positionA[0] > positionB[0] && positionA[1] < positionB[1])
        {	//System.out.println("a left top, b right bot"); Q4
            a = (90 - theta);
        }
        else if(positionA[0] < positionB[0] && positionA[1] > positionB[1])
        {
            //System.out.println("a right bot, b left top"); Q2
            //a = -(90 - theta);
            a = 360 - (theta-90);
        }
        else if(positionA[0] < positionB[0] && positionA[1] < positionB[1])
        {	//System.out.println("a left bot, b right top"); Q1
            a = (90 - theta);
        }
        return a%360;
    }
}
