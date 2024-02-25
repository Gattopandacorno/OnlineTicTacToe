package io.gattopandacorno.onlinetictactoe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity
{

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set click listener for when play local game button is touched
        findViewById(R.id.localButton).setOnClickListener(v->{startLogin(false);});

        // Set click listener for when Play multiplayer game button is touched
        findViewById(R.id.multiButton).setOnClickListener(v -> {

            // If the bluetooth is not enabled the user must turn it on to play the online game
            if(!((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled())
            {
                // Advise the player about the usage of bluetooth
                new AlertDialog.Builder(this).
                        setMessage("If you want to access the online game, you must use your bluetooth. " +
                                   "This is done to search and connect with the other player. Click 'OK' to turn it on.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                            startActivityForResult(enableBtIntent, 255);})
                        .show();
            }

            // If the bluetooth is already on the user can access the online Login
            else
            {
                startLogin(true);
            }
        });
    }

    /**
     * This method is used to know the result of the bluetooth enabling request.
     * If the result code is OK the user can use the online service and it starts the online Login.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
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
     *
     * @param online Boolean param to know if the intended game was online or not.
     *              It's useful to control the GameLogic logic.
     */
    private void startLogin(boolean online)
    {
        Intent i = new Intent(this, Login.class);
        i.putExtra("online", online);

        startActivity(i); //Start the Login activity
        finish();
    }
}