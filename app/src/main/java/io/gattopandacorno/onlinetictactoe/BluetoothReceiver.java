package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class BluetoothReceiver extends BroadcastReceiver
{

    private final BluetoothConnection bConnection;

    public BluetoothReceiver(Context ctx)
     {
         bConnection = new BluetoothConnection(ctx);
     }


    /**
     * This receiver retrieve 3 bluetooth event.
     * - The first case is the most important one.
     *   When a device with the specific name HT is found by this device then it starts the client
     *   to pair (if not already) and connect the two.
     * - The ACTION_STATE_CHANGED is used to control the internal state of the device's bluetooth.
     * - The ACTION_SCAN_MODE_CHANGED is used to control when the device is connecting or discoverable.
     *
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch(Objects.requireNonNull(intent.getAction()))
        {
            case BluetoothDevice.ACTION_FOUND:
                Log.d("SOCKET", "action found");

                // Discovery has found a device
                BluetoothDevice tmp = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(tmp!=null && tmp.getName()!=null && tmp.getName().equals("HT"))
                    bConnection.startClient(tmp);

            case BluetoothAdapter.ACTION_STATE_CHANGED:

                switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 10))
                {
                    // TODO: why bluetooth is detected off for joining device?
                    // Can't place alertDialog here because the app crashes

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
                        Log.d("SOCKET", "Discoverability Enabled. Able to receive connections.");
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
        }
    }

    /**
     * This method writes to the other devices using bluetooth connection.
     * This is done to privatize bConnection to avoid the direct use.
     *
     * @param msg This string is the message to send to the other device.
     */
    public void sendMsg(String msg)
    {
        bConnection.write(msg.getBytes());
    }

    /**
     * This method disconnects the devices connected with bluetooth socket.
     * This is done to privatize bConnection to avoid the direct use.
     */
    public void disconnect()
    {
        bConnection.disconnect();
    }

    /**
     * This method start the bluetooth's server thread.
     * This is done to privatize bConnection to avoid the direct use.
     */
    public void startServer()
    {
        bConnection.start();
    }

    /**
     * Start bluetooth discovery.
     * The discovery was performed by another adapter in GameLogic,
     * in order to not have duplicates i think its easier to get the adapter through receiver and connection.
     */
    @SuppressLint("MissingPermission")
    public void startDiscovery()
    {
        bConnection.getAdapter().startDiscovery();
    }

    /**
     * Set the name seen by searching bluetooth devices.
     * Like the start discovery it was performed by an adapter duplicate.
     * The default name to make the searching easier is HT (it stands for host tictactoe).
     */
    @SuppressLint("MissingPermission")
    public void setDeviceName()
    {
        bConnection.getAdapter().setName("HT");
    }

    /**
     * Disables device's bluetooth.
     * Like the start discovery and setName it was performed by an adapter duplicate.
     */
    @SuppressLint("MissingPermission")
    public void disableBluetooth()
    {
        bConnection.getAdapter().disable();
    }

}
