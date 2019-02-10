package dnsResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSMessage {
	DNSHeader header;
	DNSQuestion[] questions;
	DNSRecord[] answers;
	DNSRecord[] authorityRecords;
	DNSRecord[] additionalRecords;
	HashMap<String, Integer> domainLocations;
	HashMap<String, Integer> inputDomainLocations;
	
	public DNSMessage() {
		domainLocations = new HashMap<String, Integer>();
		inputDomainLocations = new HashMap<String, Integer>();
	}
	
	private byte[] rawMessage;
	
	private void setHeader(DNSHeader h) {
		header = h;
	}
	
	private void setRawMessage(byte[] bytes) {
		rawMessage = bytes;
	}
	
	public byte[] getRawMessage() {
		return rawMessage;
	}
	
	public DNSHeader getHeader() {
		return header;
	}
	
	public DNSRecord [] getAnswers() {
		return answers;
	}
	
	public static DNSMessage decodeMessage(byte[] bytes) {
		DNSMessage message = new DNSMessage();
		message.setRawMessage(bytes);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		message.setHeader(DNSHeader.decodeHeader(inputStream));
		
		int questionCount =  message.getHeader().getQuestionCount();
		int answerCount =  message.getHeader().getAnswerCount();
		int authorityRRCount =  message.getHeader().getAuthorityRRCount();
		int additionalRRCount =  message.getHeader().getAdditionalRRCount();
		
		message.questions = new DNSQuestion [questionCount];
		message.answers = new DNSRecord [answerCount];
		message.authorityRecords = new DNSRecord [authorityRRCount];
		message.additionalRecords = new DNSRecord [additionalRRCount];
		//use header to determine how many records to read
		for(int i = 0; i < questionCount; i++) {
			message.questions[i] = DNSQuestion.decodeQuestion(inputStream, message);
//			System.out.println("Question Values");
//			System.out.println(message.questions[i].toString());
		}
		for(int i = 0; i < answerCount; i++) {
			System.out.println("Starting to decode answers");
			inputStream.mark(100);
			message.toString();
			message.answers[i] = DNSRecord.decodeRecord(inputStream, message);
		}
		for(int i = 0; i < authorityRRCount; i++) {
			message.authorityRecords[i] = DNSRecord.decodeRecord(inputStream, message);
		}
		for(int i = 0; i < additionalRRCount; i++) {
			message.additionalRecords[i] = DNSRecord.decodeRecord(inputStream, message);
		}
		return message;
	}
	
	private String decodeLabel(int wordLen, InputStream inputStream) throws IOException {
		char [] newWord = new char [wordLen];
		for(int i = 0; i < wordLen; i++) {
			int asciiVal = inputStream.read();
			newWord[i] = ioUtils.getLowercaseChar(asciiVal);
		}
		String newLabel = new String(newWord);
		return newLabel;	
	}
	
	private String [] arrayListToArray(ArrayList<String> list) {
		String [] newArray = new String [list.size()];
		for(int i = 0; i < list.size(); i++) {
			newArray[i] = list.get(i);
		}
		return newArray;
	}
	
	private void addArrayToList(String [] array, ArrayList<String> list) {
		for(int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
	}
	
	public  String[] readDomainName(InputStream inputStream) {
		ArrayList<String> domainNameLabels = new ArrayList<String>();
		int wordLen = 1;
		String label = "";
		int byteCounter = 0;
		
		while(wordLen > 0) {
			try {
				int byteVal = inputStream.read();
				byteCounter += 1;
				//get first 2 bits of byte to determine below if pointer
				int labelType = (byteVal >> 6) & 0x03;
				wordLen = byteVal & 0x3F; //decode length or pointer pos
				if(wordLen <= 0 && labelType == 0) break; //exit on 0 ;
				//check first 2 bits. If 00 start of label if 11 it's a pointer
				if( labelType == 3 ) {
					int secondByte = inputStream.read();
					int ptrVal = ((byteVal << 8) & 0xcf) | secondByte;
					
					System.out.print("The ptrVal before recursive call: ");
					System.out.println(ptrVal);
					addArrayToList(readDomainName(ptrVal), domainNameLabels); //a pointer to name location
					//break; //TEST BREAK!!!!
				}else if (labelType == 0) {				
					//int domainStartLocation = DNSServer.bufferSize - inputStream.available();
					label = decodeLabel(wordLen, inputStream);
					//domainLocations.put(label, domainStartLocation); //store position in hashmap
				}			
			} catch (IOException e) {
				System.out.println("Something went wrong reading the domain name");
				e.printStackTrace();
			}
			if(label.length() != 0) {
				domainNameLabels.add(label);
			}else {
				break;
			}
		}
		String [] domain = arrayListToArray(domainNameLabels);
		System.out.print("The size of the the domain name was: ");
		System.out.println(domain.length);
		
		return domain;	
	}
	
	String[] readDomainName(int firstByte) {
		ByteArrayInputStream tempInputStream = new ByteArrayInputStream(rawMessage, firstByte, rawMessage.length - firstByte);
		String[] domainName = readDomainName(tempInputStream);
		return domainName;
	}
	
	static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers) {
		DNSMessage response = new DNSMessage();
		response.questions = request.questions;
		response.answers = answers;
		response.authorityRecords = request.authorityRecords;
		response.additionalRecords = request.additionalRecords;
		DNSHeader.buildResponseHeader(request, response);	
		return response;		
	}
	
	byte[] toBytes() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		header.writeBytes(outputStream);
		//write bytes for each resource
		try {
			for(DNSQuestion question : questions) {
				question.writeBytes(outputStream, domainLocations);
			}
			for(DNSRecord answer : answers) {
				answer.writeBytes(outputStream, domainLocations);
			}
			for(DNSRecord authRecord : authorityRecords) {
				authRecord.writeBytes(outputStream, domainLocations);
			}
			for(DNSRecord addRecord : additionalRecords) {
				addRecord.writeBytes(outputStream, domainLocations);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}
	
	static void writeDomainName(ByteArrayOutputStream outputStream, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {
		int arrSize = domainPieces.length;
		boolean endsWithLabel = true;
		for(int i = 0; i < arrSize; i++ ) { 
			String fullDomainName = getFullDomain(i, domainPieces);
			
			if(!domainLocations.containsKey(fullDomainName)) {
				//store pointer to the full name but do not write the full name
				domainLocations.put(fullDomainName, outputStream.size());
				//write part of name
				writeNewLabel(domainPieces[i], outputStream);
				
			}else if( domainLocations.containsKey(fullDomainName) ) {
				int pointerPos = domainLocations.get(fullDomainName);
				writeExistingLabel(pointerPos, outputStream);
				endsWithLabel = false;
				break;
				
			}else {
				System.out.println("Something went wrong writing question domain names");
				System.exit(1);	
			}
		}
		
		if(endsWithLabel) {
			ioUtils.writeBytes(1, 0, outputStream);
		};
	}
	
	
	private static void writeNewLabel(String domainName,  ByteArrayOutputStream outputStream) {
		int len = domainName.length();
		int ret = 0x3F & len;
		try {
			ioUtils.writeBytes(1, ret, outputStream);
			for(int i = 0; i < len; i++) {
				int charVal = domainName.charAt(i);
				ioUtils.writeBytes(1, charVal, outputStream);
			}
		} catch (IOException e) {
			System.out.println("Something went wront writing new label");
			e.printStackTrace();
		}
		
	}
	
	private static String getFullDomain(int currentPos, String [] arr) {
		String ans = "";
		for(int i = currentPos; i < arr.length; i++) {
			ans += arr[i];
			if(i != arr.length -1 ) {
				ans += ".";
			}
		}
		return ans;
	}
	
	private static void writeExistingLabel(int pointerPos,  ByteArrayOutputStream outputStream) {
		int ret = 0xC000 | pointerPos;
		try {
			ioUtils.writeBytes(2, ret, outputStream);
		} catch (IOException e) {
			System.out.println("Something went wront writing new label");
			e.printStackTrace();
		}
	}
	
	String octetsToString(String[] octets) {
		String domainName = "";
		for(String octet : octets) {
			domainName += ".";
			domainName += octet;
		}
		System.out.println("Full Domain Name: ");
		return domainName;
	}

	@Override
	public String toString() {
		return "DNSMessage \n"
				+ "\t[header=" + header + 
				"\tquestions=" + Arrays.toString(questions) + 
				"\tanswers=" + Arrays.toString(answers) + 
				"\tauthorityRecords=" + Arrays.toString(authorityRecords)
				+ "\tadditionalRecords=" + Arrays.toString(additionalRecords) + 
				"\tdomainLocations=" + domainLocations
				+ "\t, rawMessage=" + Arrays.toString(rawMessage) + "]\n";
	}
	
}
