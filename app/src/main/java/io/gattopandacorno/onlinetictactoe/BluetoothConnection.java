package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

// This class is highly inspired by @mitchtabian on github and youtube, please check all his tutorial!
// https://github.com/mitchtabian and https://www.youtube.com/@codingwithmitch
public class BluetoothConnection
{
    private static final String TAG = "SOCKET";

    // Apparently createRfcomm can be successful only if this is static
    // This is why i can't use the room code like my first idea was
    private static final UUID uuid = UUID.nameUUIDFromBytes("TRISONLINE".getBytes());
    private final BluetoothAdapter bAdapter;
    private final Context ctx;
    private AcceptThread acceptThr;
    private ConnectThread connectThr;
    private BluetoothDevice dev;
    private ConnectedThread connectedThr;
    public BluetoothSocket bSocket;

    /**
     * The contractor requires the context due to the retrieving of BluetoothAdapter.
     *  I used two devices with API 29 and 30 and i couldn't use the getDefaultAdapter.
     *  If you are using an API 31+ this is no more used either.
     *  If you are having trouble please check the android guide about BluetoothAdapter.
     */
    public BluetoothConnection(Context ctx)
    {
        this.ctx = ctx;

        bAdapter = ((BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }

    /**
     * This thread runs Accept method while listening for incoming connections.
     * This is the server-side socket. It runs until a connection is accepted (or cancelled).
     * It is used in both of the devices.
     */
    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket bServer;

        /**
         * This is where the initialization begins.
         * It is called when the thread is called with start().
         */
        @SuppressLint("MissingPermission")
        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            // ProgressDialog is deprecated so i searched a workaround using a simple ProgressBar
            Progress.showDialog(ctx);

            // Create a new listening server socket
            try{
                tmp = bAdapter.listenUsingInsecureRfcommWithServiceRecord("TRISONLINE", uuid);

                Log.d(TAG, "Setting up Server using: " + uuid);
            }

            catch (IOException e)
            {
                Log.e(TAG, "AcceptThread: " + e );
            }

            bServer = tmp;
        }

        /**
         * The run method is called when the initialization finish.
         * From the guide it is called only if the thread was constructed using a separate runnable object.
         */
        public void run()
        {
            Log.d(TAG, "AcceptThread Running.");

            BluetoothSocket bSocket = null;

            try{
                Log.d(TAG, "RFCOMM server socket start...");

                // This is a blocking call
                bSocket = bServer.accept();

                Log.d(TAG, "RFCOMM server socket accepted connection.");

            }

            catch (IOException e)
            {
                Log.e(TAG, "AcceptThread: " + e.getMessage() );
            }

            // When the connection is made (socket not null)
            if(bSocket != null)
            {
                // we close the listening server because we only have two players (one server and one client)
                cancel();

                Log.v(TAG, "Setting bsocket with bsocket " + bSocket);

                setBsocket(bSocket);
                connected(bSocket);
            }
        }

        /**
         * This method should always be used when we are connected to all the devices we want.
         * In this case we need only one device accepted.
         * An idea to close it after n devices connect is to create a list of socket
         * and control when we have all the n devices we need.
         */
        public void cancel()
        {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {bServer.close();}
            catch (IOException e)
            {
                Log.e(TAG, "Close of server failed. " + e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a device.
     * It runs straight through; the connection either succeeds or fails.
     */
    private class ConnectThread extends Thread
    {
        private BluetoothSocket mmSocket;

        /**
         * Like AcceptThread this is the constructor of ConnectThread.
         * The thread is initialized here.
         *
         * @param device A bluetoothDevice passed by startClient()
         *               In all the project i saw the best way to pass the device was onClick
         *               of the user that has to select the device to connect.
         *               In my project it is passed when a device with certain parameters is found
         *               in BroadcastReceiver.
         */
        public ConnectThread(BluetoothDevice device)
        {
            Log.d(TAG, "ConnectThread: started.");
            dev = device;
        }

        /**
         * Like AcceptThread this run is called after ConnectThread is started and initialized.
         * In this method the client tries to connect with the server listening.
         * The client in this case is one of the two devices that acts as server and client at
         * the same time.
         * The client is decided by one of the player that click on the join button.
         */
        @SuppressLint("MissingPermission")
        public void run()
        {
            BluetoothSocket tmp = null;

            // Get a Socket with the given device
            try {
                Log.d(TAG, "Trying to create InsecureRfcommSocket using: " + uuid);
                tmp = dev.createRfcommSocketToServiceRecord(uuid);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Could not create InsecureRfcommSocket " + e);
            }

            mmSocket = tmp;

            // Always cancel discovery because it will slow down a connection
            bAdapter.cancelDiscovery();

            try {
                // This is a blocking call and will only return on success or an exception
                // This call should only be used by the client-side, the server should use accept()
                mmSocket.connect();

                Log.d(TAG, "ConnectThread connected.");
            }

            catch (IOException e)
            {
                // Close the socket if there is a problem
                cancel();

                Log.d(TAG, "ConnectThread could not connect to UUID: " + uuid);
            }

            if(mmSocket != null)
            {
                Log.v(TAG, "Setting bsocket with mmsocket " + mmSocket);
                setBsocket(mmSocket);
                connected(mmSocket);
            }
        }

        /**
         * This method is used to close a socket.
         */
        public void cancel()
        {
            try {
                Log.d(TAG, "Closing Client Socket.");
                mmSocket.close();
            }

            catch (IOException e)
            {
                Log.e(TAG, " close of Socket in Connectthread failed. " + e);
            }
        }
    }

    /**
     * Start the service. Specifically AcceptThread to begin the server-side.
     */
    public synchronized void start()
    {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (connectThr != null)
        {
            connectThr.cancel();
            connectThr = null;
        }

        // Start a new server listening if there isn't already one
        if (acceptThr == null)
        {
            acceptThr = new AcceptThread();
            acceptThr.start();
        }
    }

    /**
     * startClient start a ConnectThread while the server is listening with AcceptThread.
     * @param device  This is a BluetoothDevice and it is the remote device we are trying to connect.
     */
    public void startClient(BluetoothDevice device)
    {
        Log.d(TAG, "startClient Started.");

        connectThr = new ConnectThread(device);
        connectThr.start();
    }

    /**
     * ConnectedThread is responsible for maintaining the connection, sending data and
     * receiving data through streams.
     */
    private static class ConnectedThread extends Thread
    {
        private final BluetoothSocket socket;
        private final InputStream IStream;
        private final OutputStream OStream;
        private final Context ctx;

        /**
         * This is the constructor like AcceptThread and ConnectThread.
         *
         * @param socket This is the socket connection between the two devices.
         *               The devices are already connected at this point.
         */
        public ConnectedThread(BluetoothSocket socket, Context ctx)
        {
            this.ctx = ctx;
            Log.d(TAG, "ConnectedThread Starting.");

            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = this.socket.getInputStream();
                tmpOut = this.socket.getOutputStream();
            }

            catch (IOException e)
            {
                Log.d(TAG,"retrieve streams " + e);
            }

            IStream = tmpIn;
            OStream = tmpOut;
        }

        /**
         * This run is called after the ConnectedThread finish the initialization.
         */
        public void run()
        {
            read();
        }

        /**
         * This is the method to read all the messages from the other device
         */
        public void read()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()
            Intent i = new Intent("sendmsg");

            //This is the first 'handshake', they will send each other this to mean "send me your name"
            write("start".getBytes());

            // Keep listening
            while (true)
            {
                // Read from input
                try {

                    if(IStream!=null && IStream.available() > 0)
                    {
                        // Read from the InputStream
                        bytes = IStream.read(buffer);
                        String msg = new String(buffer, 0, bytes);

                        i.putExtra("msg", msg);
                        LocalBroadcastManager.getInstance(this.ctx).sendBroadcast(i);
                    }
                }

                catch (IOException e)
                {
                    Log.e(TAG, "Error reading Input Stream. " + e );
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "Writing to output: " + text);
            try {if(bytes != null) OStream.write(bytes);}
            catch (IOException e)
            {
                Log.e(TAG, "Error writing to output. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel()
        {
            try {
                socket.close();
                IStream.close();
                OStream.close();
            }
            catch (IOException e)
            {
                Log.d("SOCKET", "error closing the socket " + e);
            }
        }
    }

    private void connected(BluetoothSocket bSocket)
    {
        Log.d(TAG, "connected: Starting.");

        // Dismiss the progress bar when connection is established because the devices can now communicate.
        Progress.dismissDialog();

        // Start the thread to manage the connection and perform transmissions
        connectedThr = new ConnectedThread(bSocket, this.ctx);
        connectedThr.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out)
    {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");

        if(connectedThr == null) connected(this.bSocket);
        if(out != null) connectedThr.write(out);
    }

    public void disconnect()
    {
        // It notify the disconnection only if the other device is connected
        if(connectedThr.socket.isConnected()) write("disconnect".getBytes());
        connectedThr.cancel();
        connectedThr = null;
        try {
            this.bSocket.close();
            this.bSocket = null;
        }
        catch (IOException e) {Log.e(TAG, "Error closing socket " + e);}


    }

    /**
     * This is a get function that returns the BluetoothAdapter of the device.
     *
     * @return BluetoothAdapter that contains the device's information about bluetooth,
     *                          and funs to use it (for ex. disable or startDiscovery)
     */
    public BluetoothAdapter getAdapter()
    {
        return bAdapter;
    }

    /**
     * Method to set bluetooth socket.
     * This is used to call connected() when bConnectedThread is null and can cause nullPointerException.
     *
     * @param bsocket The connection between the two devices.
     */
    private void setBsocket(BluetoothSocket bsocket)
    {
        Log.v(TAG, "Setting bsocket to " + bsocket.toString());
        this.bSocket = bsocket;
    }

}
