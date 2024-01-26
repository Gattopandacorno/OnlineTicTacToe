package io.gattopandacorno.onlinetictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting click listener for when Play local game button is clicked
        findViewById(R.id.localButton).setOnClickListener(v->{
            Intent i = new Intent(this, Login.class);

            i.putExtra("online", false);

            startActivity(i); //Start the activity with the name form before playing
            finish();
        });

        //Setting click listener for when Play multiplayer game button is clicked
        findViewById(R.id.multiButton).setOnClickListener(v -> {
            Intent i = new Intent(this, Login.class);

            i.putExtra("online", true);

            startActivity(i); //Start the activity with the name+join/host form before playing
            finish();
        });

    }
}