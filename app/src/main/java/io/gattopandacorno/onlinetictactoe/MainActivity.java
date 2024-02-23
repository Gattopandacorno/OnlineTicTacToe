package io.gattopandacorno.onlinetictactoe;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
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

    @SuppressLint("MissingPermission")
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

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 255);

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 255 && resultCode != 0)
        {
            Intent i = new Intent(this, Login.class);
            i.putExtra("online", true);

            startActivity(i); //Start the activity with the name+join/host form before playing
            finish();

        }
    }
}