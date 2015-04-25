package com.rit.ricart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RicartAgravala {
	public static String MY_PROCESS_ID;
	ServerSocket serverSoc;
	MulticastSocket mcSocket;
	public final static int MESSAGE_PORT = 8001;
	
	public static AtomicInteger NO_OF_REPLYS = new AtomicInteger();
	public static States MY_STATE;
	public static List<RequestMessage> requestList = new ArrayList<>();
	
	public RicartAgravala(String processID) {
		MY_PROCESS_ID = processID;
		MY_STATE = States.RELEASED;
		try{
			serverSoc =  new ServerSocket(MESSAGE_PORT);
			mcSocket = new MulticastSocket(RequestBroadcaster.GROUP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	Runnable replyHandler = new Runnable() {	
		@Override
		public void run() {
			while(true){
				Socket clientSoc;
				try {
					clientSoc = serverSoc.accept();
					System.out.println("OK message received from Process : "+
							new BufferedReader(new InputStreamReader(clientSoc.getInputStream())).readLine());
					NO_OF_REPLYS.incrementAndGet();
					clientSoc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	
	Runnable requestHandler = new Runnable() {	
		@Override
		public void run() {
			System.setProperty("java.net.preferIPv4Stack" , "true");
			try {
				InetAddress inetAddress = InetAddress.getByName(RequestBroadcaster.GROUP_ADDR);
				mcSocket.joinGroup(inetAddress);
				ExecutorService excecutor = Executors.newCachedThreadPool();
				byte[] buf = new byte[1024];
				while(true){
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					mcSocket.receive(packet);
					excecutor.submit(new RequestWorker(packet));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	
	Runnable criticalSectionRequest = new Runnable() {	
		@SuppressWarnings("resource")
		@Override
		public void run() {
			Scanner scanner = new Scanner(System.in);
			while(true){
				System.out.println("\nPress Return to Enter Critical Section");
				scanner.nextLine();
				MY_STATE = States.WANTED;
				System.out.println("State : WANTED");
				new Thread(new RequestBroadcaster()).start();
				enterCriticalSection();
			}
		}
	};
	
	
	private void enterCriticalSection() {
		while(NO_OF_REPLYS.get() != HeartBeatSender.processList.size()-1);
		
		MY_STATE = States.HELD;
		System.out.println("Entering Critical Section !!");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		NO_OF_REPLYS.set(0);
		MY_STATE = States.RELEASED;
		System.out.println("State : RELEASED");
		
		for(RequestMessage msg : requestList)
			replyToRequester(msg.ipAddress);
		requestList.clear();
	}

	
	public static void replyToRequester(String ipAddress){
		try {
			Socket socket = new Socket(ipAddress, RicartAgravala.MESSAGE_PORT);
			PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
			out.println(MY_PROCESS_ID);
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 1){
			System.err.println("Usage : java RicartAgravala <Process ID>");
			System.exit(0);
		}
		
		RicartAgravala ra = new RicartAgravala(args[0]);
		
		ExecutorService excecutor = Executors.newFixedThreadPool(4);
		excecutor.submit(ra.criticalSectionRequest);
		excecutor.submit(ra.replyHandler);
		excecutor.submit(ra.requestHandler);
		excecutor.submit(new HeartBeatSender());
	}
}


class RequestWorker implements Runnable{

	DatagramPacket packet;
	
	public RequestWorker(DatagramPacket packet) {
		this.packet = packet;
	}
	
	@Override
	public void run() {
		byte[] buf = packet.getData();
		RequestMessage msg = RequestMessageUtil.deSerialize(buf);
		
		if(!msg.processID.equals(RicartAgravala.MY_PROCESS_ID)){
			if(RicartAgravala.MY_STATE == States.HELD ||
					RicartAgravala.MY_STATE == States.WANTED){
				System.out.println("state : "+RicartAgravala.MY_STATE);
				System.out.println("Adding Process "+msg.processID+" to Queue");
				RicartAgravala.requestList.add(msg);
			}
			else{
				String address = msg.ipAddress;
				RicartAgravala.replyToRequester(address);
			}
		}
	}
	
}
