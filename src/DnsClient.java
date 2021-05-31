import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class DnsClient {
	private int timeout = 5;
	private int maxRetries = 3;
	private int portNb = 53;
	private QueryType qType = QueryType.A;
	private String domainName;
	private int[] ipAddrAsInt; // This is just to print it out nicely
	private byte[] ipAddr;
	private DatagramSocket clientSocket;
	private int numRetries = 0;
	private long deltaTime = 0;
	private long startT = 0;
	private byte[] randomRequestID;
	
	private final int MSG_LENGTH = 1024;

	// getters for validation checks in response packets received
	public String getDomainName() {
		return this.domainName;
	}
	
	public QueryType getQueryType() {
		return this.qType;
	}
	
	public DnsClient() throws SocketException {
		clientSocket = new DatagramSocket();

	}

	public void parse(String[] input) {
		int j = 0;
		while(j < input.length) {

			switch(input[j]) {

			case "-t": 
			{
				try {
					this.timeout = Integer.parseUnsignedInt(input[++j]);
				} catch(NumberFormatException e) {
					System.out.println("ERROR\tA positive integer is expected for timeout");
				} 
				break;
			} 
			case "-r":
			{
				try {
					this.maxRetries = Integer.parseUnsignedInt(input[++j]);
				} catch(NumberFormatException e) {
					System.out.println("ERROR\tA positive integer is expected for max retries");
				}
				break;

			} 
			case "-mx": 
			{
				this.qType = QueryType.MX;
				break;

			} 
			case "-ns":
			{
				this.qType = QueryType.NS;
				break;		
			}
			case " ": 
			{
				break;
			}
			default:
			{
				if (input[j].charAt(0) == '@') {
					try {
						String[] ipNb = input[j].substring(1).split("\\.");
						this.ipAddr = new byte[ipNb.length];
						this.ipAddrAsInt = new int[ipNb.length];
						for(int i = 0; i < ipNb.length; i++) {
							int ipNumber = Integer.parseUnsignedInt(ipNb[i]);
							if(ipNumber > 255) {
								System.out.println("ERROR\tServer IP address contains an integer outside of the range [0, 255]");
								System.exit(1);
							}
							this.ipAddr[i] = (byte) (ipNumber);
							this.ipAddrAsInt[i] = ipNumber;
						}
					} catch(NumberFormatException e) {
						System.out.println("ERROR\tServer IP address can only contain positive numbers from 0 to 255");
						System.exit(1);
					}
					try {
						this.domainName = input[++j];
					} catch (IndexOutOfBoundsException e) {
						System.out.println("ERROR\tInput must be provided of the form: [-t t] [-r r] [-p p] [-mx|-ns] @server name");
						System.exit(1);
					}
				} else {
					System.out.println("ERROR\tInput must be provided of the form: [-t t] [-r r] [-p p] [-mx|-ns] @server name");
					System.exit(1);
				}
			}	

			}
			j++;
		}

	}

	public void testParse() {
		System.out.println("Timeout is: " + this.timeout);
		System.out.println("Domain name is: " + this.domainName);
		for (int i=0; i<this.ipAddr.length; i++) {
			System.out.println("Server addr " + i + " is " + this.ipAddr[i]);
		}
		System.out.println("Max retries is: " + this.maxRetries);
		System.out.println("Query type is: " + this.qType.toString());
	}

	public void summarizeQuery() {
		System.out.println("DnsClient sending request for " + this.domainName);
		System.out.print("Server: ");
		int i=0;
		for (; i<this.ipAddr.length-1; i++) {
			System.out.print(ipAddrAsInt[i] + ".");
		}
		System.out.println(ipAddrAsInt[i]);
		System.out.println("Request type: " + this.qType.toString());
	}

	public void query() throws IOException {
		DNSRequest req = new DNSRequest(domainName, qType);
		byte[] header = req.makeHeader();
		byte[] body = req.makeBody();
		this.randomRequestID = req.getRandomID(); //store request ID as an instance variable
		ByteBuffer sendData = ByteBuffer.allocate(header.length + body.length);
		sendData.put(header);
		sendData.put(body);
		byte[] receivedData = new byte[MSG_LENGTH]; 
		
		startT = System.currentTimeMillis();
		for (int i=0; i< this.maxRetries; i++) {
			try {
				
				this.clientSocket.setSoTimeout(this.timeout * 1000);
				
				DatagramPacket sentPacket = new DatagramPacket(sendData.array(), sendData.capacity(), InetAddress.getByAddress(ipAddr), this.portNb);
				DatagramPacket recPacket = new DatagramPacket(receivedData, MSG_LENGTH);
				this.clientSocket.send(sentPacket);
				this.clientSocket.receive(recPacket);
				deltaTime =  System.currentTimeMillis() - startT;
				this.clientSocket.close();
				
			} catch (SocketTimeoutException e) {
				System.out.println(i);
				if (i == this.maxRetries-1) {
					System.out.println("Failed to connect");
					System.out.println(e.getMessage());
					break;
				} else {
					continue;
				}
			}
			numRetries = i;
			readMessage(receivedData, sendData.capacity());
			break;
		}
	}

	public void readMessage(byte[] receivedData, int requestLength) throws IOException {
		//Note that the first answer starts exactly at offset = requestLength
		DNSResponse resp = new DNSResponse(receivedData, requestLength, randomRequestID);
		resp.interpretResponse();
		System.out.println("Response received after " + deltaTime + " seconds (" + numRetries + " retries)");
		resp.print();
		
	}

	public static void main(String args[]) throws Exception {
		DnsClient client = new DnsClient();
		client.parse(args);
		client.summarizeQuery();
		client.query();
	}

}
