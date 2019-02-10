package dnsResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DNSRecord {
	ArrayList<String []> domainNames = new ArrayList<String []>();
	int typeNum = -1;
	int classNum = -1;
	public long ttlNum = -1;
	int rdLength = -1;
	byte [] rData;
	Calendar date;

	
	public static DNSRecord decodeRecord(InputStream inputStream, DNSMessage message) {
		DNSRecord dnsRecord = new DNSRecord();
		try {
		dnsRecord.domainNames.add(message.readDomainName(inputStream) );
		dnsRecord.typeNum = ioUtils.decode2Bytes(inputStream);
		if( dnsRecord.typeNum > 255) {
			throw new Error("error reading typeNum");
		}
		dnsRecord.classNum = ioUtils.decode2Bytes(inputStream);
		dnsRecord.ttlNum = ioUtils.decode4Bytes(inputStream);
		int ttl = (int) dnsRecord.ttlNum;
		dnsRecord.date = Calendar.getInstance();
		dnsRecord.date.add(Calendar.SECOND, ttl);
		dnsRecord.rdLength = ioUtils.decode2Bytes(inputStream);
		if(dnsRecord.rdLength > 512) {
			throw new Error("error reading rdata Length");
		}
		dnsRecord.rData = decodeRData(inputStream, dnsRecord);
		
		System.out.println(dnsRecord.toString());
		} catch (IOException e) {
			System.out.println("Something went wrong reading the DNS Record");
			e.printStackTrace();
		}
		return dnsRecord;
	}
	
	public static byte[] decodeRData(InputStream inputStream, DNSRecord dnsRecord) throws IOException {
		byte [] rDataArr = new byte [dnsRecord.rdLength];
		for(int i = 0; i < dnsRecord.rdLength; i++) {
			rDataArr[i] = (byte)inputStream.read();
		}
		
		return rDataArr;	
	}
	
	public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainLocations) {
		try {
			writeDomainNames(outputStream, domainLocations);
			ioUtils.writeBytes(2, typeNum, outputStream);
			ioUtils.writeBytes(2, classNum, outputStream);
			ioUtils.writeBytes(4, ttlNum, outputStream);
			ioUtils.writeBytes(2, rdLength, outputStream);
			ioUtils.writeBytes(rData, outputStream);			
		} catch (IOException e) {
			System.out.println("Problem occured while reading DNSRecord");
			e.printStackTrace();
		}
	}
	
	private void writeDomainNames(ByteArrayOutputStream outputStream, HashMap<String,Integer> domainLocations) throws IOException  {
		for(String [] arr : domainNames) {
			DNSMessage.writeDomainName(outputStream, domainLocations, arr);
		}
	}
	public boolean timestampValid() {
		Calendar currentTime = Calendar.getInstance();
		return currentTime.before(date);
	}
	
	@Override
	public String toString() {
		return "DNSRecord [domainNames=" + domainNames + ", typeNum=" + typeNum + ", classNum=" + classNum + ", ttlNum="
				+ ttlNum + ", rdLength=" + rdLength + ", RData=" + rData + "]\n";
	}
}
