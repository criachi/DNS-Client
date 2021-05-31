import java.nio.ByteBuffer;
import java.util.Random;


public class DNSRequest {
	private String domainName;
	private QueryType qType; 
	public byte[] randomID;

	public DNSRequest(String domainName, QueryType qType) {
		this.domainName = domainName;
		this.qType = qType;
		randomID = new byte[2];
		Random rd = new Random();
		rd.nextBytes(randomID); // generates random ID made up of 2 bytes 
	}

	public byte[] getRandomID() {
		return this.randomID;
	}

	public byte[] makeHeader() {
		ByteBuffer header = ByteBuffer.allocate(12);
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
		return header.array();
	}

	public int[] calcLabelLengths() {
		String[] labels = domainName.split("\\.");
		int[] labelSizes = new int[labels.length];
		for(int i = 0; i<labels.length; i++) {
			labelSizes[i] = labels[i].length();
		}
		return labelSizes;
	}

	public byte[] makeBody() {
		int qname_size = 0;
		int[] labelLengths = calcLabelLengths();
		for(int i = 0; i < labelLengths.length; i++) {
			qname_size += labelLengths[i] + 1; 
		}
		// size of body is size of QNAME, QTYPE, and QCLASS
		ByteBuffer body = ByteBuffer.allocate(qname_size + 5);

		String[] labels = domainName.split("\\.");
		for(int i = 0; i < labelLengths.length; i++) {
			body.put((byte) labelLengths[i]); 
			body.put(labels[i].getBytes());
		}
		body.put((byte) 0); // this is to indicate the end of a domain name 
		body.put((byte) 0);

		switch (qType) {
		case A:
		{
			body.put((byte) 0x0001);
			break;
		}
		case NS:
		{
			body.put((byte) 2);
			break;
		}
		case MX:
		{
			body.put((byte) 0x000f);
			break;
		}
		default:
			break;
		}

		body.put((byte) 0);
		body.put((byte) 1);

		return body.array();
	}
}
