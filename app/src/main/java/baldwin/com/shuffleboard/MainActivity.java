package baldwin.com.shuffleboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BluetoothSocket mSocket;
    BluetoothDevice mDevice = null;

    //For bluetooth
    int readBufferPosition = 0;

    int p1FinalScore = 0;
    int p2FinalScore = 0;

    //Holds score for current player and turn
    int currentRoundScore = 0;
    //How many shots the current plater has taken
    int shotCounter = 0;
    int currentRound = 0;
    boolean p1Turn = true;
    //only true when first shot is taken, helps with logic
    boolean firstShot = true;
    //The sensor number that was red and sent by the bluetooth device
    int sensorNumber;

    //setup Textviews Old
    TextView tvP1Score;
    TextView tvP1;
    TextView tvP2Score;
    TextView tvP2;

    TextView tvShotsTaken;
    TextView tvRounds;
    TextView tvShotHistory;

    public String SHOT_HISTORY_TEXT_KEY = "shotHistoryKey";
    public String ROUNDS_KEY ="roundsKey";
    public String SHOTS_TAKEN_KEY = "shotTakenKey";
    public String P1_SCORE_KEY = "P1ScoreKey";
    public String P2_SCORE_KEY = "P2ScoreKey";
    public String SHOT_COUNTER_KEY = "shotCounterKey";
    public String CURRENT_ROUND_KEY = "currentRoundKey";
    public String CURRENT_ROUND_SCORE_KEY = "currentRoundScoreKey";
    public String P1_FINAL_SCORE_KEY = "P1FinalScoreKey";
    public String P2_FINAL_SCORE_KEY = "P2FinalScoreKey";
    public String FIRST_SHOT_KEY = "firstShotKey";
    public String P1_TURN_KEY = "p1TurnKey";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        //tvTest = (TextView) findViewById(R.id.tv_test);

        //Setup player1 scores
        tvP1Score = (TextView) findViewById(R.id.tv_p1_score);
        tvP1 = (TextView) findViewById(R.id.tv_p1);
        tvP1.setBackgroundColor(Color.RED);

        //Setup player2 scores
        tvP2Score = (TextView) findViewById(R.id.tv_p2_score);
        tvP2 = (TextView) findViewById(R.id.tv_p2);

        //Shot counter for current player
        tvShotsTaken = (TextView) findViewById(R.id.tv_shot_counter);
        //Round counter
        tvRounds = (TextView) findViewById(R.id.tv_round_counter);
        //Shot history display
        tvShotHistory = (TextView) findViewById(R.id.tv_shot_history);

        /*tvPlayer1RoundScores[0] = (TextView) findViewById(R.id.tv_p1_round1);
        tvPlayer1RoundScores[1] = (TextView) findViewById(R.id.tv_p1_round2);
        tvPlayer1RoundScores[2] = (TextView) findViewById(R.id.tv_p1_round3);
        tvPlayer1RoundScores[3] = (TextView) findViewById(R.id.tv_p1_round4);
        tvPlayer1RoundScores[4] = (TextView) findViewById(R.id.tv_p1_round5);*/

        /*tvPlayer2RoundScores[0] = (TextView) findViewById(R.id.tv_p2_round1);
        tvPlayer2RoundScores[1] = (TextView) findViewById(R.id.tv_p2_round2);
        tvPlayer2RoundScores[2] = (TextView) findViewById(R.id.tv_p2_round3);
        tvPlayer2RoundScores[3] = (TextView) findViewById(R.id.tv_p2_round4);
        tvPlayer2RoundScores[4] = (TextView) findViewById(R.id.tv_p2_round5);*/

        //Setup round headers

        /*tvRoundHeaders[0] = (TextView) findViewById(R.id.tv_round1);
        tvRoundHeaders[0].setBackgroundColor(Color.RED);
        tvRoundHeaders[1] = (TextView) findViewById(R.id.tv_round2);
        tvRoundHeaders[2] = (TextView) findViewById(R.id.tv_round3);
        tvRoundHeaders[3] = (TextView) findViewById(R.id.tv_round4);
        tvRoundHeaders[4] = (TextView) findViewById(R.id.tv_round5);
        tvRoundHeaders[5] = (TextView) findViewById(R.id.tv_final);*/

        //Bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Is this needed?
        final Handler handler = new Handler();

        //Bluetooth thread
        final class workerThread implements Runnable {

            public workerThread() {
                //TODO implement setup get corret UUID for bluetooth device
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                try {

                    mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                    if (!mSocket.isConnected()) {
                        mSocket.connect();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void run() {
                //TODO setup read from bluetooth
                while (!Thread.currentThread().isInterrupted()) {
                    int bytesAvailable;

                    try {

                        //Get inputStream for bluetooth
                        final InputStream mInputStream;
                        mInputStream = mSocket.getInputStream();
                        bytesAvailable = mInputStream.available();
                        if (bytesAvailable > 0) {

                            byte[] packetBytes = new byte[bytesAvailable];
                            Log.e("recv bt", "bytes available");
                            byte[] readBuffer = new byte[1024];
                            mInputStream.read(packetBytes);

                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];


                                readBuffer[readBufferPosition++] = b;
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;

                                try {
                                    sensorNumber = Integer.parseInt(data);
                                } catch (Exception e) {
                                    //Fake data stored to sensorNumber to get rid of bad data
                                    Log.e("MainActivity runable", "Bluetooth read not an integer");
                                    sensorNumber = 6;
                                }

                                Log.e("Read data", data);
                                //The variable data now contains our full command
                                handler.post(new Runnable() {
                                    public void run() {

                                        //tvTest.setText(data);

                                        Log.e("workerthread current=", Integer.toString(currentRound));
                                        if (sensorNumber != 5) {
                                            //A shot was scored
                                            if (sensorNumber == 1) {
                                                calculateScore(3);
                                            }
                                            if (sensorNumber == 2) {
                                                calculateScore(5);
                                            }
                                            if (sensorNumber == 3) {
                                                calculateScore(6);
                                            }
                                            if (sensorNumber == 4) {
                                                calculateScore(4);
                                            }
                                        }


                                        if (sensorNumber == 5) {

                                            //Shot is taken increase shot counter
                                            shotCounter += 1;

                                            //Advances the players turn on shot
                                            // counter 0 to count if score was made last turn
                                            if (shotCounter == 1) {
                                                if (p1Turn) {
                                                    if (!firstShot) {
                                                        p1Turn = false;
                                                        currentRoundScore = 0;
                                                    }
                                                } else {
                                                    p1Turn = true;
                                                    //Current round is over

                                                    currentRound += 1;
                                                    currentRoundScore = 0;
                                                    /*
                                                    if (currentRound > 4) {
                                                        //Game is over
                                                        tvTest.setText("Game is over");
                                                    }*/
                                                }
                                            }
                                            if (shotCounter > 1) {
                                                firstShot = false;
                                            }

                                            if (shotCounter == 5) {
                                                //players turn is over
                                                shotCounter = 0;


                                                if (p1Turn) {

                                                    tvP1.setBackgroundColor(Color.WHITE);
                                                    tvP2.setBackgroundColor(Color.RED);

                                                } else {
                                                    //player 2 turn is over

                                                    tvP1.setBackgroundColor(Color.RED);
                                                    tvP2.setBackgroundColor(Color.WHITE);
                                                    tvRounds.setText("Round " + (currentRound + 2) + "/5");
                                                    /*tvRoundHeaders[currentRound].setBackgroundColor(Color.WHITE);
                                                    tvRoundHeaders[currentRound + 1].setBackgroundColor(Color.RED);
                                                    */
                                                    if (currentRound == 4) {
                                                        tvShotHistory.setText("Game is over \n" + tvShotHistory.getText());
                                                    }
                                                }
                                            }
                                            tvShotsTaken.setText("Shot " + shotCounter + "/5");

                                        }
                                    }
                                });
                                break;

                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        }//end of worker thread

        if (!mBluetoothAdapter.isEnabled())

        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        //Todo take out this hardcoded address
        //mDevice = mBluetoothAdapter.getRemoteDevice("20:16:12:12:83:14");
        mDevice = mBluetoothAdapter.getRemoteDevice("20:16:03:25:46:93");   //currently installed on shuffleboard
        //Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
/*
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                pairedDevices.
                if (device.getName().equals("HC-06")) //Note, you will need to change this to match the name of your device
                {
                    Log.e("Shuffleboard", device.getName());
                    mDevice = device;
                    break;
                }
            }
        }*/
        (new Thread(new workerThread())).start();

    }//end of onCreate


    @Override
    protected void onResume() {
        //TODO implement setup get corret UUID for bluetooth device
       /* UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {

            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mSocket.isConnected()) {
                mSocket.connect();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!mBluetoothAdapter.isEnabled())

        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
*/
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_new_game:

                p1Turn = true;
                firstShot = true;
                p1FinalScore = 0;
                p2FinalScore = 0;
                currentRoundScore = 0;
                shotCounter = 0;
                // tvRoundHeaders[currentRound].setBackgroundColor(Color.WHITE);
                //tvRoundHeaders[0].setBackgroundColor(Color.RED);
                tvP1.setBackgroundColor(Color.RED);
                tvP2.setBackgroundColor(Color.WHITE);
                currentRound = 0;
                tvRounds.setText("Round 1/5");
                tvShotsTaken.setText("0/5");

                //tvTest.setText("");

                for (int i = 0; i < 4; i++) {
                    //tvPlayer1RoundScores[i].setText("0");
                    //tvPlayer2RoundScores[i].setText("0");
                    tvP1Score.setText("0");
                    tvP2Score.setText("0");
                }
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Todo save important variables
        outState.putString(SHOT_HISTORY_TEXT_KEY, tvShotHistory.getText().toString());
        outState.putString(ROUNDS_KEY, tvRounds.getText().toString());
        outState.putString(SHOTS_TAKEN_KEY, tvShotsTaken.getText().toString());
        outState.putString(P1_SCORE_KEY, tvP1Score.getText().toString());
        outState.putString(P2_SCORE_KEY, tvP2Score.getText().toString());

        outState.putInt(SHOT_COUNTER_KEY, shotCounter);
        outState.putInt(CURRENT_ROUND_KEY, currentRound);
        outState.putInt(CURRENT_ROUND_SCORE_KEY, currentRoundScore);
        outState.putInt(P1_FINAL_SCORE_KEY, p1FinalScore);
        outState.putInt(P2_FINAL_SCORE_KEY, p2FinalScore);

        outState.putBoolean(P1_TURN_KEY, p1Turn);
        outState.putBoolean(FIRST_SHOT_KEY, firstShot);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Todo restore important variables and update views
        tvShotHistory.setText(savedInstanceState.getString(SHOT_HISTORY_TEXT_KEY));
        tvRounds.setText(savedInstanceState.getString(ROUNDS_KEY));
        tvShotsTaken.setText(savedInstanceState.getString(SHOTS_TAKEN_KEY));
        tvP1Score.setText(savedInstanceState.getString(P1_SCORE_KEY));
        tvP2Score.setText(savedInstanceState.getString(P2_SCORE_KEY));

        shotCounter = savedInstanceState.getInt(SHOT_COUNTER_KEY);
        currentRound = savedInstanceState.getInt(CURRENT_ROUND_KEY);
        currentRoundScore = savedInstanceState.getInt(CURRENT_ROUND_SCORE_KEY);
        p1FinalScore = savedInstanceState.getInt(P1_FINAL_SCORE_KEY);
        p2FinalScore = savedInstanceState.getInt(P2_FINAL_SCORE_KEY);

        p1Turn = savedInstanceState.getBoolean(P1_TURN_KEY);
        firstShot = savedInstanceState.getBoolean(FIRST_SHOT_KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void calculateScore(int score) {

        if (p1Turn) {
            p1FinalScore += score;
            tvP1Score.setText(Integer.toString(p1FinalScore));
            currentRoundScore += score;
            Log.e("calculateScore round =", Integer.toString(currentRound));
            tvShotHistory.setText("Player 1 scored " + score + " points \n" + tvShotHistory.getText());
            //tvPlayer1RoundScores[currentRound].setText(Integer.toString(currentRoundScore));

        } else {
            p2FinalScore += score;
            tvP2Score.setText(Integer.toString(p2FinalScore));
            currentRoundScore += score;
            tvShotHistory.setText("Player 2 scored " + score + " points \n" + tvShotHistory.getText());
            //tvPlayer2RoundScores[currentRound].setText(Integer.toString(currentRoundScore));
        }

    }
}
