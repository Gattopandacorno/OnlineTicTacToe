package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class GameLogic extends AppCompatActivity
{

    // Store all the combination to win; horizontals, verticals, diagonals
    private final int[][] winComb = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                                     {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                                     {0, 4, 8}, {2, 4, 6}};
    // Store the situation in the game; 0 = not used, 1 = x, 2 = o
    private final int[] grid = {0, 0, 0, 0, 0, 0, 0, 0, 0};

    private boolean turn = true;

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboard);

        ImageView[] cells = {findViewById(R.id.i0), findViewById(R.id.i1), findViewById(R.id.i2),
                            findViewById(R.id.i3), findViewById(R.id.i4), findViewById(R.id.i5),
                            findViewById(R.id.i6), findViewById(R.id.i7), findViewById(R.id.i8)};

        //If the game mode is local
        if(!getIntent().getBooleanExtra("online", false))
        {
            TextView tp1 = findViewById(R.id.tp1), tp2 = findViewById(R.id.tp2);
            tp1.setText(getIntent().getStringExtra("playerName1"));
            tp2.setText(getIntent().getStringExtra("playerName2"));

            findViewById(R.id.t1).setVisibility(View.VISIBLE);
            findViewById(R.id.t2).setVisibility(View.INVISIBLE);

            for(int i=0; i<9; i++)
            {
                int j = i;
                cells[i].setOnTouchListener((v, event) -> {
                    //Only if the image/cell is touched AND it is not occupied
                    if(event.getAction() == MotionEvent.ACTION_DOWN && grid[j]==0)
                    {
                        if(turn)
                        {
                            cells[j].setImageDrawable(getDrawable(R.drawable.x));
                            grid[j] = 1;

                            findViewById(R.id.t2).setVisibility(View.VISIBLE);
                            findViewById(R.id.t1).setVisibility(View.INVISIBLE);
                        }

                        else
                        {
                            cells[j].setImageDrawable(getDrawable(R.drawable.o));
                            grid[j] = 2;

                            findViewById(R.id.t1).setVisibility(View.VISIBLE);
                            findViewById(R.id.t2).setVisibility(View.INVISIBLE);
                        }


                        turn = !turn;
                        return true;
                    }

                    return false;
                });
            }
        }

        //If the game mode is online
        else
        {
            Toast.makeText(this, "online game", Toast.LENGTH_SHORT).show();
        }


        //Setting click listener for when reset/play again button is clicked
        findViewById(R.id.reset).setOnClickListener(v -> reset(cells));

        //Setting click listener when return to home button is clicked
        findViewById(R.id.home).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);

            startActivity(i); //Return to home view called activity_main
            finish();
        });

        //This take to the MainActivity if the back button is pressed
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed()
            {
                Intent i = new Intent(GameLogic.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    /* Control if there is a winner in the game, this is done with the winning combination matrix
     * If in the grid there is a winning combination it returns the value of the winner; 1 = x, 2 = o
     * Otherwise it returns 0
     */
    private int Win()
    {
        for(int i=0; i<9; i++)
            if(grid[winComb[i][0]] == grid[winComb[i][1]]  && grid[winComb[i][1]] == grid[winComb[i][2]])
                return grid[winComb[i][0]];

        return 0;
    } //TODO: Add win count here?

    /* The reset function is not only used when reset button is clicked
     * but also when the play again 'button' is shown  after a win in the alert dialog
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void reset(ImageView[] c)
    {
        turn = true;
        findViewById(R.id.t1).setVisibility(View.VISIBLE);
        findViewById(R.id.t2).setVisibility(View.INVISIBLE);
        for(int i=0; i<9; i++)
        {
            c[i].setImageDrawable(getDrawable(R.drawable.ic_launcher_background));
            grid[i] = 0;
        }
    }

    /* If a player wins this function shows an alert message with the winner's name*/
    private void AlertWin(ImageView[] c)
    {
        int w = Win();

        if(w == 1) // if the winner is th
            new AlertDialog.Builder(this).
                    setMessage(getIntent().getStringExtra("playerName1") + " WON THE GAME").
                    setPositiveButton("play again", (dialog, which) -> reset(c))
                    .show();

        else if (w == 2)
            new AlertDialog.Builder(this).
                    setMessage(getIntent().getStringExtra("playerName2") + " WON THE GAME").
                    setPositiveButton("play again", (dialog, which) -> reset(c))
                    .show();
    }
}