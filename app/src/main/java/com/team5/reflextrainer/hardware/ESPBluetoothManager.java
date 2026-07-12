package com.team5.reflextrainer.hardware;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
public class ESPBluetoothManager {

    private static final String TAG = "BluetoothManager";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String DEVICE_NAME = "ReflexTrainer";

    private static ESPBluetoothManager instance;

    public interface Listener {
        void onConnectionChanged(boolean connected, boolean connecting);
        void onMessage(SensorMessage message);
    }
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    private Listener listener;

    private ESPBluetoothManager() {}

    public static synchronized ESPBluetoothManager getInstance(){
        if (instance == null) instance = new ESPBluetoothManager();
        return instance;

    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isConnected(){
        return running && socket != null;
    }

    @SuppressLint("MissingPermission")
    public void connect(BluetoothAdapter adapter) {
        if(isConnected()) return;
        BluetoothDevice device = findPairedDevice(adapter);
        if (device == null) {
            if (listener != null) listener.onConnectionChanged(false, false);
            return;
        }
        if (listener != null) listener.onConnectionChanged(false,true);

        executor.execute(()->{
            BluetoothSocket connectedSocket = null;
            int attempt = 0;

            while (connectedSocket == null && attempt < 5){
                try {
                    BluetoothSocket s = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    s.connect();
                    connectedSocket = s;
                }   catch (IOException e) {
                    Log.w(TAG, "Connect attempt " + (attempt + 1) + "failed", e);
                    attempt++;
                    try {
                        Thread.sleep(1000L * attempt);
                    }
                    catch (InterruptedException ignored) {}



                }

            }

            if (connectedSocket != null) {
                try {
                    socket = connectedSocket;
                    inputStream = connectedSocket.getInputStream();
                    outputStream = connectedSocket.getOutputStream();
                    running = true;
                    if (listener != null) listener.onConnectionChanged(true,false);
                    listenLoop();
                }   catch (IOException e) {
                    Log.e(TAG, "Failed to open streams", e);
                    if (listener != null) listener.onConnectionChanged(false, false);
                }
                }else{
                if (listener != null) listener.onConnectionChanged(false,false);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice findPairedDevice(BluetoothAdapter adapter) {
        for (BluetoothDevice device : adapter.getBondedDevices()){
            if (DEVICE_NAME.equals(device.getName())) return device;
        }
        return null;
    }

    private void listenLoop() {
        byte[] buffer = new byte[16];
        while(running){
            try {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead <= 0) break;

                byte[] frame = new byte[bytesRead];
                System.arraycopy(buffer, 0, frame, 0, bytesRead);

                SensorMessage msg = SensorMessage.parse(frame);
                if (msg != null && listener != null) {
                    listener.onMessage(msg);
                }
            }catch(IOException e){
                Log.w(TAG, "READ failed, connectiom dropped", e);
                break;
            }
        }
        running = false;
        if(listener != null) listener.onConnectionChanged(false, false);
    }

    public void send(byte[] frame){
        if(outputStream == null) return;
        executor.execute(() ->{
            try {
                outputStream.write(frame);
            }catch (IOException e) {
                Log.e(TAG, "Write failed", e);
            }
        });
    }

    public void sendStartChallenge(byte targetId, int timeoutMs){
        send(SensorMessage.buildChallenge(SensorMessage.MSG_START_CHALLENGE, targetId, timeoutMs));

    }

    public void sendReset() {
        send(SensorMessage.buildReset());
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        }catch (IOException e){
            Log.w(TAG, "Error closing socket", e);

        }
    }

}

