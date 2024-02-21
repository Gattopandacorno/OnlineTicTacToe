package io.gattopandacorno.onlinetictactoe;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
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


            /** If Bluetooth is not enabled the player cannot start the online game
             *  Work in progress --> It shouldn't be done with Toasts!
             *  TODO: do in the proper manner!
             */
            if(!((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled())
                Toast.makeText(this, "To access this service you should enable bluetooth", Toast.LENGTH_SHORT).show();

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