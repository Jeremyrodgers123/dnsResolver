package dnsResolver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DNSHeaderTest {

	@BeforeEach
	void setUp() throws Exception {
		
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		//fail("Not yet implemented");
	}
	
	@Test
	void testBuffer() throws FileNotFoundException {
		DNSHeader header = new DNSHeader();
		File file = new File("/Users/jeremyrodgers/jeremyrodgers123/cs6014/dnsResolver/test.txt");
		FileInputStream fs = new FileInputStream(file);
		header.decodeHeader(fs);
	}

}
