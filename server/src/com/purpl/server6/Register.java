package com.purpl.server6;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

// msgType;requestId;partNum;a;b;h;res
// REGISTER;0;0;0;0;0;0
// REGISTER_OK;0;0;0;0;0;0
// CALC_REQUEST;1001;0;0.000001;10;0.001;0
// CALC_TASK;1001;1;3.333333;6.666666;0.001;0
// CALC_RESULT;1001;1;0;0;0;0.12345
// FINAL_RESULT;1001;0;0;0;0;1.23456

public class Register {
    private static final ArrayList<User> users = new ArrayList<>();

    private Register() {

    }

    public static User registration(DatagramPacket packet, Message msg) {
        if (!Message.TYPE_REGISTER.equals(msg.getMsgType())) {
            System.out.println("this is not registration message.\n");
            return null;
        }

        InetAddress ip = packet.getAddress();
        int port = packet.getPort();

        User existingUser = findByAddress(ip, port);

        if (existingUser != null) {
            System.out.println("client already registered: " + existingUser + "\n");
            return existingUser;
        }

        String clientName = "Client-" + (users.size() + 1);

        User newUser = new User(ip, port, clientName);
        users.add(newUser);

        System.out.println("client registered: " + newUser + "\n");

        return newUser;
    }

    public static User findByAddress(InetAddress ip, int port) {
        for (User user : users) {
            if (user.getClientIp().equals(ip) && user.getClientPort() == port) {
                return user;
            }
        }

        return null;
    }

    public static ArrayList<User> getUsers() {
        return users;
    }

    public static ArrayList<User> getUsersCopy() {
        return new ArrayList<>(users);
    }

    public static int getUserCount() {
        return users.size();
    }

    public static void printUsers() {
        System.out.println("=== registered users ===");

        if (users.isEmpty()) {
            System.out.println("no users registered\n");
            return;
        }

        for (User user : users) {
            System.out.println(user);
        }

        System.out.println();
    }
}