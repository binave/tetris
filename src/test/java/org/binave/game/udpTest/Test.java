package org.binave.game.udpTest;

import java.net.InetAddress;


public class Test {

    //	private DatagramSocket ds;
//	private InetAddress ip;
//	void start(){
//		try {
//			ds = new DatagramSocket(8088);
//			ip = InetAddress.getLocalHost();
//		} catch (Exception e) {}
//	}
    public static void main(String[] args) {
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // new Test().start();
    }
}
