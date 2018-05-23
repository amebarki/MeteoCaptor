package com.example.iem.meteocaptorandroidthings;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private WifiManager wifiManager;

    private static final String SERVICE_ID = "UNIQUE_SERVICE_ID";
    private static final String TAG = MyCameraManager.class.getSimpleName();
    private ConnectionLifecycleCallback mConnectionLifecycleCallback;
    private PayloadCallback mPayloadCallback;
    private String endpoint;
    private MyCameraManager mCamera;
    private GoogleApiClient mGoogleApiClient;

    /**
     * A {@link Handler} for running Camera tasks in the background.
     */
    private Handler mCameraHandler;

    /**
     * An additional thread for running Camera tasks that shouldn't block the UI.
     */
    private HandlerThread mCameraThread;

    private ImageView iv;


    private Si7021SensorDriver mEnvironmentalSensorDriver;
    private SensorManager mSensorManager;


    private float mLastTemperature;
    private float mLastHumidity;


    // Callback used when we register the Si7021 sensor driver with the system's SensorManager.
    private SensorManager.DynamicSensorCallback mDynamicSensorCallback = new SensorManager.DynamicSensorCallback() {

        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
            if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                // Our sensor is connected. Start receiving temperature data.
                mSensorManager.registerListener(mTemperatureListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG, "Test 1");

            } else if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                // Our sensor is connected. Start receiving pressure data.
                mSensorManager.registerListener(mHumidityListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG, "Test 2");
            }
        }

        @Override
        public void onDynamicSensorDisconnected(Sensor sensor) {
            super.onDynamicSensorDisconnected(sensor);
            Log.d(TAG, "Sensor disco: "+ sensor);
        }
    };

    // Callback when SensorManager delivers temperature data.
    private SensorEventListener mTemperatureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLastTemperature = event.values[0];
            Log.d(TAG, "sensor changed: " + mLastTemperature);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "accuracy changed: " + accuracy);
        }
    };

    // Callback when SensorManager delivers humidity data.
    private SensorEventListener mHumidityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLastHumidity = event.values[0];
            Log.d(TAG, "sensor changed: " + mLastHumidity);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "accuracy changed: " + accuracy);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this, this, this)
                .addApi(Nearby.CONNECTIONS_API)
                .enableAutoManage(this,this)
                .build();


        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> deviceList = manager.getI2cBusList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No I2C bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }


        // Creates new handlers and associated threads for camera and networking operations.
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        // Camera code is complicated, so we've shoved it all in this closet class for you.
        mCamera = MyCameraManager.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
        iv =(ImageView) findViewById(R.id.imageView3);
        Button button = (Button)findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture();
                //mCamera.shutDown();

                Log.e("SPAGHETT", "SOMEBODY TOUCHA MY SPAGHETT !!!");
            }
        });
        // We need permission to access the camera
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.e(TAG, "No Camera permission");
            return;
        }

        Log.d(TAG, "Started Weather Station");

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        try {
            mEnvironmentalSensorDriver = new Si7021SensorDriver("I2C1");
            mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);
            mEnvironmentalSensorDriver.registerTemperatureSensor();
            mEnvironmentalSensorDriver.registerHumiditySensor();

            Log.d(TAG, "Initialized I2C SI7021");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing SI7021", e);
        }


    }

    public void nearbyConnection(){
        mPayloadCallback = new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpoint, Payload payload) {
                Log.e("Tuts+", new String(payload.asBytes()));
            }

            @Override
            public void onPayloadTransferUpdate(String endpoint, PayloadTransferUpdate payloadTransferUpdate) {}
        };
        mConnectionLifecycleCallback =
                new ConnectionLifecycleCallback() {
                    @Override
                    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                        endpoint = endpointId;

                        Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, mPayloadCallback)
                                .setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull com.google.android.gms.common.api.Status status) {
                                        if( status.isSuccess() ) {
                                            //Connection accepted
                                        }
                                    }
                                });

                        Nearby.Connections.stopAdvertising(mGoogleApiClient);
                    }

                    @Override
                    public void onConnectionResult(String endpointId, ConnectionResolution result) {
                        Log.e("SPAGHETT", endpointId);
                        Log.e("SPAGHETT", result.getStatus().getStatusMessage());

                    }

                    @Override
                    public void onDisconnected(String endpointId) {}
                };
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();
        mCameraThread.quitSafely();
        Log.d(TAG, "Destroying");
        // Clean up sensor registrations
        mSensorManager.unregisterListener(mTemperatureListener);
        mSensorManager.unregisterListener(mHumidityListener);
        mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);

        // Clean up peripheral.
        if (mEnvironmentalSensorDriver != null) {
            try {
                mEnvironmentalSensorDriver.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mEnvironmentalSensorDriver = null;
        }
    }

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {

                    Image image = reader.acquireLatestImage();

                    FileOutputStream fos = null;
                    File file;
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    try {
                        //Specify the file path here
                        file = new File(getFilesDir() + String.valueOf(mLastTemperature) + "_" + String.valueOf(mLastHumidity));
                        fos = new FileOutputStream(file);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        fos.write(bytes);
                        fos.flush();
                        System.out.println("File Written Successfully");
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    finally {
                        try {
                            if (fos != null)
                            {
                                fos.close();
                            }
                        }
                        catch (IOException ioe) {
                            System.out.println("Error in closing the Stream");
                        }
                    }
                        Uri uri = Uri.fromFile(new File(getFilesDir() +String.valueOf(mLastTemperature) + "_" + String.valueOf(mLastHumidity)));
                        ParcelFileDescriptor pfd = null;
                        try {
                            pfd = getContentResolver().openFileDescriptor(uri, "r");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Payload filePayload = Payload.fromFile(pfd);
                        Log.d("SPAGHETT", String.valueOf(filePayload.getId()));

                        // Construct a simple message mapping the ID of the file payload to the desired filename.
                        String payloadFilenameMessage = filePayload.getId() + ":" + uri.getLastPathSegment();

                        // Send this message as a bytes payload.
                        try {
                            Nearby.Connections.sendPayload(mGoogleApiClient,
                                    endpoint, Payload.fromBytes(payloadFilenameMessage.getBytes("UTF-8")));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        // Finally, send the file payload.
                        Nearby.Connections.sendPayload(mGoogleApiClient, endpoint, filePayload);

                        // PNG is a lossless format, the compression factor (100) is ignored

                        image.close();
                       // final Bitmap finalBitmap = bitmap;
                            final Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                iv.setImageBitmap(bitmapImage);
                                //Stuff that updates the UI

                            }
                        });


                    }

            };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        nearbyConnection();
        Log.e("SPAGHETT", "onConnected");
        Nearby.Connections.startAdvertising(
                mGoogleApiClient,
                "AsusROG",
                SERVICE_ID,
                mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_STAR));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    }
