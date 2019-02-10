package dnsResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DNSQuestion {
	ArrayList<String []> domainNames;
	String qType;
	int qTypeNum;
	String qClass;
	int qClassNum;
	
	DNSQuestion(){
		domainNames = new ArrayList<String []>();
	}
	private void setDomainNames(ArrayList<String []> dn) {
		domainNames = dn;
	}

	private String lookupQType(int QTypeNum) {
//		System.out.print("QTYPE NUM: ");
//		System.out.println(QTypeNum);
		String qType;
		switch (QTypeNum) {
		case 1:  qType = "A";
        	break;
		case 2:  qType = "NS";
		        break;
		case 3:  qType = "MD";
		        break;
		case 4:  qType = "MF";
		        break;
		case 5:  qType = "CNAME";
		        break;
		case 6:  qType = "SOA";
		        break;
		case 7:  qType = "MB";
		        break;
		case 8:  qType = "MG";
		        break;
		case 9:  qType = "MR";
		        break;
		case 10: qType = "NULL";
		        break;
		case 11: qType = "WKS";
		        break;
		case 12: qType = "PTR";
		        break;
		case 13: qType = "HINFO";
		        break;
		case 14: qType = "MINFO";
        	break;
		case 15: qType = "MX";
        	break;
		case 16: qType = "TXT";
        	break;
		case 252: qType = "AXFR";
        	break;
		case 253: qType = "MAILB";
        	break;
		case 254: qType = "MAILA";
        	break;
		case 255: qType = "ALL RECORDS";
    	break;
		default: 
			qType = "ERROR";	
		}
		
		return qType;
	}
	
	private String lookupQClass(int QClassNum) {
//		System.out.print("qClass NUM: ");
//		System.out.println(QClassNum);
		String qClass;
		switch (QClassNum) {
		case 1:  qClass = "IN";
        	break;
		case 2:  qClass = "CS";
		        break;
		case 3:  qClass = "CH";
		        break;
		case 4:  qClass = "HS";
		        break;
		default: 
			qClass = "ERROR";
		}
		return qClass;
		    
	}
	
	public static DNSQuestion decodeQuestion(InputStream inputStream, DNSMessage message) {
		DNSQuestion dnsQuestion = new DNSQuestion();
		try {
			//TODO: assumes only 1 domain name currently
			ArrayList<String []> domainNames = new ArrayList<String []>();
			String [] domainName = message.readDomainName(inputStream);
			domainNames.add(domainName);
			dnsQuestion.setDomainNames(domainNames);
			dnsQuestion.qTypeNum = ioUtils.decode2Bytes(inputStream);
			dnsQuestion.qType = dnsQuestion.lookupQType(dnsQuestion.qTypeNum);
			dnsQuestion.qClassNum = ioUtils.decode2Bytes(inputStream);
			dnsQuestion.qClass = dnsQuestion.lookupQClass(dnsQuestion.qClassNum);
			System.out.println(dnsQuestion.toString());
			
		} catch (IOException e) {
			System.out.println("Something went wrong reading the question");
			e.printStackTrace();
		}
		return dnsQuestion;
	}
	
	public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String,Integer> domainNameLocations) throws IOException {
		writeDomainNames(outputStream, domainNameLocations);
		ioUtils.writeBytes(2, qTypeNum, outputStream);
		ioUtils.writeBytes(2, qClassNum, outputStream);
	
	}
	
	private void writeDomainNames(ByteArrayOutputStream outputStream, HashMap<String,Integer> domainLocations) throws IOException  {
		for(String [] arr : domainNames) {
			DNSMessage.writeDomainName(outputStream, domainLocations, arr);
			
		}
	}
	@Override
	public String toString() {
		return "DNSQuestion [domainNames=" + domainNames + ", qType=" + qType + ", qTypeNum=" + qTypeNum + ", qClass="
				+ qClass + ", qClassNum=" + qClassNum + "]";
	}
}
