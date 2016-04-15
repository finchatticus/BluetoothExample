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
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
    byte[] input;
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
        input = new byte[8192];
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
                                input[readBufferPosition++] = b;
                            }

                            *//*if(bytesAvailable > 8) {
                                int oldType, oldDist, oldAzi, oldIncl;
                                byte type = input[0];
                                if((type & 0x3F) == 1) {
                                    distance = input[1] + (input[2] << 8) + ((type & 0x40) << 10);
                                    azimuth = (short)(input[3] + (input[4] << 8));
                                    inclination = (short)(input[5] + (input[6] << 8));
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

    List<Data> dataList = new ArrayList<Data>();
    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        boolean cycle = true;
        readBufferPosition = 0;
        input = new byte[8];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        while (true) {
                            if (mmInputStream.available() >= 8) {
                                int oldType = 0, oldDist = 0, oldAzi = 0, oldIncl = 0;  // previous
                                mmInputStream.read(input, 0, 8); // receive 8 bytes
                                byte type = input[0];
                                if ((type & 0x3F) == 1) { // measurement data
                                    int distance = input[1] + (input[2] << 8) + ((type & 0x40) << 10);
                                    short azimuth = (short)(input[3] + (input[4] << 8));
                                    short inclination = (short)(input[5] + (input[6] << 8));
                                    mmOutputStream.write(type & 0x80 | 0x55);  // send acknowledge byte
                                    if (type != oldType || distance != oldDist || azimuth != oldAzi || inclination != oldIncl) { // valid data
                                        dataList.add(new Data(input));
                                        oldType = type;
                                        oldDist = distance;
                                        oldAzi = azimuth;
                                        oldIncl = inclination;
                                    }
                                }
                            }
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
        textView.append("len " + dataList.size() + "\n\n\n");

        for (int i = 0; i < dataList.size(); i++) {
            textView.append("dist " + dataList.get(i).getDistance() + "\n");
            textView.append("azim " + dataList.get(i).getAzimuth()+ "\n");
            textView.append("incl " + dataList.get(i).getInclination() + "\n");
            textView.append("roll " + dataList.get(i).getRoll() + "\n");
            textView.append("dist0 " + dataList.get(i).getDistance0() + "\n");
            textView.append("azim0 " + dataList.get(i).getAzimuth0()+ "\n");
            textView.append("incl0 " + dataList.get(i).getInclination0() + "\n");
            textView.append("roll0 " + dataList.get(i).getRoll0() + "\n");
            for (int j = 0; j < 8; j++) {
                textView.append(j + "  " + dataList.get(i).getBites(j) + "\n");
            }
            textView.append("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        }
    }
}
