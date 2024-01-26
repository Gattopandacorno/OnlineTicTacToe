package io.gattopandacorno.onlinetictactoe;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;

import java.util.Objects;


public class Login extends AppCompatActivity
{

    @SuppressLint("SetTextI18n")
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
            EditText t = findViewById(R.id.player);

            findViewById(R.id.join).setOnClickListener(v -> {

                if(!t.getText().toString().isEmpty()) i.putExtra("playerName2", t.getText().toString());
                else i.putExtra("playerName2", "PLAYER2");

                //TODO: the code to control code here
                //TODO: set name for player 1

                i.putExtra("host", false);
                i.putExtra("online", true);

                startActivity(i);
                finish();
            });//TODO: insert logic to control the code

            findViewById(R.id.host).setOnClickListener(v ->{

                if(!t.getText().toString().isEmpty()) i.putExtra("playerName1", t.getText().toString());
                else i.putExtra("playerName1", "PLAYER1");

                //TODO: the code to generate code here
                //TODO: set name for player 2 when enters the room

                i.putExtra("host", true);
                i.putExtra("online", true);
                startActivity(i);
                finish();
            });//TODO: insert logic to generate the code
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