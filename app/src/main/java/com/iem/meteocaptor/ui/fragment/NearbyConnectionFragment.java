package com.iem.meteocaptor.ui.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private Context context;
    private String endPoint ="";
    private ImageView imageView;

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
        View v = inflater.inflate(R.layout.fragment_nearby_connection, container, false);
       imageView = v.findViewById(R.id.nearby_connection_imageview);
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
            public void onPayloadReceived(String s, Payload payload) {
                byte[] imageBytes = payload.asBytes();
                final Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
                imageView.setImageBitmap(bitmapImage    );
                Log.e(TAG,"Reception : " + new String(payload.asBytes()));

                Toast.makeText(context, "It Works !!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

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
}
