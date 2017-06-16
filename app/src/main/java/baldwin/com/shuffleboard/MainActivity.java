package baldwin.com.shuffleboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.suitebuilder.TestMethod;
import android.util.Log;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BluetoothSocket mSocket;
    BluetoothDevice mDevice = null;

    //For bluetooth
    final byte delimiter = 33;
    int readBufferPosition = 0;

    int p1FinalScore = 0;
    int p2FinalScore = 0;

    //Holds score for current player and turn
    int currentRoundScore = 0;
    int shotCounter = 0;
    int currentRound = 0;
    boolean p1Turn = true;

    //setup Textviews
    TextView tv_p1_final;
    TextView tv_p2_final;
    TextView[] tvPlayer1RoundScores = new TextView[5];
    TextView[] tvPlayer2RoundScores = new TextView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tvTest = (TextView) findViewById(R.id.tv_test);

        //Setup player1 scores
        tv_p1_final = (TextView) findViewById(R.id.tv_p1_final);
        final TextView tv_p1 = (TextView) findViewById(R.id.tv_p1);
        tv_p1.setBackgroundColor(Color.RED);

        tvPlayer1RoundScores[0] = (TextView) findViewById(R.id.tv_p1_round1);
        tvPlayer1RoundScores[1] = (TextView) findViewById(R.id.tv_p1_round2);
        tvPlayer1RoundScores[2] = (TextView) findViewById(R.id.tv_p1_round3);
        tvPlayer1RoundScores[3] = (TextView) findViewById(R.id.tv_p1_round4);
        tvPlayer1RoundScores[4] = (TextView) findViewById(R.id.tv_p1_round5);

        //Setup player2 scores
        tv_p2_final = (TextView) findViewById(R.id.tv_p2_final);
        final TextView tv_p2 = (TextView) findViewById(R.id.tv_p2);

        tvPlayer2RoundScores[0] = (TextView) findViewById(R.id.tv_p2_round1);
        tvPlayer2RoundScores[1] = (TextView) findViewById(R.id.tv_p2_round2);
        tvPlayer2RoundScores[2] = (TextView) findViewById(R.id.tv_p2_round3);
        tvPlayer2RoundScores[3] = (TextView) findViewById(R.id.tv_p2_round4);
        tvPlayer2RoundScores[4] = (TextView) findViewById(R.id.tv_p2_round5);

        //Setup round headers
        final TextView[] tvRoundHeaders = new TextView[6];
        tvRoundHeaders[0] = (TextView) findViewById(R.id.tv_round1);
        tvRoundHeaders[0].setBackgroundColor(Color.RED);
        tvRoundHeaders[1] = (TextView) findViewById(R.id.tv_round2);
        tvRoundHeaders[2] = (TextView) findViewById(R.id.tv_round3);
        tvRoundHeaders[3] = (TextView) findViewById(R.id.tv_round4);
        tvRoundHeaders[4] = (TextView) findViewById(R.id.tv_round5);
        tvRoundHeaders[5] = (TextView) findViewById(R.id.tv_final);

        //Shot counter for current player
        final TextView tvShotsTaken = (TextView) findViewById(R.id.tv_shots_taken);

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
                                final int sensorNumber = Integer.parseInt(data);
                                final int shotScore;
                                Log.e("Read data", data);
                                //The variable data now contains our full command
                                handler.post(new Runnable() {
                                    public void run() {
                                        //TODO tun updates to views and scores based on data
                                        tvTest.setText(data);

                                        Log.e("workerthread current=", Integer.toString(currentRound));
                                        if (sensorNumber != 5) {
                                            //A shot was scored
                                            if (sensorNumber == 0) {
                                                calculateScore(3);
                                            }
                                            if (sensorNumber == 1) {
                                                calculateScore(5);
                                            }
                                            if (sensorNumber == 2) {
                                                calculateScore(6);
                                            }
                                            if (sensorNumber == 3) {
                                                calculateScore(4);
                                            }
                                        }

                                        //Shot is taken increase shot counter
                                        if (sensorNumber == 5) {
                                            shotCounter += 1;

                                            if (shotCounter > 4) {
                                                //players turn is over
                                                shotCounter = 0;


                                                if (p1Turn) {
                                                    p1Turn = false;
                                                    tv_p1.setBackgroundColor(Color.WHITE);
                                                    tv_p2.setBackgroundColor(Color.RED);

                                                } else {
                                                    //player 2 turn is over
                                                    p1Turn = true;
                                                    tv_p1.setBackgroundColor(Color.RED);
                                                    tv_p2.setBackgroundColor(Color.WHITE);
                                                    //Current round is over
                                                    currentRound += 1;
                                                    if (currentRound > 4) {
                                                        //Game is over
                                                        tvTest.setText("Game is over");
                                                    }
                                                    tvRoundHeaders[currentRound - 1].setBackgroundColor(Color.WHITE);
                                                    tvRoundHeaders[currentRound].setBackgroundColor(Color.RED);
                                                }
                                            }
                                            tvShotsTaken.setText(shotCounter + 1 + "/5");

                                        }
                                    }
                                });
                                break;

                            }

                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            }
        }//end of worker thread

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-06")) //Note, you will need to change this to match the name of your device
                {
                    Log.e("Shuffleboard", device.getName());
                    mDevice = device;
                    break;
                }
            }
        }
        (new Thread(new workerThread())).start();
    }//end of onCreate

    public void calculateScore(int score) {
        //Take int and add to score
        //Todo currentRound is always 5
        if (p1Turn) {
            p1FinalScore += score;
            tv_p1_final.setText(Integer.toString(p1FinalScore));
            currentRoundScore += score;
            Log.e("calculateScore round =", Integer.toString(currentRound));
            tvPlayer1RoundScores[currentRound].setText(Integer.toString(currentRoundScore));

        } else {
            p2FinalScore += score;
            tv_p2_final.setText(Integer.toString(p2FinalScore));
            currentRoundScore += score;
            tvPlayer2RoundScores[currentRound].setText(Integer.toString(currentRoundScore));
        }

    }
}
