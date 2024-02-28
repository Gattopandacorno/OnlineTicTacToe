package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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

    private ConnectedThread connectedThr;

    public BluetoothConnection(Context context, String code)
    {
        this.uuid = UUID.nameUUIDFromBytes(code.getBytes());
        this.ctx  = context;
        bAdapter  = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }



    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread
    {

        // The local server socket
        private BluetoothServerSocket bServer = null;

        @SuppressLint("MissingPermission")
        public AcceptThread()
        {
            // Create a new listening server
            try{
                bServer = bAdapter.listenUsingInsecureRfcommWithServiceRecord("tictactoe", uuid);

                Log.d("SOCKET", "Accept: Setting up Server using: " + uuid);
            }

            catch (IOException e){Log.e("SOCKET", "Exception in Accept: " + e.getMessage() );}
        }

        public void run()
        {
            Log.d("SOCKET", "AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a successful connection or an exception
                Log.d("SOCKET", "run: RFCOMM server socket start.....");

                socket = bServer.accept();

                if(socket != null) cancel();

                Log.d("SOCKET", "RFCOMM server socket accepted connection and closed server.");

            }

            catch (IOException e){Log.e("SOCKET", "Exception in Accept: " + e );}

            if(socket != null) connected(socket, dev);

            Log.i("SOCKET", "end Accept ");
        }

        public void cancel()
        {
            Log.d("SOCKET", "Canceling Accept.");
            try {bServer.close();}
            catch (IOException e) {Log.e("SOCKET", "Close of Server failed. " + e );}
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread
    {
        private BluetoothSocket bSocket;

        public ConnectThread(BluetoothDevice device)
        {
            Log.d("SOCKET", "Connect started.");
            dev = device;
        }

        @SuppressLint("MissingPermission")
        public void run()
        {
            Log.i("SOCKET", "Connect started");

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                Log.d("SOCKET", "Trying to create InsecureRfcommSocket using UUID: " + uuid );
                dev.createBond();
                bSocket = dev.createInsecureRfcommSocketToServiceRecord(uuid);
            }

            catch (IOException e) {Log.e("SOCKET", "Could not create InsecureRfcommSocket " + e);}

            // Always cancel discovery because it will slow down a connection
            bAdapter.cancelDiscovery();

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                bSocket.connect();

                Log.d("SOCKET", "Connected.");
            }

            catch (IOException e)
            {
                // Close the socket
                cancel();
                Log.d("SOCKET", "Could not connect to UUID: " + uuid );
            }

            connected(bSocket, dev);
        }

        public void cancel()
        {
            try {
                Log.d("SOCKET", "Closing Client Socket.");
                bSocket.close();
            }
            catch (IOException e) {Log.e("SOCKET", "close() of bSocket failed. " + e);}
        }
    }




    public synchronized void start()
    {
        Log.d("SOCKET", "communication start");

        // Cancel any thread attempting to make a connection
        if (connectThr != null)
        {
            connectThr.cancel();
            connectThr = null;
        }

        else
        {
            AcceptThread insecureAccept = new AcceptThread();
            insecureAccept.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/

    public void startClient(BluetoothDevice device)
    {
        Log.d("SOCKET", "startClient: Started.");

        //init progress dialog
        Progress.showDialog(ctx, "Waiting for connection...");

        connectThr = new ConnectThread(device);
        connectThr.start();
    }

    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/
    private static class ConnectedThread extends Thread
    {
        private final BluetoothSocket bSocket;
        private InputStream IStream;
        private OutputStream OStream;

        public ConnectedThread(BluetoothSocket s)
        {
            Log.d("SOCKET", "Connected Starting.");

            this.bSocket = s;

            //dismiss the progress dialog when connection is established
            Progress.dismissDialog();


            try {
                IStream = this.bSocket.getInputStream();
                OStream = this.bSocket.getOutputStream();
            }

            catch (IOException e) {e.printStackTrace();}
        }

        public void run(){read();}

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d("SOCKET", "write: Writing to output stream: " + text);
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
                bSocket.close();}
            catch (IOException e) {Log.d("SOCKET", String.valueOf(e));}
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice device)
    {
        Log.d("SOCKET", "connected Starting.");

        // Start the thread to manage the connection and perform transmissions
        connectedThr = new ConnectedThread(mmSocket);
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
        // Create temporary object

        // Synchronize a copy of the ConnectedThread
        Log.d("SOCKET", "Write Called.");
        //perform the write
        connectedThr.write(out);
    }

    public String read()
    {
        return connectedThr.read();
    }

}