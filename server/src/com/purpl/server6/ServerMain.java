package com.purpl.server6;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.SocketTimeoutException;

// msgType;requestId;partNum;a;b;h;res
// REGISTER;0;0;0;0;0;0
// REGISTER_OK;0;0;0;0;0;0
// CALC_REQUEST;1001;0;0.000001;10;0.001;0
// CALC_TASK;1001;1;3.333333;6.666666;0.001;0
// CALC_RESULT;1001;1;0;0;0;0.12345
// FINAL_RESULT;1001;0;0;0;0;1.23456

public class ServerMain {
    private static final int SERVER_PORT = 5000;
    private static final int STATUS_CHECK_TIMEOUT_MS = 1500;

    private static final HashMap<Integer, CalcRequest> requests = new HashMap<>();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("UDP server started on port " + SERVER_PORT);
            System.out.println("waiting for messages...\n");

            while (true) {
                DatagramPacket packet = Recipient.receive(socket);
                String text = Recipient.getText(packet);

                System.out.println("received from "
                        + packet.getAddress().getHostAddress()
                        + ":"
                        + packet.getPort()
                        + ": "
                        + text);

                try {
                    Message msg = new Message(text);

                    if (msg.isRegister()) {
                        handleRegister(socket, packet, msg);
                    } else if (msg.isCalcRequest()) {
                        handleCalcRequest(socket, packet, msg);
                    } else if (msg.isCalcResult()) {
                        handleCalcResult(socket, msg);
                    } else {
                        System.out.println("unknown message type: " + msg.getMsgType() + "\n");
                    }

                } catch (Exception ex) {
                    System.out.println("message processing error: " + ex.getMessage() + "\n");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void handleRegister(DatagramSocket socket, DatagramPacket packet, Message msg) throws Exception {
        User user = Register.registration(packet, msg);

        if (user != null) {
            Sender.send(socket, user, Message.createRegisterOk());
            Register.printUsers();
        }
    }

    private static void handleCalcRequest(DatagramSocket socket, DatagramPacket packet, Message msg) throws Exception {
        User mainUser = Register.findByAddress(packet.getAddress(), packet.getPort());

        if (mainUser == null) {
            System.out.println("calc request from unregistered user\n");
            return;
        }

        ArrayList<User> registeredUsers = Register.getUsersCopy();

        if (registeredUsers.isEmpty()) {
            System.out.println("no registered users for calculation\n");
            Sender.send(socket, mainUser, Message.createError());
            return;
        }

        int requestId = msg.getRequestId();

        if (requests.containsKey(requestId)) {
            System.out.println("request with this id already exists: " + requestId + "\n");
            return;
        }

        System.out.println("checking active clients before calculation...\n");

        ArrayList<User> calcUsers = checkActiveUsers(socket, requestId, registeredUsers);

        if (calcUsers.isEmpty()) {
            System.out.println("no active users for calculation\n");
            Sender.send(socket, mainUser, Message.createError());
            return;
        }

        CalcRequest request = new CalcRequest(
                requestId,
                mainUser,
                calcUsers,
                msg.getA(),
                msg.getB(),
                msg.getH()
        );

        requests.put(requestId, request);

        System.out.println("new calc request: " + request);
        System.out.println("distributing task between " + calcUsers.size() + " active clients...\n");

        distributeTask(socket, request);
    }

    private static void distributeTask(DatagramSocket socket, CalcRequest request) throws Exception {
        ArrayList<User> users = request.getCalcUsers();

        double a = request.getA();
        double b = request.getB();
        double h = request.getH();

        int clientCount = users.size();

        double length = b - a;
        double partLength = length / clientCount;

        for (int i = 0; i < clientCount; i++) {
            double partA = a + partLength * i;
            double partB;

            if (i == clientCount - 1) {
                partB = b;
            } else {
                partB = partA + partLength;
            }

            String taskMessage = Message.createCalcTask(
                    request.getRequestId(),
                    i,
                    partA,
                    partB,
                    h
            );

            Sender.send(socket, users.get(i), taskMessage);
        }

        System.out.println();
    }

    private static void handleCalcResult(DatagramSocket socket, Message msg) throws Exception {
        CalcRequest request = requests.get(msg.getRequestId());

        if (request == null) {
            System.out.println("result for unknown request: " + msg.getRequestId() + "\n");
            return;
        }

        request.addResult(msg.getRes());

        System.out.println("received result for request "
                + msg.getRequestId()
                + ", part "
                + msg.getPartNum()
                + ": "
                + msg.getRes());

        System.out.println("progress: "
                + request.getReceivedResults()
                + "/"
                + request.getExpectedResults()
                + "\n");

        if (request.isCompleted()) {
            double totalResult = request.getTotalResult();

            String finalMessage = Message.createFinalResult(
                    request.getRequestId(),
                    totalResult
            );

            Sender.send(socket, request.getMainUser(), finalMessage);

            System.out.println("request completed: " + request.getRequestId());
            System.out.println("final result: " + totalResult);
            System.out.println("sent final result to: " + request.getMainUser() + "\n");

            requests.remove(request.getRequestId());
        }
    }

    private static ArrayList<User> checkActiveUsers(DatagramSocket socket, int requestId, ArrayList<User> users) throws Exception {
        ArrayList<User> activeUsers = new ArrayList<>();
        ArrayList<User> inactiveUsers = new ArrayList<>();

        String statusCheckMessage = Message.createStatusCheck(requestId);

        for (User user : users) {
            Sender.send(socket, user, statusCheckMessage);
        }

        int oldTimeout = socket.getSoTimeout();
        socket.setSoTimeout(STATUS_CHECK_TIMEOUT_MS);

        long startTime = System.currentTimeMillis();

        try {
            while (activeUsers.size() < users.size()) {
                try {
                    DatagramPacket packet = Recipient.receive(socket);
                    String text = Recipient.getText(packet);

                    System.out.println("status response received from "
                            + packet.getAddress().getHostAddress()
                            + ":"
                            + packet.getPort()
                            + ": "
                            + text);

                    Message msg = new Message(text);

                    if (!msg.isStatusOk()) {
                        System.out.println("not status message during status check: " + msg.getMsgType());
                        continue;
                    }

                    if (msg.getRequestId() != requestId) {
                        System.out.println("status ok for another request: " + msg.getRequestId());
                        continue;
                    }

                    User user = Register.findByAddress(packet.getAddress(), packet.getPort());

                    if (user == null) {
                        System.out.println("STATUS_OK from unknown user\n");
                        continue;
                    }

                    if (!activeUsers.contains(user)) {
                        activeUsers.add(user);
                        System.out.println("client is active: " + user);
                    }

                    long elapsed = System.currentTimeMillis() - startTime;

                    if (elapsed >= STATUS_CHECK_TIMEOUT_MS) {
                        break;
                    }

                } catch (SocketTimeoutException ex) {
                    break;
                }
            }
        } finally {
            socket.setSoTimeout(oldTimeout);
        }

        for (User user : users) {
            if (!activeUsers.contains(user)) {
                inactiveUsers.add(user);
            }
        }

        if (!inactiveUsers.isEmpty()) {
            System.out.println("\ninactive clients:");

            for (User user : inactiveUsers) {
                System.out.println(user);
            }

            System.out.println();
        }

        System.out.println("active clients: " + activeUsers.size() + "/" + users.size() + "\n");

        Register.removeUsers(inactiveUsers);

        return activeUsers;
    }

}