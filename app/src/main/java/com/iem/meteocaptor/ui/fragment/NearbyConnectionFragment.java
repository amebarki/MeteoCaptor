package com.iem.meteocaptor.ui.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.iem.meteocaptor.R;
import com.iem.meteocaptor.data.manager.WeatherManager;
import com.iem.meteocaptor.data.model.WeatherModel;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Date;

/**
 * Created by iem on 30/04/2018.
 */

public class NearbyConnectionFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String SERVICE_ID = "UNIQUE_SERVICE_ID";
    private static final String TAG = "TAGO";
    private GoogleApiClient weatherGoogleApiClient;
    private PayloadCallback weatherPayloadCallback;
    private ConnectionLifecycleCallback weatherConnectionLifecycleCallback;
    private EndpointDiscoveryCallback weatherEndPointDiscoveryCallback;
    private String endPoint ="";
    private double temp=0.0;
    private double humid = 0.0;

    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();


    private WeatherManager weatherManager = WeatherManager.getInstance();
    private Context context;
    private ImageView screenshot;
    private TextView dateTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;


    public static NearbyConnectionFragment newInstance() {
        Bundle args = new Bundle();
        NearbyConnectionFragment fragment = new NearbyConnectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
        if(endPoint != "")
            Nearby.Connections.disconnectFromEndpoint(weatherGoogleApiClient,
                endPoint);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weatherGoogleApiClient = new GoogleApiClient
                .Builder(context, this, this)
                .addApi(Nearby.CONNECTIONS_API)
                .enableAutoManage(getActivity(), this)
                .build();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View v = inflater.inflate(R.layout.fragment_last_measure, container, false);
        screenshot = v.findViewById(R.id.fragment_last_measure_screenshot);
        screenshot.setImageResource(R.drawable.splash_screen);
        dateTextView = v.findViewById(R.id.fragment_last_measure_textview_date);
        temperatureTextView = v.findViewById(R.id.fragment_last_measure_textview_temperature);
        humidityTextView = v.findViewById(R.id.fragment_last_measure_textview_humidity);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Nearby Connection Fragment");


    }





    private void initCallBackNearbyConnection()
    {

        Log.d(TAG,"initCallBackNearbyConnection");

      weatherPayloadCallback  = new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpoint, Payload payload) {

                Log.d("TAGO","onPayloadReceived : " + endpoint);
                if (payload.getType() == Payload.Type.BYTES) {
                    String payloadFilenameMessage = null;
                    try {
                        payloadFilenameMessage = new String(payload.asBytes(), "UTF-8");
                        Log.d("TAGO", "onPayloadReceived : " + payloadFilenameMessage);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    addPayloadFilename(payloadFilenameMessage);
                } else if (payload.getType() == Payload.Type.FILE) {
                    // Add this to our tracking map, so that we can retrieve the payload later.
                    Log.d("TAGO", "onPayloadReceived : FILE");
                    Log.d("TAGO", "onPayloadReceived : FILE : " + payload.getId());
                    incomingPayloads.put(payload.getId(), payload);
                }
            }
            /*    byte[] imageBytes = payload.asBytes();
                final Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
                imageView.setImageBitmap(bitmapImage    );
                Log.e(TAG,"Reception : " + new String(payload.asBytes()));

                Toast.makeText(context, "It Works !!!", Toast.LENGTH_SHORT).show();
            }*/

            @Override
            public void onPayloadTransferUpdate(String endpoint, PayloadTransferUpdate update) {
                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    Log.d("TAGO", "SUCCESS");
                    Log.d("TAGO", "endpoint : " + endpoint);
                    Log.d("TAGO", "Size incomingPayloads : " + incomingPayloads.size()+"");
                    Log.d("TAGO", "payload ID : " + update.getPayloadId() + "");
                    Payload payload = incomingPayloads.get(update.getPayloadId());
                    if(payload != null){
                        if (payload.getType() == Payload.Type.FILE) {
                            // Retrieve the filename that was received in a bytes payload.
                            String newFilename = filePayloadFilenames.get(update.getPayloadId());
                            Log.d("TAGO", "name of the file : " + newFilename);
                            File payloadFile = payload.asFile().asJavaFile();
                            // Rename the file.
                            payloadFile.renameTo(new File(payloadFile.getParentFile(), newFilename));
                            Log.d("TAGO", "Transfert update : " + payloadFile.getParentFile().getAbsolutePath());
                            Log.d("TAGO", "Transfert update : " + payloadFile.getName());
                            Log.d("TAGO", "Transfert update : " + payloadFile.getAbsolutePath());
                            File file = new File(payloadFile.getParentFile().getAbsolutePath(),newFilename);
                            try {
                             //   byte[] array = Files.readAllBytes(file.toPath());
                                //ByteArrayOutputStream blob = new ByteArrayOutputStream();
                                byte[] bitmapdata = readBytesFromFile(file);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                                WeatherModel model = new WeatherModel(new Date(),temp,humid,bitmap);
                                WeatherManager.getInstance().setLastMeasure(model);
                                initLastTemperature();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }else
                    {
                        Log.d("TAGO","PAYLOAD IS NULL");
                    }

                }

            }
        };

     weatherConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                Log.d(TAG,"ConnectionLifecycle : onConnectioninitiated");
                Nearby.Connections.acceptConnection(weatherGoogleApiClient,endpointId,weatherPayloadCallback);
                endPoint  = endpointId;
                Log.d(TAG,"ConnectionLifecycle endPoint : " + endpointId);
                Nearby.Connections.stopDiscovery(weatherGoogleApiClient);
            }

            @Override
            public void onConnectionResult(String s, ConnectionResolution connectionResolution) {
                Log.d(TAG,"ConnectionLifecycle : onConnectionResult");

            }

            @Override
            public void onDisconnected(String s) {
                Log.d(TAG,"ConnectionLifecycle : onDisconnected");

            }
        };

       weatherEndPointDiscoveryCallback  = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(String endPointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                if(discoveredEndpointInfo.getServiceId().equalsIgnoreCase(SERVICE_ID))
                {
                        Log.d(TAG,"EndPointDiscovery : equals SERVICE ID");
                        Nearby.Connections.requestConnection(
                                weatherGoogleApiClient,
                                "AsusROG",
                                endPointId,
                                weatherConnectionLifecycleCallback
                        );

                }
                Log.d(TAG,"EndpointDiscovery : onEndPointFound");

            }

            @Override
            public void onEndpointLost(String s) {
                Log.e(TAG,"Disconnected");
            }
        };

        Log.d(TAG,"StartDiscovery");
        Nearby.Connections.startDiscovery(weatherGoogleApiClient,
                SERVICE_ID,
                weatherEndPointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_STAR));

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        this.initCallBackNearbyConnection();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }


    /**
     * Extracts the payloadId and filename from the message and stores it in the
     * filePayloadFilenames map. The format is payloadId:filename.
     */
    private void addPayloadFilename(String payloadFilenameMessage) {
        Log.d("TAGO","addPayLoadFileName");
        int colonIndex = payloadFilenameMessage.indexOf(':');
        String payloadId = payloadFilenameMessage.substring(0, colonIndex);
        String filename = payloadFilenameMessage.substring(colonIndex + 1);
        filePayloadFilenames.put(Long.valueOf(payloadId), filename);
        Log.d("TAGO","addPayLoad : " + payloadId);
        Log.d("TAGO","addPayLoad : " + filename);
        filename = filename.substring(5);
        String[] values = filename.split("_");
        temp = Double.valueOf(values[0]);
        humid = Double.valueOf(values[1]);
    }


    public static byte[] readBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


    public void initLastTemperature()
    {
        dateTextView.setText("Date : " + weatherManager.getLastMeasure().getDate());
        temperatureTextView.setText("Temperature : " + weatherManager.getLastMeasure().getTemperature());
        humidityTextView.setText("Humidity : " + weatherManager.getLastMeasure().getHumidity());
        screenshot.setImageBitmap(weatherManager.getLastMeasure().getScreenshot());
    }


}
