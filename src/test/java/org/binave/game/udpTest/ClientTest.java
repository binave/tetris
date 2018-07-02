package org.binave.game.udpTest;

import org.binave.game.tetris.common.UDPArrayAlter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;


public class ClientTest {
    private DatagramSocket ds;
    private DatagramPacket dp;
    private InetAddress ip;
    private byte[] buf;
    private static final int port = 8088;
    private UDPArrayAlter uaa;

    private ClientTest() {
        try {
            ip = InetAddress.getByName("localhost");
            buf = new byte[2];
            ds = new DatagramSocket();
            dp = new DatagramPacket(buf, buf.length);
//			uaa = new UDPArrayAlter();
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        ClientTest cl = new ClientTest();
        Random ran = new Random();
        cl.send(ran.nextInt(15), ran.nextInt(15));
        cl.rec();
        cl.send(ran.nextInt(15), ran.nextInt(15));
        cl.rec();
    }

    private void send(int a, int b) {
//		uaa.link(buf);
//		uaa.offer(a, 4);
//		uaa.offer(b, 4);
        try {
            dp.setAddress(ip);
            dp.setPort(port);
            dp.setData(buf);
//			dp.setLength(uaa.close());
            ds.send(dp);
        } catch (Exception ignored) {
        }
    }

    private void rec() {
        try {
            dp.setData(buf);
            ds.receive(dp);
//			uaa.link(buf);
//			System.out.println("Server:" + uaa.poll(4) + ", " + uaa.poll(4));
//			uaa.close();
        } catch (IOException ignored) {
        }
    }
}
