package dnsResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DNSHeader {
	private int id;
	private boolean isResponse;
	private int opcode;
	private boolean isAuthortiativeAns;
	private boolean isTruncated;
	private boolean recursionDesired;
	private boolean isRecursive;
	private boolean isAuthentic;
	private boolean isCheckDisabled;
	private int responseCode;
	private int questionCount;
	private int answerCount;
	private int authorityRRCount;
	private int additionalRRCount;
	
	
	public int getId() {
		return id;
	}
	
	public boolean getIsResponse() {
		return isResponse;
	}
	
	public int getAnswerCount() {
		return answerCount;
	}
	
	public int getQuestionCount() {
		return questionCount;
	}
	
	public int getAuthorityRRCount() {
		return authorityRRCount;
	}

	public int getAdditionalRRCount() {
		return additionalRRCount;
	}
	
	
	private void setQueryType(int byteVal) {
		int oc = (byteVal >> 3 ) & 0x0F;
		assert(oc <= 2 && oc <= 0);
		opcode = oc;
	}
	
	
	private void read3rdByte(InputStream inputStream) throws IOException {
		int byteVal = inputStream.read();	
		if(byteVal == -1) throw new Error("Error reading 3rd Byte");
		isResponse = ioUtils.decodeTrueFalse(7, byteVal);
		setQueryType(byteVal);
		isAuthortiativeAns = ioUtils.decodeTrueFalse(2, byteVal);
		isTruncated = ioUtils.decodeTrueFalse(1, byteVal);
		recursionDesired = ioUtils.decodeTrueFalse(0, byteVal);
	}
	
	
	private void setResponseCode (int byteVal) {
		int rc = byteVal & 0x0F;
		assert(rc <= 5 && rc <= 0);
		responseCode = rc;
	}
	
	private void read4thByte(InputStream inputStream) throws IOException {
		int byteVal = inputStream.read();
		if(byteVal == -1) throw new Error("Error reading 4th Byte");
		isRecursive = ioUtils.decodeTrueFalse(7, byteVal);
		isAuthentic = ioUtils.decodeTrueFalse(6, byteVal);
		isCheckDisabled = ioUtils.decodeTrueFalse(5, byteVal);
		setResponseCode(byteVal);
	}
	

	/**
	 *reads header from an input stream
	 ***/
	public static DNSHeader decodeHeader(InputStream inputStream) {
		DNSHeader dnsHeader = new DNSHeader();
		
		try {
			dnsHeader.id = ioUtils.decode2Bytes(inputStream);
			dnsHeader.read3rdByte(inputStream);
			dnsHeader.read4thByte(inputStream);
			dnsHeader.questionCount = ioUtils.decode2Bytes(inputStream);
			dnsHeader.answerCount = ioUtils.decode2Bytes(inputStream);
			dnsHeader.authorityRRCount = ioUtils.decode2Bytes(inputStream);
			dnsHeader.additionalRRCount = ioUtils.decode2Bytes(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dnsHeader;
	}
	
	/**
	 *creates header from a response
	 ***/
	public static DNSHeader buildResponseHeader(DNSMessage request, DNSMessage response) {
		DNSHeader responseHeader = new DNSHeader();
		responseHeader.id = request.getHeader().getId();
		responseHeader.isResponse = true;
		responseHeader.opcode = request.getHeader().opcode;
		responseHeader.isAuthortiativeAns = false;
		responseHeader.isTruncated = false;
		responseHeader.recursionDesired = request.getHeader().recursionDesired;
		responseHeader.isRecursive = false;
		responseHeader.isAuthentic = true;
		responseHeader.isCheckDisabled = true;
		
		responseHeader.responseCode = 0;
		responseHeader.questionCount = request.getHeader().questionCount;
		responseHeader.answerCount = response.answers.length;
		responseHeader.authorityRRCount = response.authorityRecords.length;
		responseHeader.additionalRRCount = response.additionalRecords.length;
		
		response.header = responseHeader;
		return responseHeader;
	}
	
	/**
	 *writes bytes to output stream
	 ***/
	public void writeBytes(OutputStream outputStream) {
		try {
			File file = new File("text1.txt");
			FileOutputStream fileStream = new FileOutputStream(file);
			ioUtils.writeBytes(2, id, outputStream);
			writeByte3and4(outputStream);
			ioUtils.writeBytes(2, questionCount, outputStream);
			ioUtils.writeBytes(2, answerCount, outputStream);
			ioUtils.writeBytes(2, authorityRRCount, outputStream);
			ioUtils.writeBytes(2, additionalRRCount, outputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;	
	}
	//Byte 3 refers to header protocol
	private void writeByte3and4(OutputStream outputStream) throws IOException {
		int response;
		if(isResponse) {
			response = 0x00008000;
		}else {
			response = 0x00000000;
		}
			
		int oc = ((opcode << 11) & 0x00007800);
		
		int authoritative;
		if(isAuthortiativeAns) {
			authoritative = 0x00000400;
		}else {
			authoritative = 0x00000000;
		}
		
		int trunc;
		if(isTruncated) {
			trunc = 0x00000200;
		}else {
			trunc = 0x00000000;
		}
		
		int rd;
		if(recursionDesired) {
			rd= 0x00000100;
		}else {
			rd= 0x00000000;
		}
		
		int recursive;
		if(isRecursive) {
			recursive= 0x00000080;
		}else {
			recursive= 0x00000000;
		}
		
		int authentic;
		if(isAuthentic) {
			authentic= 0x00000040;
		}else {
			authentic= 0x00000000;
		}
		
		int checkDis;
		if(isCheckDisabled) {
			checkDis = 0x00000020;
		}else {
			checkDis = 0x00000000;
		}
		
		int rc = (responseCode & 0x0000000F);
		
		response = response | oc | authoritative | trunc | rd | recursive | authentic | checkDis | rc;
		ioUtils.writeBytes(2, response, outputStream);
	}
	
	
	@Override
	public String toString() {
		return "DNSHeader [id=" + id + ", isResponse=" + isResponse + ", opcode=" + opcode + ", isAuthortiativeAns="
				+ isAuthortiativeAns + ", isTruncated=" + isTruncated + ", recursionDesired=" + recursionDesired
				+ ", isRecursive=" + isRecursive + ", isAuthentic=" + isAuthentic + ", isCheckDisabled="
				+ isCheckDisabled + ", responseCode=" + responseCode + ", questionCount=" + questionCount + ", rrCount="
				+ answerCount + ", nameServerRRCount=" + authorityRRCount + ", additionalRRCount=" + additionalRRCount
				+ "]\n";
	}
	
	
	
}

