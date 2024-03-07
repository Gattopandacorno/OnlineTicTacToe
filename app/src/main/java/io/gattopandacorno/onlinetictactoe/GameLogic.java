package io.gattopandacorno.onlinetictactoe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class GameLogic extends AppCompatActivity
{

    // Store all the combination to win; horizontals, verticals, diagonals
    private final int[][] winComb = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                                    {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                                    {0, 4, 8}, {2, 4, 6}};

    // Store the situation in the game; 0 = not used, 1 = x, 2 = o
    private final int[] grid = {0, 0, 0, 0, 0, 0, 0, 0, 0};

    private boolean turn = true;

    private ImageView[] cells;

    private int numPlayer;
    private BluetoothReceiver bReceiver;
    private BluetoothAdapter bAdapter;


    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables", "ClickableViewAccessibility", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboard);
        
        TextView tp1 = findViewById(R.id.tp1), tp2 = findViewById(R.id.tp2);
        findViewById(R.id.t1).setVisibility(View.VISIBLE);
        findViewById(R.id.t2).setVisibility(View.INVISIBLE);

        cells = new ImageView[] {findViewById(R.id.i0), findViewById(R.id.i1), findViewById(R.id.i2),
                findViewById(R.id.i3), findViewById(R.id.i4), findViewById(R.id.i5),
                findViewById(R.id.i6), findViewById(R.id.i7), findViewById(R.id.i8)};


        // Add specific action that should be detected by the IntentFilter, useful for the online game
        IntentFilter fil = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        fil.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        fil.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        fil.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        bReceiver = new BluetoothReceiver(this);
        registerReceiver(bReceiver, fil);

        bAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bAdapter.setName("HT");

        // If the game mode is local
        if (!getIntent().getBooleanExtra("online", false))
        {
            tp1.setText(getIntent().getStringExtra("playerName1"));
            tp2.setText(getIntent().getStringExtra("playerName2"));

            for (int i = 0; i < cells.length; i++)
            {
                int j = i;
                cells[i].setOnTouchListener((v, event) -> {

                    // Only if the image/cell is touched AND it is not occupied
                    if (event.getAction() == MotionEvent.ACTION_DOWN && grid[j] == 0)
                    {
                        if (turn)
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


                        // Create a thread to control if somebody win
                        // Thread cannot be used because after calling Win it shows an alert dialog
                        runOnUiThread(() -> AlertWin(cells, grid));
                        
                        turn = !turn;
                        return true;
                    }

                    return false;
                });
            }
        }

        // If the game is online
        else
        {
            tp1.setText(getIntent().getStringExtra("playerName1"));
            tp2.setText(getIntent().getStringExtra("playerName2"));
            Drawable d;
            LocalBroadcastManager.getInstance(this).registerReceiver(getMsg , new IntentFilter("sendmsg"));

            // Ask if the device can be discoverable so it can be found and then paired/connected with the other player
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 800);

            startActivity(discoverableIntent);
            bReceiver.bConnection.start();

            if(!getIntent().getBooleanExtra("host", false))
            {
                tp2.setText(getIntent().getStringExtra("playerName2"));
                d = getDrawable(R.drawable.o);
                numPlayer = 2;
                turn = false;
                bAdapter.startDiscovery();
            }

            else
            {
                d = getDrawable(R.drawable.x);
                numPlayer = 1;
            }

            for (int i = 0; i < cells.length; i++)
            {
                int j = i;
                cells[i].setOnTouchListener((v, event) -> {

                    // Only if the image/cell is touched AND it is not occupied
                    if (event.getAction() == MotionEvent.ACTION_DOWN && grid[j] == 0 && turn)
                    {
                        cells[j].setImageDrawable(d);
                        grid[j] = numPlayer;

                        turnVisibility(numPlayer);

                        bReceiver.bConnection.write(String.valueOf(j).getBytes());

                        turn = !turn;

                        // Create a thread to control if somebody win
                        // Thread cannot be used because after calling Win it shows an alert dialog
                        runOnUiThread(() -> AlertWin(cells, grid));

                        return true;
                    }

                    return false;
                });
            }
        }




        // Setting click listener for when reset/play again button is clicked
        findViewById(R.id.reset).setOnClickListener(v -> {
            reset(cells);
            if(getIntent().getBooleanExtra("online", false)) bReceiver.bConnection.write("again".getBytes());

        });

        // Setting click listener when return to home button is clicked
        findViewById(R.id.home).setOnClickListener(v -> returnHome());

        // Avoids the slide of the win streak seekbar by the users
        findViewById(R.id.seekBar).setOnTouchListener((v, event) -> true);


        // If the Back button is pressed it shows an Alert, if ok is pressed too it take the players to the MainActivity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed()
            {
                new AlertDialog.Builder(GameLogic.this)
                        .setMessage("Are you sure you want to leave the game?")
                        .setNegativeButton("ok", (dialog, which) -> returnHome()).show();
            }
        });
    }

    BroadcastReceiver getMsg = new BroadcastReceiver() {
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String msg = intent.getStringExtra("msg");

            if (msg == null || msg.equals(" ")) return;
            else if (msg.equals("disconnect")) returnHome();
            else if(msg.matches("^(\\d)+$"))
            {
                turn    = true;
                int i   = Integer.parseInt(msg);
                grid[i] = invertPlayer(numPlayer);
                if(invertPlayer(numPlayer) == 1) cells[i].setImageDrawable(getDrawable(R.drawable.x));
                else cells[i].setImageDrawable(getDrawable(R.drawable.o));

                runOnUiThread(() -> AlertWin(cells, grid));

                turnVisibility(invertPlayer(numPlayer));
            }
            else if(msg.equals("again")) reset(cells);
            else if(msg.equals("start"))
                bReceiver.bConnection.write(getIntent().getStringExtra("playerName"+ numPlayer).getBytes());
            else
            {
                TextView tmp;
                if(numPlayer==1){
                    tmp = findViewById(R.id.tp2);
                    getIntent().putExtra("playerName2", msg);
                }
                else
                {
                    tmp = findViewById(R.id.tp1);
                    getIntent().putExtra("playerName2", msg);
                }

                tmp.setText(msg);
            }
        }
    };

    /**
     * Control if there is a winner in the game, this is done with the winning combination matrix
     * If in the grid there is a winning combination it returns the value of the winner; 1 = x, 2 = o
     * Otherwise it returns 0
     *
     * @param grid Is the numeric representation of the game's situation.
     *             It is used to better control when a player is winning or not.
     */
    private int Win(int[] grid) // TODO: find why some combination don't works
    {
        for (int[] ints : winComb)
            if (grid[ints[0]] == grid[ints[1]] && grid[ints[1]] == grid[ints[2]])
                return grid[ints[0]];

        return 0;
    }

    /**
     * The reset function is not only used when reset button is clicked
     * but also when the play again 'button' is shown  after a win in the alert dialog
     *
     * @param c Is an array of images, represents the board game.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void reset(ImageView[] c)
    {
        if(!getIntent().getBooleanExtra("online", false)) turn = true;
        else turn = (numPlayer==1);

        findViewById(R.id.t1).setVisibility(View.VISIBLE);
        findViewById(R.id.t2).setVisibility(View.INVISIBLE);

        for (int i = 0; i < 9; i++)
        {
            c[i].setImageDrawable(getDrawable(R.drawable.card));
            grid[i] = 0;
        }
    }

    /**
     * If a player wins this function shows an alert message with the winner's name
     *
     * @param c Is an array of images, represents the board game.
     *
     * @param grid Is the numeric representation of the game's situation.
     *             It is used to better control when a player is winning or not.
     * */
    private void AlertWin(ImageView[] c, int[] grid)
    {
        int w = Win(grid);

        SeekBar sb = findViewById(R.id.seekBar); // TODO: find out wat makes the alert not popping up

        if (w == 1) // If the winner is the one with the X
        {
            new AlertDialog.Builder(this).
                    setMessage(getIntent().getStringExtra("playerName1") + " WON THE GAME").
                    setPositiveButton("play again", (dialog, which) -> {
                        reset(c);
                        if(getIntent().getBooleanExtra("online", false))
                            bReceiver.bConnection.write("again".getBytes());
                    })
                    .show();

            runOnUiThread(() -> sb.setProgress(sb.getProgress() + 1));
        }


        else if (w == 2) // If the winner is the one with the O
        {
            new AlertDialog.Builder(this).
                    setMessage(getIntent().getStringExtra("playerName2") + " WON THE GAME").
                    setPositiveButton("play again", (dialog, which) -> {
                        reset(c);
                        if(getIntent().getBooleanExtra("online", false))
                            bReceiver.bConnection.write("again".getBytes());
                    })
                    .show();
            runOnUiThread(() -> sb.setProgress(sb.getProgress()-1));
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(getIntent().getBooleanExtra("online", false))
        {
            bReceiver.bConnection.disconnect();
            unregisterReceiver(bReceiver);
            if(bAdapter != null) bAdapter.disable();
            bAdapter = null;
        }
    }

    /**
     * This method was created to not repeat the same code for the home button and the back pressed.
     * As the name suggest start the activity to return in the Home view (MainActivity).
     */
    private void returnHome()
    {
        Intent i = new Intent(this, MainActivity.class);

        startActivity(i); //Return to home view

        finish();
    }


    private int invertPlayer(int num)
    {
        if(num == 1) return 2;
        else if( num == 2) return 1;
        return num;
    }

    private void turnVisibility(int num)
    {
        if(num == 1)
        {
            findViewById(R.id.t2).setVisibility(View.VISIBLE);
            findViewById(R.id.t1).setVisibility(View.INVISIBLE);
        }
        else
        {
            findViewById(R.id.t1).setVisibility(View.VISIBLE);
            findViewById(R.id.t2).setVisibility(View.INVISIBLE);
        }
    }
}
