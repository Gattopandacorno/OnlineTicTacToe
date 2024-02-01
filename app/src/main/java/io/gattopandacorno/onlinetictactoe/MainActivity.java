package io.gattopandacorno.onlinetictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set click listener for when Play local game button is touched
        findViewById(R.id.localButton).setOnClickListener(v->{
            Intent i = new Intent(this, Login.class);

            i.putExtra("online", false);

            startActivity(i); //Start the activity with the name form before playing
            finish();
        });

        // Set click listener for when Play multiplayer game button is touched
        findViewById(R.id.multiButton).setOnClickListener(v -> {


            if(((WifiManager)getSystemService(Context.WIFI_SERVICE)).isWifiEnabled())
            {
                Intent i = new Intent(this, Login.class);

                i.putExtra("online", true);

                startActivity(i); //Start the activity with the name+join/host form before playing
                finish();
            }

            else
                Toast.makeText(this, "To access this service you should enable wifi", Toast.LENGTH_SHORT).show();
        });

    }
}