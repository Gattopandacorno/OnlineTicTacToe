package io.gattopandacorno.onlinetictactoe;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Login extends AppCompatActivity
{

    @SuppressLint({"SetTextI18n", "NewApi"})
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
            EditText t = findViewById(R.id.player), code = findViewById(R.id.code);
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();

            // Set click listener for when Create/Host button is touched
            findViewById(R.id.host).setOnClickListener(v ->{

                String c = code.getText().toString();

                db.child("codes").child(c).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        // If the code 'c' is not empty and doesn't already exists the it hosts a new game
                        if (!c.isEmpty() && !snapshot.exists())
                        {
                            // Set database value for the player who hosts the game; if not given the default is "PLAYER1"
                            if(!t.getText().toString().isEmpty()) i.putExtra("playerName1", t.getText().toString());
                            else i.putExtra("playerName1", "PLAYER1");

                            db.child("codes").child(c).child("0").setValue(i.getStringExtra("playerName1"));
                            i.putExtra("host", true);
                            i.putExtra("online", true);
                            i.putExtra("code", c);
                            startActivity(i);
                            finish();
                        }

                        // If 'c' already exists or if it is null (the EditText was not filled out)
                        else
                            Toast.makeText(Login.this, "Enter a valid code!", Toast.LENGTH_SHORT).show();
                    }

                    @Override // If there is some error during the previous operation
                    public void onCancelled(@NonNull DatabaseError error) { Log.d("FIREDB", error.toString());}
                });
            });

            // Set click listener for when Join button is touched
            findViewById(R.id.join).setOnClickListener(v -> {

                String c = code.getText().toString();

                db.child("codes").child(c).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        // If the code 'c' is not empty, exists and there is only one player (the host) in the game
                        if(!c.isEmpty() && snapshot.exists() && snapshot.getChildrenCount() == 1)
                        {
                            // Set database value for the player who hosts the game; if not given the default is "PLAYER1"
                            if(!t.getText().toString().isEmpty()) i.putExtra("playerName2", t.getText().toString());
                            else i.putExtra("playerName2", "PLAYER2");

                            db.child("codes").child(c).child("1").setValue(i.getStringExtra("playerName2"));

                            i.putExtra("code", c);
                            i.putExtra("host", false);
                            i.putExtra("online", true);
                            i.putExtra("code", c);
                            startActivity(i);
                            finish();
                        }

                        // If 'c' not exists, is empty or the game room is already full (two players)
                        else
                            Toast.makeText(Login.this, "Enter a valid code", Toast.LENGTH_SHORT).show();

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {Log.d("FIREDB", error.toString());}
                });
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