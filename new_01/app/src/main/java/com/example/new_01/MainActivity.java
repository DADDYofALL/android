package com.example.new_01;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
/*import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;*/

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
import static android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE;
import static android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, LocationListener {

    private TextView mText;
    private Button mAdvertiseButton;

    //private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();
    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private Context mContext;
    private int currentCounterValue = 0;
    private HashSet<BluetoothDevice> mBluetoothDevices;

    private static final UUID SERVICE_UUID = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c");
    //private static final UUID CHARACTERISTIC_COUNTER_UUID = UUID.fromString("31517c58-66bf-470c-b662-e352a6c80cba");
    //private static final UUID CHARACTERISTIC_INTERACTOR_UUID = UUID.fromString("0b89d2d4-0ea6-4141-86bb-0c5fb91ab14a");
    //private static final UUID DESCRIPTOR_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_USER_DESCRIPTION_UUID = UUID
            .fromString("00002901-0000-1000-8000-00805f9b34fb");

    //private List<ScanFilter> filters;
    private List<BluetoothDevice> mRegisteredDevices;

    ///////////////////////////////////////////
    ///         Call back Section           ///
    ///////////////////////////////////////////
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        /*
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            Toast.makeText(MainActivity.this,"desc UUID :" + Arrays.toString(value),Toast.LENGTH_LONG).show();
            mText.setText("Notification enabled");
            if (CLIENT_CHARACTERISTIC_CONFIGURATION_UUID.equals(descriptor.getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    mRegisteredDevices.add(device);
                    mText.setText("Notification enabled");
                } else if (Arrays.equals(DISABLE_NOTIFICATION_VALUE, value)) {
                    mRegisteredDevices.remove(device);
                    mText.setText("Notification disabled");
                }

                if (responseNeeded) {
                    mGattServer.sendResponse(device, requestId, GATT_SUCCESS, 0, null);
                }
            }
        }
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            if (DUMMY_UUID.equals(characteristic.getUuid())) {
                //byte value = Integer.valueOf(currentCounterValue).byteValue();
                //byte[] values = new byte[1];
                //values[0] = value;
                mGattServer.sendResponse(device, requestId, GATT_SUCCESS, offset, characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            if (UUID.fromString(getString(R.string.charInt_uuid)).equals(characteristic.getUuid())) {
                currentCounterValue++;
                Toast.makeText(MainActivity.this,"Create service"+currentCounterValue ,Toast.LENGTH_LONG).show();
                //notifyRegisteredDevices();
            }
        }
        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Toast.makeText(MainActivity.this,"onNotiSent" ,Toast.LENGTH_LONG).show();

        }*/
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            //Toast.makeText(MainActivity.this,"Connected " ,Toast.LENGTH_LONG).show();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothDevices.add(device);
                    updateConnectedDevicesStatus();
                    //Log.v(TAG, "Connected to device: " + device.getAddress());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    mBluetoothDevices.remove(device);
                    updateConnectedDevicesStatus();
                    //Log.v(TAG, "Disconnected from device");
                }
            } else {
                mBluetoothDevices.remove(device);
                updateConnectedDevicesStatus();
                // There are too many gatt errors (some of them not even in the documentation) so we just
                // show the error to the user.
                final String errorMessage = getString(R.string.status_errorWhenConnecting) + ": " + status;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
                //Log.e(TAG, "Error when connecting: " + status);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            //Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            //Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
            /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            //Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            //Log.d(TAG, "Device tried to read descriptor: " + descriptor.getUuid());
            //Log.d(TAG, "Value: " + Arrays.toString(descriptor.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
            /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                    offset, value);
            //Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
            int status = BluetoothGatt.GATT_SUCCESS;
            if (descriptor.getUuid() == CLIENT_CHARACTERISTIC_CONFIGURATION_UUID) {
                BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                boolean supportsNotifications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                boolean supportsIndications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;

                if (!(supportsNotifications || supportsIndications)) {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                } else if (value.length != 2) {
                    status = BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
                } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    //mCurrentServiceFragment.notificationsDisabled(characteristic);
                    descriptor.setValue(value);
                } else if (supportsNotifications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    //mCurrentServiceFragment.notificationsEnabled(characteristic, false /* indicate */);
                    descriptor.setValue(value);
                } else if (supportsIndications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    notificationsEnabled(characteristic, true /* indicate */);
                    //sendNotificationToDevices(characteristic);
                    descriptor.setValue(value);
                } else {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                }
            } else {
                status = BluetoothGatt.GATT_SUCCESS;
                //mText.setText("Notification enabled");
                descriptor.setValue(value);
            }
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, status,
            /* No need to respond with offset */ 0,
            /* No need to respond with a value */ null);
            }
        }
    };

    public void notificationsEnabled(final BluetoothGattCharacteristic characteristic, boolean indicate) {
        if (characteristic.getUuid() != DUMMY_UUID) {
            return;
        }
        if (!indicate) {
            return;
        }
        final String message = getString(R.string.status_devicesConnected) + " " + mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).size();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*int newMeasurementInterval = Integer.parseInt(mEditTextMeasurementInterval.getText()
                        .toString());
                if (isValidMeasurementIntervalValue(newMeasurementInterval)) {
                    mMeasurementIntervalCharacteristic.setValue(newMeasurementInterval,
                            MEASUREMENT_INTERVAL_FORMAT, 0);*/

                //set Timer

                mText.setText("Enabled");
                mConnectionStatus.setText(message);


                //}
            }
        });
    }

    private static final int EXPONENT_MASK = 0x7f800000;
    private static final int EXPONENT_SHIFT = 23;
    private static final int MANTISSA_MASK = 0x007fffff;
    private static final int MANTISSA_SHIFT = 0;

    public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
        //boolean indicate = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
        //characteristic.setValue(new byte[]{0b00000000, 0, 0, 0, 0});
        // Characteristic Value: [press,orient,rotate]

        int bits = Float.floatToIntBits(pressVal);
        int exponent = (bits & EXPONENT_MASK) >>> EXPONENT_SHIFT;
        int mantissa = (bits & MANTISSA_MASK) >>> MANTISSA_SHIFT;
        byte[] p = ByteBuffer.allocate(4).putFloat(pressVal).array();
        byte[] o = ByteBuffer.allocate(4).putFloat(orientVal).array();
        byte[] r = ByteBuffer.allocate(4).putFloat(rotateVal).array();
        byte[] lat = ByteBuffer.allocate(4).putFloat((float) currentLocation.getLatitude()).array();
        byte[] lon  = ByteBuffer.allocate(4).putFloat((float) currentLocation.getLongitude()).array();
        byte[] out = new byte[20];
        System.arraycopy(p, 0, out, 0, 4);
        System.arraycopy(o, 0, out, 4, 4);
        System.arraycopy(r, 0, out, 8, 4);
        System.arraycopy(lat, 0, out, 12, 4);
        System.arraycopy(lon, 0, out, 16, 4);

        characteristic.setValue(out);
        //characteristic.setValue(mantissa, exponent, BluetoothGattCharacteristic.FORMAT_FLOAT, /* offset */ 1);
        if (mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).size() < 1)
            return;
        for (BluetoothDevice device : mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT)) {
            mGattServer.notifyCharacteristicChanged(device, characteristic, true);
        }
        //pressVal += 1;
        /*for (BluetoothDevice device : mBluetoothDevices) {
            // true for indication (acknowledge) and false for notification (unacknowledge).
            mGattServer.notifyCharacteristicChanged(device, characteristic, true);
        }*/
        //mGattServer.notifyCharacteristicChanged(mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).get(0),characteristic,true);
    }


    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Toast.makeText(MainActivity.this, "Adv Success ", Toast.LENGTH_LONG).show();
            super.onStartSuccess(settingsInEffect);
            openServer();
        }

        @Override
        public void onStartFailure(int errorCode) {
            //Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
            Toast.makeText(MainActivity.this, "Fail ", Toast.LENGTH_LONG).show();
            super.onStartFailure(errorCode);
        }
    };

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void discover() {
        Toast.makeText(MainActivity.this, "Discovering", Toast.LENGTH_LONG).show();
       /* ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid( new ParcelUuid(UUID.fromString( getString(R.string.ble_uuid ) ) ) )
                .build();
        filters.add( filter );
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }, 10000);*/
    }

    public void advertise() {
        Toast.makeText(MainActivity.this, "Advertising", Toast.LENGTH_LONG).show();
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        //BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setTimeout(0)
                .build();

        //ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString(R.string.ble_uuid) ) );
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();


        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        mBluetoothLeAdvertiser.startAdvertising(settings, data, advertisingCallback);

    }

    private BluetoothGattCharacteristic Dummy;
    private static final UUID DUMMY_UUID = UUID.fromString("00002A21-0000-1000-8000-00805f9b34fb");

    private BluetoothGattService createService() {

        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, SERVICE_TYPE_PRIMARY);

        //Dummy test
        Dummy = new BluetoothGattCharacteristic(DUMMY_UUID, BluetoothGattCharacteristic.PROPERTY_INDICATE, 0);
        //BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(DESCRIPTOR_CONFIG_UUID,(BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        //descriptor.setValue(new byte[]{0, 0});
        Dummy.setValue(new byte[]{0, 0b00000001});
        // Counter characteristic (read-only, supports subscriptions)
        //BluetoothGattCharacteristic counter = new BluetoothGattCharacteristic(CHARACTERISTIC_COUNTER_UUID, PROPERTY_READ | PROPERTY_NOTIFY, PERMISSION_READ);
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID, PERMISSION_READ | PERMISSION_WRITE);
        BluetoothGattDescriptor descriptor2 = new BluetoothGattDescriptor(CHARACTERISTIC_USER_DESCRIPTION_UUID, PERMISSION_READ | PERMISSION_WRITE);

        descriptor.setValue(new byte[]{0, 0});
        try {
            descriptor2.setValue("XXX".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*try {
            descriptor.setValue("DUMMY".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        Dummy.addDescriptor(descriptor);
        Dummy.addDescriptor(descriptor2);

        // Interactor characteristic
        //BluetoothGattCharacteristic interact = new BluetoothGattCharacteristic(CHARACTERISTIC_INTERACTOR_UUID, PROPERTY_WRITE_NO_RESPONSE, PERMISSION_WRITE);

        //service.addCharacteristic(counter);
        service.addCharacteristic(Dummy);
        Toast.makeText(MainActivity.this, "Created service", Toast.LENGTH_LONG).show();
        setTimer();
        return service;
    }

    private void openServer() {
        mGattServer = mBluetoothManager.openGattServer(MainActivity.this, mGattServerCallback);
        if (mGattServer == null) {
            Toast.makeText(MainActivity.this, "Open server fail", Toast.LENGTH_LONG).show();
            return;
        } else
            Toast.makeText(MainActivity.this, "Open server success", Toast.LENGTH_LONG).show();
        mGattServer.addService(createService());
    }

    /*private void notifyRegisteredDevices() {
        BluetoothGattCharacteristic characteristic = mGattServer
                .getService(SERVICE_UUID)
                .getCharacteristic(CHARACTERISTIC_COUNTER_UUID);

        for (BluetoothDevice device : mRegisteredDevices) {
            byte[] value = Ints.toByteArray(currentCounterValue);
            characteristic.setValue(value);
            mGattServer.notifyCharacteristicChanged(device, characteristic, false);
        }
    }*/

    ////////////////////////////////////////////////////////////////////////////////////////
    ///////////////               Sensor and UI                       //////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    private SensorManager mSensorManager;
    private Sensor mRotationSensor;
    private Sensor mPressureSensor;
    private Sensor mOrientatSensor;
    private TextView pressView;
    private TextView rotateView;
    private TextView orientView;
    private TextView mConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pressView = (TextView) findViewById(R.id.pressView);
        rotateView = (TextView) findViewById(R.id.rotateView);
        orientView = (TextView) findViewById(R.id.orientView);
        mConnectionStatus = (TextView) findViewById(R.id.conStatus);
        mText = (TextView) findViewById(R.id.textView);
        mAdvertiseButton = (Button) findViewById(R.id.advBtn);

        mAdvertiseButton.setOnClickListener(this);

        //mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mText.setText("Notification disabled");
        mConnectionStatus.setText("XXXX");
        readSensor();

        getLocation();
    }

    private Timer mTimer;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.advBtn) {
            advertise();
        }
    }

    public void setTimer() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendNotificationToDevices(Dummy);

            }
        }, 0, 1000);
    }

    public void readSensor() {
        try {
            mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            mOrientatSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            mSensorManager.registerListener(this, mPressureSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mOrientatSensor, SensorManager.SENSOR_DELAY_UI);
        } catch (Exception e) {
            Toast.makeText(this, "Hardware compatibility issue", Toast.LENGTH_LONG).show();
        }
    }

    private LocationManager locationManager;

    public void getLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000,//minTime
                    10, (LocationListener) this);//minDistance
            //currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            /*String msg = "New Latitude: " + currentLocation.getLatitude()
                    + "New Longitude: " + currentLocation.getLongitude();
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();*/
        }catch (Exception e) {
            Toast.makeText(MainActivity.this,"request location error",Toast.LENGTH_LONG).show();
        }
    }

    private float pressVal;
    private float orientVal;
    private float rotateVal;
    private int num =0;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == mRotationSensor) {
            //if (sensorEvent.values.length > 4) {
                //float[] truncatedRotationVector = new float[4];
                //System.arraycopy(sensorEvent.values, 0, truncatedRotationVector, 0, 4);
                //rotateView.setText("Rotate sensor: "+sensorEvent.values[0]);
                //rotateVal = sensorEvent.values[0];
                //truncatedRotationVector = sensorEvent.values;
                //update(truncatedRotationVector);
            //} else {
            //    rotateView.setText("Rotate sensor: "+sensorEvent.values);
                //update(sensorEvent.values);
            //}
        }
        else if(sensorEvent.sensor == mPressureSensor){
            float[] values = sensorEvent.values;
            pressView.setText("Pressure sensor: " + values[0]);
            pressVal = values[0];

            /*if(num==0){
                //pressure = (double)values[0];
                pressVal = values[0];
                num=1;
                //Log.v("Pressure", pressure.toString());
            }*/
        }
        else if (sensorEvent.sensor == mOrientatSensor){
            float degreeZ = Math.round(sensorEvent.values[0]);
            float degreeX = Math.round(sensorEvent.values[1]);
            orientVal = degreeZ;
            rotateVal = degreeX;
            //degree = calibrateDegree(degree);
            //realHeading = degree;
            //((MyApplication) this.getApplication()).setCurrentHeading(realHeading);
            orientView.setText("Orient sensor: " + Float.toString(degreeZ) + " degrees");
            rotateView.setText("Rotate sensor: "+sensorEvent.values[1]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateConnectedDevicesStatus() {
        final String message = getString(R.string.status_devicesConnected) + " " + mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).size();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionStatus.setText(message);
            }
        });
    }

    private void setEnabled(){
        mText.setText("Notification enabled");
    }

    private void setDisabled(){
        mText.setText("Notification disabled");
    }

    ///////// GPS/////////////////////////////////////////////////////////////////////////
    private Location currentLocation;

    @Override
    public void onLocationChanged(Location location) {
        String msg = "New Latitude: " + location.getLatitude()
                + "New Longitude: " + location.getLongitude();
        currentLocation = location;
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }

}