package dnsResolver;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DNSServer {
	public static int bufferSize = 512;
	private static DatagramSocket socket = null;
	private static DatagramSocket googleSocket = null;
	public static void run(){
		//DatagramSocket socket = null;
		DNSCache cache = new DNSCache(); //get and add methods synchronized
		socket = createSocket(8053);
		googleSocket = createSocket(8054);
		
		while(true) {
			byte [] buffer = new byte [DNSServer.bufferSize];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				socket.receive(packet);	 //**LISTEN**
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				DNSMessage message = DNSMessage.decodeMessage(buffer);
				ArrayList<DNSRecord> answersList = new ArrayList<DNSRecord>();
			
				//Loop through questions
				for(int i = 0; i < message.questions.length; i++) {
					DNSQuestion question = message.questions[i];
					DNSRecord response = cache.get(question);
					
					if (response == null) {	
						DatagramPacket googlePacket = askGoogle(message);
						
						printResponseToFile(googlePacket.getData(), "googleBytes.txt");
						DNSMessage googleMessage = DNSMessage.decodeMessage(googlePacket.getData());
						
						
						//write to cache
						DNSRecord answer = googleMessage.getAnswers()[0];
						cache.add(question, answer);
						answersList.add(answer);
					}else if(!response.timestampValid()) {
						cache.remove(question);
						DatagramPacket googlePacket = askGoogle(message);
						DNSMessage googleMessage = DNSMessage.decodeMessage(googlePacket.getData());
						DNSRecord answer = googleMessage.getAnswers()[0];
						cache.add(question, answer);
						answersList.add(answer);
					}
				}
			
			
				DNSRecord[] answers = listToArray(answersList);
				DNSMessage response = DNSMessage.buildResponse(message, answers);
				byte [] responseBytes = response.toBytes();
				printResponseToFile(responseBytes, "responseBytes.txt");
				DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
				
				try {
					responsePacket.setPort(port);
					responsePacket.setAddress(address);
					socket.send(responsePacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	private static DatagramSocket createSocket(int port) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(port);
			
		} catch (SocketException e) {
			System.out.println("Error accessing the socket");
			socket.close();
			e.printStackTrace();
			System.exit(0);	
		}
		return socket;
	}
	
	private static DNSRecord [] listToArray(ArrayList<DNSRecord> list) {
		DNSRecord [] ans = new DNSRecord [list.size()];
		for(int i = 0; i < list.size(); i++) {
			ans[i] = list.get(i);
		}
		return ans;
	}
	
	private static DatagramPacket askGoogle(DNSMessage message) {
		//compose question
		DatagramPacket receivingPacket = null;
		try {
			byte [] buffer = new byte [512];
			receivingPacket = new DatagramPacket(buffer, buffer.length);
			sendQuestionToGoogle(message);
			googleSocket.receive(receivingPacket);
			System.out.println("google packet received");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return receivingPacket;
	}
	
	private static void printResponseToFile(byte [] data, String filename) {
		File outputFile = new File(filename);
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(outputFile);
			fileOut.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void sendQuestionToGoogle(DNSMessage message) throws IOException {
		byte [] requestBytes = message.toBytes();
		DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, InetAddress.getByName("8.8.8.8"), 53);
		System.out.println("sending request to google");
		googleSocket.send(packet);
	}
}
