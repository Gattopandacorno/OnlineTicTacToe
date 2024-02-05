package io.gattopandacorno.onlinetictactoe;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;


public class Login extends AppCompatActivity {

    WifiP2pManager mng;
    WifiP2pManager.Channel channel;
    Wifip2pReceiver receiver;
    IntentFilter fil = new IntentFilter();

    @SuppressLint({"SetTextI18n", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mng = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = mng.initialize(this, Looper.getMainLooper(), () -> Log.d("WIFIP2P", "Channel disconnected"));
        receiver = new Wifip2pReceiver(mng, channel, Login.this);

        Intent i = new Intent(this, GameLogic.class);

        //If the game is in local mode the players have to compile formlocal
        if (!getIntent().getBooleanExtra("online", false)) {
            setContentView(R.layout.formlocal);
            EditText t1 = findViewById(R.id.player1), t2 = findViewById(R.id.player2);

            //Setting click listener for when play button is clicked
            findViewById(R.id.playButton).setOnClickListener(v -> {
                if (!t1.getText().toString().isEmpty())
                    i.putExtra("playerName1", t1.getText().toString());
                else i.putExtra("playerName1", "PLAYER1");

                if (!t2.getText().toString().isEmpty())
                    i.putExtra("playerName2", t2.getText().toString());
                else i.putExtra("playerName2", "PLAYER2");

                i.putExtra("online", false);

                startActivity(i); //Start the activity with the board's game
                finish();
            });
        }

        //Else (mode = online) the form will be formonline
        else {
            setContentView(R.layout.formonline);
            EditText t = findViewById(R.id.player), code = findViewById(R.id.code);

            ActivityCompat.requestPermissions(Login.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.NEARBY_WIFI_DEVICES,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 111);

            // Set click listener for when Create/Host button is touched
            findViewById(R.id.host).setOnClickListener(v -> {

                String c = code.getText().toString();

                // If the code 'c' is not empty and doesn't already exists the it hosts a new game
                if (!c.isEmpty()) {
                    // Set database value for the player who hosts the game; if not given the default is "PLAYER1"
                    if (!t.getText().toString().isEmpty())
                        i.putExtra("playerName1", t.getText().toString());
                    else i.putExtra("playerName1", "PLAYER1");


                    i.putExtra("online", true);
                    i.putExtra("host", true);
                    i.putExtra("code", c);
                    startActivity(i);
                    finish();
                }

                // If 'c' already exists or if it is null (the EditText was not filled out)
                else
                    Toast.makeText(Login.this, "Enter a valid code!", Toast.LENGTH_SHORT).show();
            });

            // Set click listener for when Join button is touched
            findViewById(R.id.join).setOnClickListener(v -> {

                String c = code.getText().toString();

                // If the code 'c' is not empty, exists and there is only one player (the host) in the game
                if (!c.isEmpty()) {
                    // Set database value for the player who hosts the game; if not given the default is "PLAYER1"
                    if (!t.getText().toString().isEmpty())
                        i.putExtra("playerName2", t.getText().toString());
                    else i.putExtra("playerName2", "PLAYER2");


                    i.putExtra("online", true);
                    i.putExtra("host", false);
                    i.putExtra("code", c);


                    startActivity(i);
                    finish();
                }

                // If 'c' not exists, is empty or the game room is already full (two players)
                else
                    Toast.makeText(Login.this, "Enter a valid code", Toast.LENGTH_SHORT).show();

            });

        }


        /* This take to the MainActivity if the back button is pressed */
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, fil);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 111 && grantResults.length > 0) {
            fil.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            fil.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            fil.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            fil.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != 0
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != 0)
            {
                mng.discoverServices(channel, new WifiP2pManager.ActionListener() {

                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess()
                    {
                        Log.d("WIFIP2P", "discovery started");


                        mng.requestPeers(channel, peers -> {
                            for(WifiP2pDevice dev : peers.getDeviceList())
                                Log.d("WIFIP2P", "DEV NAME: " + dev.deviceName);
                        });
                    }

                    @Override
                    public void onFailure(int reason)
                    {
                        Log.d("WIFIP2P", "fails on discovery " + reason);
                    }
                });
            }
        }
    }
}

