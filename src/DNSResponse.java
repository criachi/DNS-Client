import java.util.ArrayList;


public class DNSResponse {

	// reading from response header
	private byte[] id;
	// Store most of these in case necessary for analysis of packet in future.
	private int QR, OPCODE, AA, TC, RD, RA, Z, RCODE;
	private int ANCOUNT, NSCOUNT, ARCOUNT;

	// attributes that come from DNSClient to act as reference to compare for validation checks 
	private byte[] randomRequestID;
	private byte[] receivedData;
	private int requestLength;
	
	// grouping together all responses 
	private ArrayList<DNSRecord> answerRecords;
	private ArrayList<DNSRecord> additionalRecords;
	

	public DNSResponse(byte[] receivedData, int requestLength, byte[] randomRequestID) {
		this.receivedData = receivedData;
		this.requestLength = requestLength;
		this.randomRequestID = randomRequestID;
		this.id = new byte[2];
	}

	public void interpretResponse() {

		// header done
		extractHeader();
		checkIDMatches(); 
		checkRCODE();
		int offset = requestLength;		
		
		answerRecords = new ArrayList<DNSRecord>();
		
		for (int i=0; i < ANCOUNT; i++) {
			answerRecords.add(new DNSRecord(receivedData, offset, this.AA));
			offset = answerRecords.get(i).getOffset();
			if (offset == -1) {
				answerRecords.remove(i);
				break;
			}
		}
		
		for (int i=ANCOUNT; i < NSCOUNT + ANCOUNT; i++) {
			answerRecords.add(new DNSRecord(receivedData, offset, this.AA));
			offset = answerRecords.get(i).getOffset();
			if (offset == -1) {
				answerRecords.remove(i);
				break;
			}
		}

		additionalRecords = new ArrayList<DNSRecord>();
		for (int i=0; i < ARCOUNT; i++) {
			additionalRecords.add(new DNSRecord(receivedData, offset, this.AA));
			offset = additionalRecords.get(i).getOffset();
			if (offset == -1) {
				answerRecords.remove(i);
				break;
			}
		}
	}

	private void checkIDMatches() {
		for(int i = 0; i < 2; i++) {
			if (id[i] != randomRequestID[i]) {
				System.out.println("ERROR\tThe response ID and the request ID do not match");
				System.exit(1);
			}
		}
	}

	private void checkRCODE() {
		switch(RCODE) {
		case 1: 
		{
			System.out.println("ERROR\tThe name server was unable to interpret the query");
			System.exit(1);
		}
		case 2:
		{
			System.out.println("ERROR\tServer failure: the name server was unable to process the query due to a problem with the name server");
			System.exit(1);
		}
		case 3:
		{
			System.out.println("NOTFOUND");
			System.exit(1);
		}

		case 4:
		{
			System.out.println("ERROR\tNot implemented: The name server does not support the requested kind of query");
			System.exit(1);
		}
		case 5:
		{
			System.out.println("ERROR\tRefused: the name server refuses to perform the requested operation for policy reasons");
			System.exit(1);
		}
		default:;
		}
	}

	private void extractHeader() {

		id[0] = receivedData[0];
		id[1] = receivedData[1];
		byte qrToRDFlags = receivedData[2];
		QR = (qrToRDFlags & (0b10000000)) >> 7;
		OPCODE = (qrToRDFlags & (0b01111000)) >> 3;
		AA = (qrToRDFlags & (0b00000100)) >> 2;
		TC = (qrToRDFlags & (0b00000010)) >> 1;
		RD = (qrToRDFlags & (0b00000001));
		byte RAToRCODEFlags = receivedData[3];
		RA = (RAToRCODEFlags & (0b10000000)) >> 7;
		Z = (RAToRCODEFlags & (0b01110000)) >> 4;
		
		RCODE = (RAToRCODEFlags & (0b00001111));
		
		ANCOUNT = (receivedData[6] << 8) | receivedData[7];
		NSCOUNT = (receivedData[8] << 8) | receivedData[9];
		ARCOUNT = (receivedData[10] << 8) | receivedData[11];
	}


	public void print() {
		if(RA == 0) { // means recursive queries are not supported by the server; dsnt matter if the server was able to find the domain name by iterative query, it shld still display this error msg 
			System.out.println( "The server does not support recursive queries\n");
			return;
		}

		if(this.answerRecords.size()> 0) {
			System.out.println("***Answer Section (" + this.answerRecords.size() + ") records***");
			for (DNSRecord r : answerRecords) {
				r.print();
			}
		}
		
		if(additionalRecords.size() > 0) {
			System.out.println("***Additional Section (" + additionalRecords.size() + ") records" + "\n");
			for (DNSRecord r : additionalRecords) {
				r.print();
			}
		}
	}
}

