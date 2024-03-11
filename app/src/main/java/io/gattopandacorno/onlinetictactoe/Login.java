package io.gattopandacorno.onlinetictactoe;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;


public class Login extends AppCompatActivity
{
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
            EditText t = findViewById(R.id.player);

            // the app only need to ask location permissions because is the only dangerous one used
            ActivityCompat.requestPermissions(Login.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADMIN}, 255);

            i.putExtra("playerName1", "PLAYER1");
            i.putExtra("playerName2", "PLAYER2");

            // Set click listener for when Join button is touched
            findViewById(R.id.host).setOnClickListener(v -> {

                if (!t.getText().toString().isEmpty())
                    i.putExtra("playerName1", t.getText().toString());

                i.putExtra("online", true);
                i.putExtra("host", true);

                startActivity(i);
                finish();

            });

            // Set click listener for when Join button is touched
            findViewById(R.id.join).setOnClickListener(v -> {

                if (!t.getText().toString().isEmpty())
                    i.putExtra("playerName2", t.getText().toString());


                i.putExtra("online", true);
                i.putExtra("host", false);

                startActivity(i);
                finish();
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

