package com.purpl.server6;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

// msgType;requestId;partNum;a;b;h;res
// REGISTER;0;0;0;0;0;0
// REGISTER_OK;0;0;0;0;0;0
// CALC_REQUEST;1001;0;0.000001;10;0.001;0
// CALC_TASK;1001;1;3.333333;6.666666;0.001;0
// CALC_RESULT;1001;1;0;0;0;0.12345
// FINAL_RESULT;1001;0;0;0;0;1.23456

public class Recipient {
    private static final int BUFFER_SIZE = 1024;

    private Recipient() {

    }

    public static DatagramPacket receive(DatagramSocket socket) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        return packet;
    }

    public static String getText(DatagramPacket packet) {
        return new String(
                packet.getData(),
                0,
                packet.getLength(),
                StandardCharsets.UTF_8
        );
    }
}