package com.rit.ricart;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class HeartBeatSender implements Runnable{
	public static Map<String,RequestMessage> processList = new HashMap<>();
	final static int HEARTBEAT_PORT = 8002;
	
	MulticastSocket mcSocket;
	public HeartBeatSender() {
		try {
			mcSocket = new MulticastSocket(HEARTBEAT_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	Runnable HeartBeatHandler = new Runnable() {
		byte[] buf = new byte[1024];
		public void run() {
			while(true){
				try {
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					mcSocket.receive(packet);
					new HeartBeatWorker(packet).start();	
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	@Override
	public void run() {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		try {
			InetAddress inetAddress = InetAddress.getByName(RequestBroadcaster.GROUP_ADDR);
			mcSocket.joinGroup(inetAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Timer heartBeatTimer = new Timer();
		heartBeatTimer.schedule(new HeartBeat(), 0, 1000);
		
		Thread thread = new Thread(HeartBeatHandler);
		thread.start();
	}

}


class HeartBeat extends TimerTask{
	public void run() {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(RequestBroadcaster.GROUP_ADDR);
			byte[] msg = RequestMessageUtil.serialize(new RequestMessage());
			
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(msg, msg.length,inetAddress, 
					HeartBeatSender.HEARTBEAT_PORT);
			socket.send(packet);
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
}


class HeartBeatWorker extends Thread{
	DatagramPacket packet;
	
	public HeartBeatWorker(DatagramPacket packet) {
		this.packet = packet;
	}
	
	@Override
	public void run() {
		byte[] buf = packet.getData();
		RequestMessage msg = RequestMessageUtil.deSerialize(buf);
		
		synchronized (HeartBeatSender.processList) {
			HeartBeatSender.processList.put(msg.processID,msg);
		}
	}
	
}
