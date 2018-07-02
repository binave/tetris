package org.binave.game.udpTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    private DatagramSocket socket;
    private DatagramPacket recvPacket;
    private byte[] data;

    private Server() {
        try {
            data = new byte[21];
            socket = new DatagramSocket(8088);
            recvPacket = new DatagramPacket(data, data.length);
        } catch (SocketException ignored) {
        }
    }

    private void start() {
        try {
            socket.receive(recvPacket);
            int dlen = recvPacket.getLength();
            String str = new String(data, 0, dlen, "UTF-8");
            System.out.println("客户端：" + str);
            data = "你好！客户端！".getBytes();
            recvPacket.setData(data);
            socket.send(recvPacket);
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        Server ser = new Server();
        ser.start();
    }
}
