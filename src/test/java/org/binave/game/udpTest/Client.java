package org.binave.game.udpTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    private DatagramSocket socket;
    private DatagramPacket recvPacket;
    private InetAddress address;
    private byte[] data;
    private static final int port = 8088;

    private Client() {
        try {
            address = InetAddress.getByName("localhost");
            data = new byte[10];
            socket = new DatagramSocket();
            recvPacket = new DatagramPacket(data, data.length);
        } catch (Exception ignored) {
        }
    }

    private void start() {
        try {
            data = "你好！服务端！".getBytes();
            recvPacket.setAddress(address);
            recvPacket.setPort(port);
            recvPacket.setData(data);
            socket.send(recvPacket);
            recvPacket.setData(data);
            socket.receive(recvPacket);
            int dlen = recvPacket.getLength();
            String str = new String(data, 0, dlen, "UTF-8");
            System.out.println("服务端：" + str);
        } catch (IOException ignored) {
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
