package com.fsoft.btcopter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by Dr. Failov on 09.12.2016.
 */
public class BluetoothArduino {
    String name = "Dr. Failov:Arduino";
    BluetoothSocket socket = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;
    OnStatusMessage onStatusMessage = null;
    Runnable onConnected = null;

    public interface OnStatusMessage{
        void onStatusMessage(String text);
    }


    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = null;
                    if (bluetooth != null) {
                        // Enabled. Work with Bluetooth.
                        if (bluetooth.isEnabled()) {
                            Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
                            if (pairedDevices.size() > 0) {
                                for (BluetoothDevice d : pairedDevices) {
                                    if (d.getName().equals(name)) {
                                        device = d;
                                        break;
                                    }
                                }
                            }
                            if (device != null) {
                                try {
                                    Log.d("EF-BTBee", "Bluetooth Device Found");
                                    //Create a Socket connection: need the server's UUID number of registered
                                    Log.d("EF-BTBee", ">>dev: " + device.getName());
                                    //Инициируем соединение с устройством
                                    Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                                    //clientSocket.connect();
                                    //socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")); //a60f35f0-b93a-11de-8a39-08002009c666
                                    socket = (BluetoothSocket) m.invoke(device, 1);
                                    socket.connect();
                                    Log.d("EF-BTBee", ">>Client connectted");

                                    inputStream = socket.getInputStream();
                                    startReading();
                                    outputStream = socket.getOutputStream();
                                    //outputStream.write("{\"alpha\":20, \"x1\":150, \"x2\":50}".getBytes("UTF-8"));
                                    status("Connected");
                                    if(onConnected != null)
                                        onConnected.run();

                                } catch (Exception e) {
                                    Log.e("EF-BTBee", "", e);
                                    status("Error connecting: " + e.toString());
                                    socket = null;
                                } finally {
//                                    if (socket != null) {
//                                        try {
//                                            Log.d("EF-BTBee", ">>Client Close");
//                                            socket.close();
//                                            //finish();
//                                            return;
//                                        } catch (IOException e) {
//                                            Log.e("EF-BTBee", "", e);
//                                        }
//                                    }
                                }
                            }
                        } else {
                            status("Enable bluetooth!");
//                            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                            startActivityForResult(enableIntent, 223);
                        }
                    }
                }
                catch (Exception e){
                    status("Error get Bluetooth Manager");
                }
            }
        }).start();
    }

    public void send(String text){
        try {
            if (socket != null) {
                outputStream.write(text.getBytes());
                status(">>> " + text);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void startReading(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    while (socket.isConnected()) {
                        String line = bufferedReader.readLine();
                        if (line != null) {
                            status(line);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    status("Error reading: " + e.toString());
                }
            }
        }).start();
    }
    public void close(){
        try {
            socket.close();
        }catch (Exception e){}

    }
    public boolean isConnected(){
        if(socket == null)
            return false;
        return socket.isConnected();
    }

    private void status(String text) {
        if(onStatusMessage != null)
            onStatusMessage.onStatusMessage(text);
    }

    public void setOnStatusMessage(OnStatusMessage onStatusMessage) {
        this.onStatusMessage = onStatusMessage;
    }

    public void setOnConnected(Runnable onConnected) {
        this.onConnected = onConnected;
    }
}
