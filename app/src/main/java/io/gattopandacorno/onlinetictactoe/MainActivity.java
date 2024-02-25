package io.gattopandacorno.onlinetictactoe;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
            startLogin(false);
        });

        // Set click listener for when Play multiplayer game button is touched
        findViewById(R.id.multiButton).setOnClickListener(v -> {

            if(!((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled())
            {
                new AlertDialog.Builder(this).
                        setMessage("If you want to access the online game, you must use your bluetooth")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                            startActivityForResult(enableBtIntent, 255);})
                        .show();
            }

            else
            {
                startLogin(true);
            }


        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 255 && resultCode != 0)
        {
           startLogin(true);
        }
    }


    /**
     * This method was created to not repeat the same code for online and local game.
     * Start the activity Login and put an extra boolean param to the intent, this is made to distinguish
     * when the user clicked online or local button.
     * @param online
     */
    private void startLogin(boolean online)
    {
        Intent i = new Intent(this, Login.class);
        i.putExtra("online", online);

        startActivity(i); //Start the Login activity
        finish();
    }
}