import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Random;

public class TestClass {
	public static void main(String args[]) throws Exception {
		System.out.println("goodbyee");
		testMissingTParse();
//		testMissingAtSign();
//		testBadIPAddr();
//		testBadIPAddr2();
//		testBadIPAddr3();
//		testNoT();
//		testNoDomain();
//		testRequestHeaderCreation();
//		testRequestCreation();
//		testGoogleNS(); // WORKS
		testWithFacebook(); // WORKS This is ns
//		testWMcGill(); // WORKS this is IP
//		testMcGillNOWWW(); // WORKS This is MX
//		testGoogle(); //Different query tests
//		testDomainGetterWithPtr();
//		testDomainGetterNoPtr();
	}
	
	private static void testAllArgsParse() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-t", "10" ,"-r", "5","-ns", "@123.123.12.1", "www.mcgill.ca"};
		client.parse(x);
		client.testParse();
	}
	
	private static void testNoT() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-r", "5","-ns", "@123.123.12.1", "www.mcgill.ca"};
		client.parse(x);
		client.testParse();
	}

	private static void testNoDomain() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-r", "5","-ns", "@123.123.12.1"};
		client.parse(x);
		client.testParse();
	}
	
	private static void testMissingTParse() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","-r", "5","-ns", "@123.123.12.1", "www.mcgill.ca"};
		client.parse(x);
		client.testParse();
	}
	private static void testMissingAtSign() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","10","-r", "5","-ns", "123.123.12.1", "www.mcgill.ca"};
		client.parse(x);
		client.testParse();
	}
	private static void testBadIPAddr() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","10","-r", "5","-ns", "@123123121", "www.mcgill.ca"};
		client.parse(x);
		client.testParse();
	}
	
	private static void testBadIPAddr2() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","10","-r", "5","-ns", "@-123.123.121", "www.mcgill.ca"};
		client.parse(x);
		client.testParse();
	}
	private static void testBadIPAddr3() throws SocketException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","10","-r", "5","-ns", "@257.123.121", "www.mcgill.ca"};
		client.parse(x);
		client.testParse();
	}
	
	// This works well.
	private static void testRequestHeaderCreation() {
		ByteBuffer header = ByteBuffer.allocate(12);
		byte[] randomID = new byte[2];
		Random rd = new Random();
		rd.nextBytes(randomID); // generates random ID made up of 2 bytes 
		header.put(randomID);

		//QR = 0
		//OPCODE = 0000
		//AA = 0
		//TC = 0 
		//RD = 1
		//Hence put in 0 0000 0 0 1
		header.put((byte) 1); // this is for QR -> RD

		//RA = 0
		//Z = 000
		//RCODE = 0000
		//Hence put in 0 000 0000
		header.put((byte) 0); // this is for RA -> RCODE

		// QD COUNT TAKES 2 BYTES
		header.put((byte) 0);
		header.put((byte) 1);

		// AN COUNT TAKES 2 BYTES 
		header.put((byte) 0);
		header.put((byte) 0);

		// ns count and AR COUNT TOO 
		header.put((byte) 0);
		header.put((byte) 0);

		header.put((byte) 0);
		header.put((byte) 0);
		
		for (int i=0; i< 12; i++) {
			System.out.println(header.array()[i]);
		}
	}
	private static void testRequestCreation() throws SocketException {

		DnsClient client = new DnsClient();
		String[] x = {"@8.8.8.8", "www.facebook.com"};
		client.parse(x);
		DNSRequest req = new DNSRequest(client.getDomainName(), client.getQueryType());
		byte[] header = req.makeHeader();
		byte[] body = req.makeBody();
		ByteBuffer sendData = ByteBuffer.allocate(header.length + body.length);
		sendData.put(header);
		sendData.put(body);
		for (int i=0; i< sendData.capacity(); i++) {
			System.out.println(sendData.array()[i]);
		}
	}
	
	private static void testWithFacebook() throws IOException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","20","-ns", "@8.8.8.8", "facebook.com"};
		client.parse(x);
		client.summarizeQuery();
		client.query();
	}

	private static void testMcGillNOWWW() throws IOException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","20","-mx", "@8.8.8.8", "mcgill.ca"};
		client.parse(x);
		client.summarizeQuery();
		client.query();
	}
	private static void testGoogle() throws IOException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","20","-mx", "@8.8.8.8", "google.com"};
		client.parse(x);
		client.summarizeQuery();
		client.query();
	}
	
	private static void testWMcGill() throws IOException {
		DnsClient client = new DnsClient();
		String[] x = {"@8.8.8.8", "www.mcgill.ca"};
		client.parse(x);
		client.summarizeQuery();
		client.query();
	}	
	
	private static void testGoogleNS() throws IOException {
		DnsClient client = new DnsClient();
		String[] x = {"-t","20","-ns", "@8.8.8.8", "google.com"};
		client.parse(x);
		client.summarizeQuery();
		client.query();
	}
	
//	private static void testDomainGetterNoPtr() throws SocketException {
////		DnsClient client = new DnsClient();
//		String[] x = {"@8.8.8.8", "www.facebook.com"};
//		byte b1 = (byte) 0b11000000;
//		byte b2 = (byte) 0b00000000;
//		byte[] abc = {(byte) 3, (byte) 'w', (byte) 'w', (byte) 'w', (byte) 2, (byte) 'a', (byte) 'b', (byte) 0, b1, b2};
//		DNSResponse r = new DNSResponse(abc, 0, null, null, abc);
//		System.out.println(r.getRecordName(0));
//	}
//	private static void testDomainGetterWithPtr() throws SocketException {
////		DnsClient client = new DnsClient();
//		String[] x = {"@8.8.8.8", "www.facebook.com"};
//		byte b1 = (byte) 0b11000000;
//		byte b2 = (byte) 0b00000000;
//		byte[] abc = {(byte) 3, (byte) 'w', (byte) 'w', (byte) 'w', (byte) 2, (byte) 'a', (byte) 'b', (byte) 0, (byte) 2, (byte) 'a', (byte) 'b', b1, b2};
//		DNSResponse r = new DNSResponse(abc, 0, null, null, abc);
//		System.out.println(r.getRecordName(8));
//	}
}
