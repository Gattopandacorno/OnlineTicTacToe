package io.gattopandacorno.onlinetictactoe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Wifip2pReceiver extends BroadcastReceiver
{
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final AppCompatActivity activity;

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

        switch(Objects.requireNonNull(action))
        {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                if(intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) != WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                    Log.d("WIFIP2P", "not enabled");
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if(manager != null)
                    manager.requestPeers(channel, peers -> {
                        for (WifiP2pDevice device : peers.getDeviceList())
                            Log.d("WIFIP2P", device.deviceName);
                    }); // TODO: add logic if the code in intent already exist or not + connect?
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
        }
    }
}
