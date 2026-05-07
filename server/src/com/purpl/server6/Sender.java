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

public class Sender {
    private Sender() {

    }

    public static void send(DatagramSocket socket, User user, String message) throws Exception {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                user.getClientIp(),
                user.getClientPort()
        );

        socket.send(packet);

        System.out.println("sent to " + user + ": " + message);
    }
}