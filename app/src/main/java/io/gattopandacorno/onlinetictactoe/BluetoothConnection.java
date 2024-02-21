package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnection
{
    private final UUID uuid;

    private final BluetoothAdapter bAdapter;
    private BluetoothDevice dev;
    Context ctx;

    private ConnectThread connectThr;
    ProgressDialog progressbar;

    private ConnectedThread mConnectedThread;

    public BluetoothConnection(Context context, String code)
    {
        this.uuid = UUID.nameUUIDFromBytes(code.getBytes());
        this.ctx  = context;
        bAdapter  = BluetoothAdapter.getDefaultAdapter();
    }



    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread
    {

        // The local server socket
        private final BluetoothServerSocket server;

        @SuppressLint("MissingPermission")
        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket

            try{
                tmp = bAdapter.listenUsingInsecureRfcommWithServiceRecord("tictactoe", uuid);

                Log.d("SOCKET", "AcceptThread: Setting up Server using: " + uuid);
            }

            catch (IOException e){Log.e("SOCKET", "AcceptThread: IOException: " + e.getMessage() );}

            server = tmp;
        }

        public void run()
        {
            Log.d("SOCKET", "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d("SOCKET", "run: RFCOM server socket start.....");

                // TODO: close the socket when one connection is established
                socket = server.accept();

                Log.d("SOCKET", "run: RFCOM server socket accepted connection.");

            }

            catch (IOException e){Log.e("SOCKET", "AcceptThread: IOException: " + e );}

            //talk about this is in the 3rd
            if(socket != null) connected(socket, dev);

            Log.i("SOCKET", "END mAcceptThread ");
        }

        public void cancel()
        {
            Log.d("SOCKET", "Canceling AcceptThread.");
            try {
                server.close();}
            catch (IOException e) {Log.e("SOCKET", "Close of AcceptThread ServerSocket failed. " + e );}
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread
    {
        private BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device)
        {
            Log.d("SOCKET", "ConnectThread: started.");
            dev = device;
        }

        @SuppressLint("MissingPermission")
        public void run()
        {
            Log.i("SOCKET", "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                Log.d("SOCKET", "Trying to create InsecureRfcommSocket using UUID: " + uuid );
                socket = dev.createInsecureRfcommSocketToServiceRecord(dev.getUuids()[0].getUuid());
            }

            catch (IOException e) {Log.e("SOCKET", "ConnectThread: Could not create InsecureRfcommSocket " + e);}

            // Always cancel discovery because it will slow down a connection
            bAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket.connect();

                Log.d("SOCKET", "run: ConnectThread connected.");
            }

            catch (IOException e)
            {
                // Close the socket
                cancel();
                Log.d("SOCKET", "run: ConnectThread: Could not connect to UUID: " + uuid );
            }

            //will talk about this in the 3rd video
            connected(socket, dev);
        }
        public void cancel()
        {
            try {
                Log.d("SOCKET", "Closing Client Socket.");
                socket.close();
            }
            catch (IOException e) {Log.e("SOCKET", "close() of mmSocket in Connectthread failed. " + e);}
        }
    }



    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start()
    {
        Log.d("SOCKET", "start");

        // Cancel any thread attempting to make a connection
        if (connectThr != null)
        {
            connectThr.cancel();
            connectThr = null;
        }

        else
        {
            AcceptThread mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/

    public void startClient(BluetoothDevice device)
    {
        Log.d("SOCKET", "startClient: Started.");

        //initprogress dialog
        progressbar = ProgressDialog.show(this.ctx,"Connecting Bluetooth"
                ,"Please Wait...",true);

        connectThr = new ConnectThread(device);
        connectThr.start();
    }

    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket socket;
        private final InputStream IStream;
        private final OutputStream OStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            Log.d("SOCKET", "ConnectedThread: Starting.");

            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                progressbar.dismiss();}
            catch (NullPointerException e){e.printStackTrace();}


            try {
                tmpIn  = this.socket.getInputStream();
                tmpOut = this.socket.getOutputStream();
            }

            catch (IOException e) {e.printStackTrace();}

            IStream = tmpIn;
            OStream = tmpOut;
        }

        public void run(){read();}

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d("SOCKET", "write: Writing to outputstream: " + text);
            try {OStream.write(bytes);}
            catch (IOException e) {Log.e("SOCKET", "write: Error writing to output stream. " + e.getMessage() );}
        }

        public String read()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()
            String incomingMessage = "";

            while (true)
            {
                // Read from the InputStream
                try {
                    bytes = IStream.read(buffer);
                    incomingMessage = new String(buffer, 0, bytes);
                    Log.d("SOCKET", "InputStream: " + incomingMessage);
                }

                catch (IOException e)
                {
                    Log.e("SOCKET", "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
            return incomingMessage;
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel()
        {
            try {
                socket.close();}
            catch (IOException e) {Log.d("SOCKET", String.valueOf(e));}
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice device)
    {
        Log.d("SOCKET", "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out)
    {
        // Create temporary object

        // Synchronize a copy of the ConnectedThread
        Log.d("SOCKET", "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

    public String read()
    {
        return mConnectedThread.read();
    }

}