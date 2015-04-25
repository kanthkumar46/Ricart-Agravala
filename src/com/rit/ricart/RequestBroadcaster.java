package com.rit.ricart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RequestBroadcaster implements Runnable{

	final static String GROUP_ADDR = "224.0.9.10";
	final static int GROUP_PORT = 8000;

	
	@Override
	public void run() {
		try {
			InetAddress inetAddress = InetAddress.getByName(RequestBroadcaster.GROUP_ADDR);
			byte[] msg =  RequestMessageUtil.serialize(new RequestMessage());
			
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(msg, msg.length,inetAddress, 
					RequestBroadcaster.GROUP_PORT);
			socket.send(packet);
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}


class RequestMessage implements Serializable{
	private static final long serialVersionUID = 1L;
	
	String processID;
	String ipAddress;
	
	public RequestMessage() {
		try {
			this.processID = RicartAgravala.MY_PROCESS_ID;
			this.ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}


class RequestMessageUtil{
	public static byte[] serialize(RequestMessage msg){
		ByteArrayOutputStream baos = null;
		
		try {
			baos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(baos);
			os.writeObject(msg);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return baos.toByteArray();
	}
	
	public static RequestMessage deSerialize(byte[] buf){
		RequestMessage msg = null;
		
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bais);
			msg = (RequestMessage)ois.readObject();
			
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return msg;
	}
}
