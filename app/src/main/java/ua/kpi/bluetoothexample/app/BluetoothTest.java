package ua.kpi.bluetoothexample.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class BluetoothTest extends Activity
{
    TextView myLabel;
    EditText myTextbox;
    TextView textView;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    int distance;
    short azimuth;
    short inclination;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (EditText)findViewById(R.id.entry);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText("Test");

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
                textView.setText("");
            }
        });

        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    byte b[] = {48}; // calib mode off
                    //byte b[] = {56,0,120};
                    sendData(b);
                }
                catch (IOException ex) { }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
    }

    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("DistoX"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mBluetoothAdapter.cancelDiscovery();
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        Log.d("OK","OK");

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
    }

    /*void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[8192];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            final byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                readBuffer[readBufferPosition++] = b;
                            }

                            *//*if(bytesAvailable > 8) {
                                int oldType, oldDist, oldAzi, oldIncl;
                                byte type = readBuffer[0];
                                if((type & 0x3F) == 1) {
                                    distance = readBuffer[1] + (readBuffer[2] << 8) + ((type & 0x40) << 10);
                                    azimuth = (short)(readBuffer[3] + (readBuffer[4] << 8));
                                    inclination = (short)(readBuffer[5] + (readBuffer[6] << 8));
                                    sendData((byte) (type & 0x80 | 0x55)); // send acknowledge byte
                                    Log.v("B dist", Integer.toString(distance));
                                    Log.v("B azi", Integer.toString(azimuth));
                                    Log.v("B incl", Integer.toString(inclination));
                                    *//**//*if (type != oldType || distance != oldDist || azimuth != oldAzi || inclination != oldIncl) { // valid data
                                        Store(distance, azimuth, inclination); // store new data
                                        oldType = type;
                                        oldDist = distance;
                                        oldAzi = azimuth;
                                        oldIncl = inclination;
                                    }*//**//*
                                    stopWorker = true;
                                }
                            }*//*

                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }*/

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[8192];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            final byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                readBuffer[readBufferPosition++] = b;
                            }

                            /*if(bytesAvailable > 8) {
                                int oldType, oldDist, oldAzi, oldIncl;
                                byte type = readBuffer[0];
                                if((type & 0x3F) == 1) {
                                    distance = readBuffer[1] + (readBuffer[2] << 8) + ((type & 0x40) << 10);
                                    azimuth = (short)(readBuffer[3] + (readBuffer[4] << 8));
                                    inclination = (short)(readBuffer[5] + (readBuffer[6] << 8));
                                    sendData((byte) (type & 0x80 | 0x55)); // send acknowledge byte
                                    Log.v("B dist", Integer.toString(distance));
                                    Log.v("B azi", Integer.toString(azimuth));
                                    Log.v("B incl", Integer.toString(inclination));
                                    *//*if (type != oldType || distance != oldDist || azimuth != oldAzi || inclination != oldIncl) { // valid data
                                        Store(distance, azimuth, inclination); // store new data
                                        oldType = type;
                                        oldDist = distance;
                                        oldAzi = azimuth;
                                        oldIncl = inclination;
                                    }*//*
                                    stopWorker = true;
                                }
                            }*/

                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData(byte[] b) throws IOException
    {
        //String msg = myTextbox.getText().toString();
        //msg += "\n";
        mmOutputStream.write(b);
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();



        myLabel.setText("Bluetooth Closed\n");
        textView.append("ReadBuffer\n");
        textView.append("ReadBuffer len " + readBuffer.length + "\n");
        for (int i = 0; i < 123; i++) {
            if(i % 8 == 0) {
                textView.append("###################\n");
            }
            String s1 = String.format("%8s", Integer.toBinaryString(readBuffer[i] & 0xFF)).replace(' ', '0');
            Byte b = new Byte(readBuffer[i]);
            textView.append(s1 + "  " + b.intValue() + "\n");
        }

        byte type = readBuffer[0];
        if((type & 0x3F) == 1) {
            distance = readBuffer[1] + (readBuffer[2] << 8) + ((type & 0x40) << 10);
            azimuth = (short) (readBuffer[3] + (readBuffer[4] << 8));
            inclination = (short) (readBuffer[5] + (readBuffer[6] << 8));
        }

        textView.append("type " + type);
        textView.append("distance " + distance + "\n");
        textView.append("azimuth " + azimuth + "\n");
        textView.append("inclination " + inclination + "\n");

        textView.append("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        byte[] test = {1, 49, 3, -17, 114, -51, 19, -2};
        //byte[] test = {1, 120, 1, -23, -124, 29, 1, 2};
        Data data = new Data(test);

        textView.append("type " + data.getType() + "\n");
        textView.append("distance " + data.getDistance() + "\n");
        textView.append("azimuth " + data.getAzimuth() + "\n");
        textView.append("inclination " + data.getInclination() + "\n");
        textView.append("roll " + data.getRoll() + "\n");

        textView.append("000000000000000000000000000000\n");


    }
}
