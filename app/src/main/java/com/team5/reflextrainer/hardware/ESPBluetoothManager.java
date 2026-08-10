package com.team5.reflextrainer.hardware;

import static androidx.core.app.PendingIntentCompat.send;

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
                    new Thread(this::listenLoop, "BT-Listen-Thread").start();
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
        byte[] readBuf = new byte[64];
        java.io.ByteArrayOutputStream frameBuffer = new java.io.ByteArrayOutputStream();

        while (running) {
            try {
                int bytesRead = inputStream.read(readBuf);
                if (bytesRead <= 0) break;

                frameBuffer.write(readBuf, 0, bytesRead);
                byte[] all = frameBuffer.toByteArray();

                int consumed = 0;
                while (consumed < all.length) {
                    if (all[consumed] != SensorMessage.START_BYTE) {
                        consumed++;
                        continue;
                    }
                    if (consumed + 1 >= all.length) break;

                    int msgType = all[consumed + 1] & 0xFF;
                    int frameLen = (msgType == (SensorMessage.RESP_ACK & 0xFF)
                            || msgType == (SensorMessage.RESP_SIMON_PROGRESS & 0xFF)) ? 4 : 6;

                    if (consumed + frameLen > all.length) break;

                    byte[] frame = new byte[frameLen];
                    System.arraycopy(all, consumed, frame, 0, frameLen);
                    SensorMessage msg = SensorMessage.parse(frame);
                    if (msg != null && listener != null) {
                        listener.onMessage(msg);
                    }
                    consumed += frameLen;
                }


                byte[] remaining = java.util.Arrays.copyOfRange(all, consumed, all.length);
                frameBuffer.reset();
                frameBuffer.write(remaining);

            } catch (IOException e) {
                Log.w(TAG, "Read failed", e);
                break;
            }
        }
        running = false;
        if (listener != null) listener.onConnectionChanged(false, false);
    }

    public void send(byte[] frame){
        if(outputStream == null) {
            Log.e("BT_DEBUG", "outputStream is null, cannot send!"); // TEMP DEBUG
            return;
        }
        executor.execute(() ->{
            try {
                outputStream.write(frame);
                Log.e("BT_DEBUG", "Frame written successfully, " + frame.length + " bytes"); // TEMP DEBUG
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

    public void sendResetBlocking() {
        if (outputStream == null) return;
        try {
            outputStream.write(SensorMessage.buildReset());
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Reset write failed", e);
        }
    }

    public void sendRhythmModeOn() {
        send(SensorMessage.buildRhythmModeOn());
    }

    public void sendRhythmModeOff() {
        send(SensorMessage.buildRhythmModeOff());
    }

    public void sendStartSimon(byte[] sequence) {
        send(SensorMessage.buildStartSimon(sequence));
    }
}

