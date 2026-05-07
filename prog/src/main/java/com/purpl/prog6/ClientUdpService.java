package com.purpl.prog6;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ClientUdpService {
    private static final int CLIENT_PORT_FROM = 5001;
    private static final int CLIENT_PORT_TO = 5010;

    private final JFrameGUI gui;
    private final InetAddress serverAddress;
    private final int serverPort;

    private DatagramSocket socket;
    private volatile boolean running = false;

    public ClientUdpService(JFrameGUI gui, String serverHost, int serverPort) throws Exception {
        this.gui = gui;
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.socket = createSocketInRange(CLIENT_PORT_FROM, CLIENT_PORT_TO);
    }

    private DatagramSocket createSocketInRange(int from, int to) throws IOException {
        for (int port = from; port <= to; port++) {
            try {
                return new DatagramSocket(port);
            } catch (BindException ex) {
                // порт занят, пробуем следующий
            }
        }

        throw new IOException("Нет свободных UDP-портов в диапазоне " + from + "-" + to);
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public void start() {
        running = true;

        Thread listenerThread = new Thread(this::listenLoop);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void stop() {
        running = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public void registerOnServer() throws IOException {
        sendToServer(Message.createRegister());
    }

    public void sendCalcRequest(int requestId, double a, double b, double h) throws IOException {
        String message = Message.createCalcRequest(requestId, a, b, h);
        sendToServer(message);
    }

    public void sendCalcResult(int requestId, int partNum, double result) throws IOException {
        String message = Message.createCalcResult(requestId, partNum, result);
        sendToServer(message);
    }

    private void sendToServer(String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                serverAddress,
                serverPort
        );

        socket.send(packet);

        System.out.println("sent to server: " + message);
    }

    private void listenLoop() {
        byte[] buffer = new byte[1024];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String text = new String(
                        packet.getData(),
                        0,
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println("received from server: " + text);

                Message msg = new Message(text);
                handleMessage(msg);

            } catch (Exception ex) {
                if (running) {
                    gui.showNetworkError("Ошибка получения UDP-сообщения: " + ex.getMessage());
                }
            }
        }
    }

    private void handleMessage(Message msg) throws Exception {
        if (msg.isRegisterOk()) {
            gui.showNetworkStatus("Клиент зарегистрирован на сервере. UDP-порт: " + getLocalPort());
        } else if (msg.isCalcTask()) {
            gui.calculateServerTask(msg);
        } else if (msg.isFinalResult()) {
            gui.receiveFinalResult(msg.getRequestId(), msg.getRes());
        } else if (msg.isError()) {
            gui.showNetworkError("Сервер вернул ошибку.");
        } else {
            System.out.println("unknown message type: " + msg.getMsgType());
        }
    }
}
