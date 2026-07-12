package com.team5.reflextrainer.hardware;

import static org.junit.Assert.*;
import org.junit.Test;

public class SensorMessageTest {

    // helper: build a valid response frame with correct XOR checksum
    private byte[] frame(byte response, byte target, int lo, int hi) {
        byte start = (byte) 0xAA;
        byte b3 = (byte) lo, b4 = (byte) hi;
        byte checksum = (byte) (start ^ response ^ target ^ b3 ^ b4);
        return new byte[]{ start, response, target, b3, b4, checksum };
    }

    @Test
    public void parsesValidResult() {
        // 300ms = 0x012C -> lo=0x2C, hi=0x01
        byte[] msg = frame(SensorMessage.RESP_RESULT, SensorMessage.OUTCOME_CORRECT, 0x2C, 0x01);
        SensorMessage m = SensorMessage.parse(msg);
        assertNotNull(m);
        assertEquals(300, m.reactionTimeMs);
        assertEquals(SensorMessage.RESP_RESULT, m.response);
    }

    @Test
    public void reassemblesLargeTime() {
        // 1500ms = 0x05DC -> lo=0xDC, hi=0x05
        byte[] msg = frame(SensorMessage.RESP_RESULT, SensorMessage.OUTCOME_CORRECT, 0xDC, 0x05);
        SensorMessage m = SensorMessage.parse(msg);
        assertNotNull(m);
        assertEquals(1500, m.reactionTimeMs);
    }

    @Test
    public void rejectsBadChecksum() {
        byte[] msg = frame(SensorMessage.RESP_RESULT, SensorMessage.OUTCOME_CORRECT, 0x2C, 0x01);
        msg[5] = (byte) (msg[5] ^ 0xFF);   // corrupt it
        assertNull(SensorMessage.parse(msg));
    }

    @Test
    public void rejectsWrongStartByte() {
        byte[] msg = frame(SensorMessage.RESP_RESULT, SensorMessage.OUTCOME_CORRECT, 0x2C, 0x01);
        msg[0] = 0x00;
        assertNull(SensorMessage.parse(msg));
    }

    @Test
    public void rejectsWrongLength() {
        assertNull(SensorMessage.parse(new byte[]{ (byte) 0xAA, 0x01 }));
        assertNull(SensorMessage.parse(null));
    }

    @Test
    public void buildsChallengeWithCorrectBytes() {
        // 1000ms = 0x03E8 -> lo=0xE8, hi=0x03
        byte[] out = SensorMessage.buildChallenge(
                SensorMessage.MSG_START_CHALLENGE, SensorMessage.TARGET_SHAKE_IMU, 1000);
        assertEquals(6, out.length);
        assertEquals((byte) 0xAA, out[0]);
        assertEquals(SensorMessage.MSG_START_CHALLENGE, out[1]);
        assertEquals(SensorMessage.TARGET_SHAKE_IMU, out[2]);
        assertEquals((byte) 0xE8, out[3]);
        assertEquals((byte) 0x03, out[4]);
    }

    @Test
    public void challengeRoundTripsThroughChecksum() {
        byte[] out = SensorMessage.buildChallenge(
                SensorMessage.MSG_START_CHALLENGE, SensorMessage.TARGET_SHAKE_IMU, 500);
        byte expected = (byte) (out[0] ^ out[1] ^ out[2] ^ out[3] ^ out[4]);
        assertEquals(expected, out[5]);
    }
}