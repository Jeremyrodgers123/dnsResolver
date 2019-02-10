package dnsResolver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DNSQuestionTest {
	DNSQuestion question = new DNSQuestion();;
	HashMap<String, Integer> domainLocations = new HashMap<String, Integer>();
	ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
	@BeforeEach
	void setUp() throws Exception {
		String [] domain1 = new String [] {"google", "com"};
		String [] domain2 = new String [] {"google", "uk"};
		String [] domain3 = new String [] {"mail", "google", "com"};
		question.domainNames.add(domain1);
		question.domainNames.add(domain2);
		question.domainNames.add(domain3);
		
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testWriteBytes() {
		
		try {
			question.writeBytes(outputStream, domainLocations);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fail("Not yet implemented"); 
	}

	@Test
	void testTestWriteNewLabel() {
		//fail("Not yet implemented");
	}

}
