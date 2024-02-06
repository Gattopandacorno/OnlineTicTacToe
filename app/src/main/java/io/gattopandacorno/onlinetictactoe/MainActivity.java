package io.gattopandacorno.onlinetictactoe;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
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


            /** If the WiFi or the GPS are not enabled the player cannot start the online game
             *  Work in progress --> It shouldn't be done with Toasts!
             *  TODO: do in the proper manner!
             */
            if(!((WifiManager)getSystemService(Context.WIFI_SERVICE)).isWifiEnabled())
                Toast.makeText(this, "To access this service you should enable wifi", Toast.LENGTH_SHORT).show();

            else if(!((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER))
                Toast.makeText(this, "To access service you should enable GPS", Toast.LENGTH_SHORT).show();

            else if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT))
                Toast.makeText(this, "You can't use this service due to a missing system feature", Toast.LENGTH_SHORT).show();

            else
            {
                Intent i = new Intent(this, Login.class);

                i.putExtra("online", true);

                startActivity(i); //Start the activity with the name+join/host form before playing
                finish();
            }

        });

    }
}