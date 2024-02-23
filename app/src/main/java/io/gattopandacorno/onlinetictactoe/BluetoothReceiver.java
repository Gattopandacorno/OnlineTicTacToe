package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
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
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch(Objects.requireNonNull(intent.getAction()))
        {
            case BluetoothDevice.ACTION_FOUND:
                Log.d("SOCKET", "action found");

                // Discovery has found a device. Get the BluetoothDevice
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(dev != null && dev.getName() != null && Objects.equals(dev.getName(), "HT"))
                {
                    Log.d("SOCKET", "found device " + dev.getName());
                    dev.createBond();

                    Intent local = new Intent();
                    local.setAction("service.to.activity.transfer");
                    local.putExtra("device", dev);
                    context.sendBroadcast(local);
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

            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                switch (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 10))
                {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("SOCKET", "bonding");
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        Log.d("SOCKET", "bonded");
                        break;
                }

        }
    }
}
