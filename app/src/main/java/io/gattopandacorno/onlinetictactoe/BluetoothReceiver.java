package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class BluetoothReceiver extends BroadcastReceiver
{

    // Not sure if this is the correct thing to do to pass the device and socket to the activity
    // sendBroadcast seems to be only for API 31+
    public BluetoothDevice dev = null;
    public BluetoothSocket bSocket;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch(Objects.requireNonNull(intent.getAction()))
        {
            case BluetoothDevice.ACTION_FOUND:
                Log.d("SOCKET", "action found");

                // Discovery has found a device. Get the BluetoothDevice
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // TODO: It should be more generalized with the code
                if(dev != null && dev.getName().equals("HT"))
                {
                    Log.d("SOCKET", "found device " + dev.getName());
                    dev.createBond();

                    try {
                        bSocket = dev.createRfcommSocketToServiceRecord(UUID.nameUUIDFromBytes("proviamo".getBytes()));
                        bSocket.connect();
                    }
                    catch (IOException e) {Log.d("SOCKET", String.valueOf(e));}
                }

                break;

            case BluetoothAdapter.ACTION_STATE_CHANGED:

                switch( intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 10))
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("SOCKET", "STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("SOCKET", "STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("SOCKET", "STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("SOCKET", "STATE TURNING ON");
                        break;
                }

            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:

                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR))
                {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("SOCKET", "Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("SOCKET", "Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("SOCKET", "Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("SOCKET", "Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("SOCKET", "Connected.");
                        break;
                }

            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().cancelDiscovery();

        }
    }
}
