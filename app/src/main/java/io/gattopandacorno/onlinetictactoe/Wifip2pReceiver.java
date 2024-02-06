package io.gattopandacorno.onlinetictactoe;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Objects;

public class Wifip2pReceiver extends BroadcastReceiver
{
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final AppCompatActivity activity;

    private List<WifiP2pDevice> peerList;

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
                if(manager != null && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == 0)
                    manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers)
                        {
                            if(!peerList.equals(peers))
                            {
                                peerList.clear();
                                peerList.addAll(peers.getDeviceList());
                            }

                            if(peers.getDeviceList().size() == 0)
                                Log.d("WIFIP2P", "no device found");
                            else
                                for(WifiP2pDevice p : peers.getDeviceList())
                                    Log.d("WIFIP2P", p.deviceName);
                        }
                    });
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                // respond to connection or disconnection

        }
    }
}
