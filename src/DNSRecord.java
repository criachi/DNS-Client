
public class DNSRecord {

	private QueryType qType;
	private String name = "";
	private int offset;
	private boolean incOffset;
	private final int CLASS = 1;
	private int TTL;
	private int rDataLength;
	private String rData;
	private int preference = 0;
	private int AA;
	
	
	public DNSRecord(byte[] receivedData, int offset, int AA) {
		this.offset = offset;
		this.extractAnswer(receivedData);
		this.AA = AA;
	}
	
	private void extractAnswer(byte[] receivedData) {
		this.setName(receivedData, offset);
		int type = (receivedData[offset++] << 8) | receivedData[offset++];
		if (!this.setQueryType(type)) {
			return;
		}
		
		int currClass = (receivedData[offset++] << 8) | receivedData[offset++];
		
		if (currClass != CLASS) {
			this.offset = -1;
			return;
		}
		offset++;
		TTL = (receivedData[offset++] << 8) | receivedData[offset++];
		rDataLength = (receivedData[offset++] << 8) | receivedData[offset++];
		
		rData = this.getRDATA(receivedData);
	}
	
	public int getOffset() {
		return this.offset;
	}
	

	public boolean setQueryType(int type) {
		switch (type) {
		case 0x001:
		{
			this.qType = QueryType.A;
			return true;
		}
		case 0x002:
		{
			this.qType = QueryType.NS;
			return true;
		}
		case 0x00f:
		{
			this.qType = QueryType.MX;
			return true;
		}
		case 0x005:
		{
			this.qType = QueryType.CNAME;
			return true;
		}
		default:
		{
			this.offset = -1;
			return false;
		}
		}
	}
	

	/////////////// COMPRESSION ALGORITHM /////////////////////////////////////
	private boolean usePointer(byte b) {
		// If the byte starts with 11, the following 14 bits correspond
		// To a pointer pointing to the offset we have to check for the rest of the domain name
		return (b & 0b11000000) == 0b11000000;
	}
	private int getPointerValue(byte b1, byte b2) {
		//The 14 last bits return an offset that we need to go to.
		return ((b1 & 0b00111111) << 8) | b2;
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(byte[] receivedData, int location) {
		if (receivedData[location] != 0) {
			this.incOffset = true;
			this.name = this.getNameHelper(receivedData, location).substring(1);
		}	
	}
	
	private String getNameHelper(byte[] receivedData, int location) {
		
		if (receivedData[location] == 0) {
			return "";
		}

		String domName = "."; // So the beginning will always have a '.' (we trim this in the helper)
		if (usePointer(receivedData[location])) {
			offset += 2;
			this.incOffset = false;
			return getNameHelper(receivedData, getPointerValue(receivedData[location], receivedData[location+1]));
		}

		int index =  Byte.toUnsignedInt(receivedData[location]);
		
		for (int i=1; i <= index; i++) {
			domName += (char) Byte.toUnsignedInt(receivedData[i+location]);
		}
		
		if (incOffset) {
			offset +=index + 1;
		}
		
		return domName += getNameHelper(receivedData, location + index + 1);	
	}
	
	private String getRDATA(byte[] receivedData) {
		if (receivedData[offset] == 0) {
			return "";
		}
		
		this.incOffset = true;
		switch (this.qType) {
		case A:
		{
			return this.getIPHelper(receivedData, offset).substring(1);
		}
		case MX:
		{
			offset++;
			this.preference = (receivedData[offset++] << 8) | receivedData[offset++];
			return this.getNameHelper(receivedData, offset).substring(1);
		}
		case NS:
		{
			offset++;
			return this.getNameHelper(receivedData, offset).substring(1);
		}
		default:
		{
			offset++;
			return this.getNameHelper(receivedData, offset).substring(1);	
		}
		}
	}
	
	public String getIPHelper(byte[] receivedData, int location) {
		if (receivedData[location] == 0) {
			return "";
		}

		String domName = "."; // So the beginning will always have a '.' (we trim this in the helper)

		int index =  Byte.toUnsignedInt(receivedData[location]);
		for (int i=1; i <= index; i++) {
			domName += Byte.toUnsignedInt(receivedData[i+location]);
			if (i != index) {
				domName += ".";
			}
		}
		
		if (incOffset) {
			offset +=index + 1;
		}

		return domName += getIPHelper(receivedData, location + index + 1);	
	}
	///////////////////////////////////////////////////////////////////////////
	
	public void print() {
		
		switch (this.qType) {
		case A:
		{
			System.out.println("IP\t" + rData + "\t" + TTL + "\t" +  (AA==1 ? "auth": "nonauth"));	
			break;	
		}
		case NS:
		{
			System.out.println("NS\t" + rData + "\t" + TTL + "\t" +  (AA==1 ? "auth": "nonauth"));
			break;
		}
		case MX:
		{
			System.out.println("MX\t" + rData + "\t" + preference + "\t" + TTL + "\t" +  (AA==1 ? "auth": "nonauth"));
			break;
		}
		case CNAME:
		{
			System.out.println("CNAME\t" + rData + "\t" + TTL + "\t" + (AA==1 ? "auth": "nonauth"));
			break;
			
		}
		default:
		{
			System.out.println("What");
		}
		}
	}
}
