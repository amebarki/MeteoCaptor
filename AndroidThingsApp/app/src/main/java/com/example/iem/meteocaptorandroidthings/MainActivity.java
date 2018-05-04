package com.example.iem.meteocaptorandroidthings;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.IOException;
import java.nio.ByteBuffer;
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
    }

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    File imageFile = new File(image);
                    // get image bytes
               //     ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                   // byte[] bytes = new byte[buffer.capacity()];
                  //  Nearby.Connections.sendPayload(mGoogleApiClient, endpoint, Payload.fromFile
                   // buffer.get(bytes);
                  //  final Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                    image.close();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                       //     iv.setImageBitmap(bitmapImage);
                            // Stuff that updates the UI

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




      /*  private static final String I2C_ADDRESS = "I2C1";
        private static final int BMP280_TEMPERATURE_SENSOR_SLAVE = 0x3C;

        private static final int REGISTER_TEMPERATURE_RAW_VALUE_START = 0xFA;
        private static final int REGISTER_TEMPERATURE_RAW_VALUE_SIZE = 3;

        private final short[] calibrationData = new short[3];

        private I2cDevice bus;
        private Handler handler;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PeripheralManager service = PeripheralManager.getInstance();
            try {
                bus = service.openI2cDevice(I2C_ADDRESS, BMP280_TEMPERATURE_SENSOR_SLAVE);
            } catch (IOException e) {
                throw new IllegalStateException(I2C_ADDRESS + " bus slave "
                        + BMP280_TEMPERATURE_SENSOR_SLAVE + " connection cannot be opened.", e);
            }

            try {
                calibrationData[0] = bus.readRegWord(BMP280_TEMPERATURE_SENSOR_SLAVE);
                calibrationData[1] = bus.readRegWord(BMP280_TEMPERATURE_SENSOR_SLAVE);
                calibrationData[2] = bus.readRegWord(BMP280_TEMPERATURE_SENSOR_SLAVE);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read calibration data, can't read temperature without it.", e);
            }

            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        protected void onStart() {
            super.onStart();
            handler.post(readTemperature);
        }

        private final Runnable readTemperature = new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[REGISTER_TEMPERATURE_RAW_VALUE_SIZE];
                try {
                    bus.readRegBuffer(REGISTER_TEMPERATURE_RAW_VALUE_START, data, REGISTER_TEMPERATURE_RAW_VALUE_SIZE);
                } catch (IOException e) {
                    Log.e("TUT", "Cannot read temperature from bus.", e);
                }
                if (data.length != 0) {
                    float temperature = Bmp280DataSheet.readTemperatureFrom(data, calibrationData);
                    Log.d("TUT", "Got temperature of: " + temperature);
                }

                handler.postDelayed(readTemperature, TimeUnit.HOURS.toMillis(1));
            }
        };

        @Override
        protected void onStop() {
            handler.removeCallbacks(readTemperature);
            super.onStop();
        }

        @Override
        protected void onDestroy() {
            try {
                bus.close();
            } catch (IOException e) {
                Log.e("TUT", I2C_ADDRESS + " bus slave "
                        + BMP280_TEMPERATURE_SENSOR_SLAVE + "connection cannot be closed, you may experience errors on next launch.", e);
            }
            super.onDestroy();
        }*/

    }
