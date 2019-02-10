package dnsResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ioUtils {
	public static int decode2Bytes(InputStream inputStream) throws IOException {
		int ret;
		int byte1 = inputStream.read();
		int byte2 = inputStream.read();
		if(byte1 == -1 || byte2 == -1) throw new Error("Error reading 2 Bytes");
		ret = (byte1 << 8) | byte2;
		return ret;
	}
	
	public static long decode4Bytes(InputStream inputStream) throws IOException {
		long ret;
		int byte1 = inputStream.read();
		int byte2 = inputStream.read();
		int byte3 = inputStream.read();
		int byte4 = inputStream.read();
		if(byte1 == -1 || byte2 == -1) throw new Error("Error reading 2 Bytes");
		ret = ((byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4) & 0x00000000FFFFFFFF;
		return ret;
		
	}
	public static boolean decodeTrueFalse(int shift, int byteVal) {
		int val = (byteVal >> shift) & 0x01;
		assert(val == 1 || val == 0);
		return val == 1;
	}
	
	public static void writeBytes(int numBytes, int resource, OutputStream outputStream) throws IOException {
		byte byteToWrite;
		int byteCount = numBytes;
		while(byteCount > 0) {
			int shiftAmt = (byteCount * 8) - 8;
			byteToWrite = (byte)(resource >> shiftAmt);
			outputStream.write(byteToWrite);
			byteCount--;
		}
	}
	
	//TODO: consolidate writeByte methods with generics
	public static void writeBytes(int numBytes, long resource, OutputStream outputStream) throws IOException {
		byte byteToWrite;
		int byteCount = numBytes;
		while(byteCount > 0) {
			int shiftAmt = (byteCount * 8) - 8;
			byteToWrite = (byte)(resource >> shiftAmt);
			outputStream.write(byteToWrite);
			byteCount--;
		}
	}
	
	public static void writeBytes(String data, OutputStream outputStream) throws IOException {
		outputStream.write(data.getBytes());
	}
	
	public static void writeBytes(byte [] data, OutputStream outputStream) throws IOException {
		outputStream.write(data);
	}
	
	public static char getLowercaseChar(int asciiVal) {
		char ret;
		if(asciiVal > 65 && asciiVal < 91) {
			asciiVal = asciiVal - 65 + 97; // lowercase captial letter
		}
		ret = (char) asciiVal;
		return ret;
	}
}
