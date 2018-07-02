package org.binave.game.udpTest;

import org.binave.game.tetris.common.UDPArrayAlter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;


public class ServerTest {
    private DatagramSocket ds;
    private DatagramPacket dp;
    private byte[] buf;
    private UDPArrayAlter uaa;

    private ServerTest() {
        try {
            buf = new byte[2];
            ds = new DatagramSocket(8088);
            dp = new DatagramPacket(buf, buf.length);
            uaa = new UDPArrayAlter();
        } catch (SocketException ignored) {
        }
    }

    public static void main(String[] args) {
        ServerTest ser = new ServerTest();
        Random ran = new Random();
        while (true) {
            ser.rec();
            ser.send(ran.nextInt(15), ran.nextInt(15));
        }
    }

    private void rec() {
        try {
            ds.receive(dp);
//			uaa.link(buf);
//			System.out.println("Client:" + uaa.poll(4) + ", " + uaa.poll(4));
//			uaa.close();
        } catch (IOException ignored) {
        }
    }

    private void send(int a, int b) {
//		uaa.link(buf);
//		uaa.offer(a, 4);
//		uaa.offer(b, 4);
        try {
//			dp.setData(buf); // 若共用发送和接收的数组，仅需要变更数组内容
//			dp.setLength(uaa.close());
            ds.send(dp);
        } catch (Exception ignored) {
        }
    }
}
