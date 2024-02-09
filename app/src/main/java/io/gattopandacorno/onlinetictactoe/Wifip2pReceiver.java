package io.gattopandacorno.onlinetictactoe;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Objects;

public class Wifip2pReceiver extends BroadcastReceiver
{
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final AppCompatActivity activity;

    boolean found = false;

    public Wifip2pReceiver(WifiP2pManager m, WifiP2pManager.Channel c, AppCompatActivity a)
    {
        super();
        this.manager  = m;
        this.channel  = c;
        this.activity = a;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        WifiP2pDevice mydev =  intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        String code = intent.getStringExtra("code");

        switch(Objects.requireNonNull(action))
        {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                if(intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) != WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                    Log.d("WIFIP2P", "not enabled");

            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if (manager != null && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == 0)
                   manager.requestPeers(channel, peers -> {

                       if(peers.getDeviceList().size() == 0)
                           Log.d("WIFIP2P", "no device found");
                       else
                           for(WifiP2pDevice p : peers.getDeviceList())
                               Log.d("WIFIP2P", "device " + p.deviceName);
                   });

            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                if(mydev != null)
                {
                    mydev.deviceName = "newName";
                    Log.d("WIFIP2P", "mydev name " + mydev.deviceName);
                }
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                // respond to connection or disconnection

        }
    }
}
