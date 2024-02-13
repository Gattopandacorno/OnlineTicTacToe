package io.gattopandacorno.onlinetictactoe;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;


public class Login extends AppCompatActivity
{

    @SuppressLint({"SetTextI18n", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, GameLogic.class);

        //If the game is in local mode the players have to compile formlocal
        if(!getIntent().getBooleanExtra("online", false))
        {
            setContentView(R.layout.formlocal);
            EditText t1 = findViewById(R.id.player1), t2 = findViewById(R.id.player2);

            //Setting click listener for when play button is clicked
            findViewById(R.id.playButton).setOnClickListener(v -> {
                if(!t1.getText().toString().isEmpty()) i.putExtra("playerName1", t1.getText().toString());
                else i.putExtra("playerName1", "PLAYER1");

                if(!t2.getText().toString().isEmpty()) i.putExtra("playerName2", t2.getText().toString());
                else i.putExtra("playerName2", "PLAYER2");

                i.putExtra("online", false);

                startActivity(i); //Start the activity with the board's game
                finish();
            });
        }

        //Else (mode = online) the form will be formonline
        else
        {
            setContentView(R.layout.formonline);
            EditText t = findViewById(R.id.player), code = findViewById(R.id.code);
            NsdManager nsd = (NsdManager) getSystemService(Context.NSD_SERVICE);

            findViewById(R.id.join).setOnClickListener(v -> {

                String c = code.getText().toString();
                NsdServiceInfo sinfo = new NsdServiceInfo();
                sinfo.setServiceType("_TRIS._tcp.");
                sinfo.setPort(1111);
                sinfo.setServiceName("TRIS");

                nsd.registerService(sinfo, NsdManager.PROTOCOL_DNS_SD, new NsdManager.RegistrationListener() {
                    @Override
                    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode)
                    {
                        nsd.discoverServices("_TRIS._tcp.", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
                            @Override
                            public void onStartDiscoveryFailed(String serviceType, int errorCode) {Log.d("SOCKET", "discovery failed" + errorCode);}
                            @Override
                            public void onStopDiscoveryFailed(String serviceType, int errorCode) {Log.d("SOCKET", "stop discovery failed" + errorCode);}
                            @Override
                            public void onDiscoveryStarted(String serviceType) {Log.d("SOCKET", "start discovery");}
                            @Override
                            public void onDiscoveryStopped(String serviceType) {Log.d("SOCKET", "discovery stopped");}
                            @Override
                            public void onServiceFound(NsdServiceInfo serviceInfo)
                            {
                                if(serviceInfo.getHostAddresses().size() == 0)
                                    nsd.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                                        @Override
                                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {}

                                        @Override
                                        public void onServiceResolved(NsdServiceInfo serviceInfo)
                                        {
                                            try {Socket s = new Socket(serviceInfo.getHost(), serviceInfo.getPort());}
                                            catch (IOException e)
                                            {throw new RuntimeException(e);}

                                        }
                                    });

                                else Toast.makeText(Login.this, "Game room already full", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onServiceLost(NsdServiceInfo serviceInfo) {Log.d("SOCKET", "service lost");}
                        });
                    }

                    @Override
                    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {Log.d("SOCKET", "Service not registered" +errorCode);}

                    @Override
                    public void onServiceRegistered(NsdServiceInfo serviceInfo) {Log.d("SOCKET", "Service registered!");}

                    @Override
                    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {Log.d("SOCKET", "Service unregistered!");}
                });


            });
        }

        /* This take to the MainActivity if the back button is pressed */
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed()
            {
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}