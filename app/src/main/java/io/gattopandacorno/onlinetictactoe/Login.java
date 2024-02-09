package io.gattopandacorno.onlinetictactoe;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class Login extends AppCompatActivity
{

    private WifiP2pManager mng;
    private WifiP2pManager.Channel channel;
    private Wifip2pReceiver receiver;
    private IntentFilter fil = new IntentFilter();
    final HashMap<String, String> services = new HashMap<String, String>();


    @SuppressLint({"SetTextI18n", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, GameLogic.class);

        //If the game is in local mode the players have to compile formlocal
        if (!getIntent().getBooleanExtra("online", false))
        {
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
        else
        {
            setContentView(R.layout.formonline);
            EditText t = findViewById(R.id.player), code = findViewById(R.id.code);

            mng      = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
            channel  = mng.initialize(this, Looper.getMainLooper(), () -> Log.d("WIFIP2P", "Channel disconnected"));
            receiver = new Wifip2pReceiver(mng, channel, Login.this);

            // Set click listener for when Join button is touched
            findViewById(R.id.join).setOnClickListener(v -> {

                String c = code.getText().toString();

                // If the code 'c' is not empty, exists and there is only one player (the host) in the game
                if (!c.isEmpty())
                {
                    if (!t.getText().toString().isEmpty())
                        i.putExtra("playerName2", t.getText().toString());
                    else i.putExtra("playerName2", "PLAYER2");

                    i.putExtra("online", true);
                    i.putExtra("code", c);

                    new Thread(this::requestService).start();

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
            public void handleOnBackPressed()
            {
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(receiver, fil);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void discoverService()
    {
        /* Callback includes:
         * fullDomain: full domain name: e.g. "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */
        WifiP2pManager.DnsSdTxtRecordListener txtListener =
                (fullDomainName, txtRecordMap, srcDevice) -> {
                    Log.d("WIFIP2P", "TxTRecord available -" + txtRecordMap.toString());
                    services.put(srcDevice.deviceAddress, txtRecordMap.get("DevName"));
                };

        WifiP2pManager.DnsSdServiceResponseListener servListener =
                (instanceName, registrationType, resourceType) -> Log.d("WIFIP2P", "onService available " + instanceName);

        mng.setDnsSdResponseListeners(channel, servListener, txtListener);
    }

    private void requestService()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0)
            mng.addServiceRequest(channel, WifiP2pDnsSdServiceRequest.newInstance(), new WifiP2pManager.ActionListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess()
                {
                    mng.discoverServices(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() { discoverService();}

                        @Override
                        public void onFailure(int reason)
                        {
                            if (reason == WifiP2pManager.P2P_UNSUPPORTED)
                                Log.d("WIFIP2P", "Wi-Fi Direct not supported" + reason);
                        }
                    });
                }
                @Override
                public void onFailure(int reason)
                {
                    if (reason == WifiP2pManager.P2P_UNSUPPORTED)
                        Log.d("WIFIP2P", "Wi-Fi Direct not supported" + reason);
                }
            });
    }
}

