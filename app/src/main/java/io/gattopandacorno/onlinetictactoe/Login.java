package io.gattopandacorno.onlinetictactoe;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class Login extends AppCompatActivity
{

    private final String url= "https://onlinetictactoe-4e7b9-default-rtdb.europe-west1.firebasedatabase.app/";


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

                // Set value for the player who hosts the game; if not given the default is "PLAYER1"
                if(!t.getText().toString().isEmpty()) i.putExtra("playerName1", t.getText().toString());
                else i.putExtra("playerName1", "PLAYER1");

                db.child("codes").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        new Handler().postDelayed(() -> {
                            if(!c.isEmpty() && !IsValueAvailable(snapshot, c))
                                db.child("codes").push().setValue(c);
                            else
                                Toast.makeText(Login.this, "Enter a valid code", Toast.LENGTH_SHORT).show();
                            },2000);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                });

                i.putExtra("host", true);
                i.putExtra("online", true);
                startActivity(i);
                finish();



            });

            // Set click listener for when Join button is touched
            findViewById(R.id.join).setOnClickListener(v -> {

                String c = code.getText().toString();

                // Set value for the player who joins the game; if not given the default is "PLAYER2"
                if(!t.getText().toString().isEmpty()) i.putExtra("playerName2", t.getText().toString());
                else i.putExtra("playerName2", "PLAYER2");

                db.child("codes").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        new Handler().postDelayed(() -> {
                            if(!c.isEmpty() && IsValueAvailable(snapshot, c))
                            {
                                i.putExtra("playerName1",db.child("codes").child("players").get().toString());
                                i.putExtra("host", false);
                                i.putExtra("online", true);
                                startActivity(i);
                                finish();
                            }
                            else
                                Toast.makeText(Login.this, "Enter a valid code", Toast.LENGTH_SHORT).show();
                            }, 2000);

                    }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {}
                });

            }); //TODO: set name for player 2 when enters the room
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

    /**
     * Used to control if the code given by the joining player is available online
     * It searches in all the 'entry' of the snapshot for a value equal to the given code
     */
    private boolean IsValueAvailable(DataSnapshot snap, String code)
    {
        for (DataSnapshot s : snap.getChildren())
            if(Objects.requireNonNull(s.getValue()).toString().equals(code)) return true;
        return false;
    }
}