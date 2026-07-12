package com.team5.reflextrainer.hardware;

/**
 * Parses and builds the 6-byte messages exchanged with the ESP32 reflex sensor.
 *
 * App  -> ESP32:  [START_BYTE][msgType ][targetId][timeout_lo][timeout_hi][checksum]
 * ESP32 -> App:   [START_BYTE][response][targetId][time_lo   ][time_hi   ][checksum]
 *
 * Protocol defined by Arran (hardware). Constants MUST match the ESP32 firmware.
 */
public class SensorMessage {

    // ---- Framing ----
    public static final byte START_BYTE = (byte) 0xAA;

    // ---- Message types (App -> ESP32) ----
    public static final byte MSG_START_CHALLENGE = 0x01;
    public static final byte MSG_RESET           = 0x02;

    // ---- Targets ----
    public static final byte TARGET_SHAKE_IMU = (byte) 0xFF;

    // ---- Responses (ESP32 -> App) ----
    public static final byte RESP_RESULT = (byte) 0x81;
    public static final byte RESP_ACK    = (byte) 0x82;

    // ---- Outcomes (carried in the response byte's meaning / target field) ----
    public static final byte OUTCOME_CORRECT   = 0x00;
    public static final byte OUTCOME_WRONG_BTN = 0x01;
    public static final byte OUTCOME_TIMEOUT   = 0x02;

    public static final int MESSAGE_LENGTH = 6;

    // ---- Parsed fields ----
    public final byte response;
    public final byte targetId;
    public final int  reactionTimeMs;   // reassembled from lo/hi

    private SensorMessage(byte response, byte targetId, int reactionTimeMs) {
        this.response = response;
        this.targetId = targetId;
        this.reactionTimeMs = reactionTimeMs;
    }

    /**
     * Parse a 6-byte response from the ESP32.
     * @return a SensorMessage, or null if the packet is invalid/corrupted
     *         (HW-3.3 / HW-5: reject bad packets).
     */
    public static SensorMessage parse(byte[] bytes) {
        // wrong length -> reject
        if (bytes == null || bytes.length < 4) return null;
        // wrong start byte -> not a valid frame
        if (bytes[0] != START_BYTE) return null;

        byte response = bytes[1];

        if (response == RESP_ACK && bytes.length == 4) {
            byte targetId = bytes[2];
            byte checksum = bytes[3];
            byte computed = (byte) (response ^ targetId);
            if (computed != checksum) return null;
            return new SensorMessage(response, targetId, -1);
        }
        if (response == RESP_RESULT && bytes.length == 6){
            byte outcome = bytes[2];
            int timeLo = bytes[3];
            int timeHi = bytes[4];
            byte checksum = bytes[5];
            int reactionTime = (timeHi << 8) | timeLo;
            byte computed = (byte) (response ^ outcome ^ bytes[3] ^ bytes[4]);
            if (computed != checksum) return null;
            return new SensorMessage(response,outcome,reactionTime);
        }
     return null;
    }

    /**
     * Build a 6-byte challenge to SEND to the ESP32.
     * @param msgType   e.g. MSG_START_CHALLENGE
     * @param targetId  e.g. TARGET_SHAKE_IMU
     * @param timeoutMs how long the user has to react (0..65535 ms)
     */
    public static byte[] buildChallenge(byte msgType, byte targetId, int timeoutMs) {
        byte timeoutLo = (byte) (timeoutMs & 0xFF);
        byte timeoutHi = (byte) ((timeoutMs >> 8) & 0xFF);
        byte checksum  = computeChecksum(msgType, targetId, timeoutLo, timeoutHi);
        return new byte[]{ START_BYTE, msgType, targetId, timeoutLo, timeoutHi, checksum };
    }

    /** Convenience: a RESET message. */
    public static byte[] buildReset() {
        return buildChallenge(MSG_RESET, (byte) 0x00, 0);
    }

    /**
     * XOR checksum over the first five bytes.
     * >>> CONFIRM WITH ARRAN that the ESP32 uses this exact formula. <
     * If his firmware uses a sum-mod-256 or CRC instead, change ONLY this method.
     */
    private static byte computeChecksum( byte b1, byte b2, byte b3, byte b4) {
        return (byte) (b1 ^ b2 ^ b3 ^ b4);
    }
}