package dnsResolver;

import java.util.HashMap;

public class DNSCache {
	HashMap<DNSQuestion, DNSRecord> cache;
	
	DNSCache(){
		cache = new HashMap<DNSQuestion, DNSRecord> ();
	}
	
	public synchronized boolean add(DNSQuestion question, DNSRecord answer) {
		if(cache.containsKey(question)) {
			return false;
		}
		cache.put(question, answer);
		return true;
	}
	
	public synchronized boolean contains(DNSQuestion question) {
		DNSRecord answer = cache.get(question);
		if(answer == null) {
			return false;
		}
		if(!answer.timestampValid()) {
			cache.remove(question);
			return false;
		};
		return true;
	}
	
	public synchronized DNSRecord get(DNSQuestion question) {
		DNSRecord answer = cache.get(question);
		if(answer == null) {
			return null;
		}
		if(!answer.timestampValid()) {
			cache.remove(question);
			return null;
		};
		return answer;
	}
	
	public synchronized DNSRecord remove(DNSQuestion question) {
		
		return cache.remove(question);
	}
	
	
}

