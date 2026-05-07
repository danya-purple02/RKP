package com.purpl.server6;

import java.net.InetAddress;

// msgType;requestId;partNum;a;b;h;res
// REGISTER;0;0;0;0;0;0
// REGISTER_OK;0;0;0;0;0;0
// CALC_REQUEST;1001;0;0.000001;10;0.001;0
// CALC_TASK;1001;1;3.333333;6.666666;0.001;0
// CALC_RESULT;1001;1;0;0;0;0.12345
// FINAL_RESULT;1001;0;0;0;0;1.23456

public class User {
    private final InetAddress clientIp;
    private final int clientPort;
    private final String clientName;

    public User(InetAddress clientIp, int clientPort, String clientName) {
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.clientName = clientName;
    }

    public InetAddress getClientIp() {
        return clientIp;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public String toString() {
        return clientName + " " + clientIp.getHostAddress() + ":" + clientPort;
    }
}