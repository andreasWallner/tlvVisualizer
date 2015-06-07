package at.innovative_solutions.tlv;

import java.nio.ByteBuffer;

// TODO implement equals, simplify test cases
public class ID {
	public static final int CLASS_UNIVERSAL = 0;
	public static final int CLASS_APPLICATION = 1;
	public static final int CLASS_CONTEXT = 2;
	public static final int CLASS_PRIVATE = 3;
	
	final int _tagClass;
	final boolean _isPrimitive;
	final int _tagNumber;
	final int _tagLength;
	
	public ID(int tagClass, boolean isPrimitive, int tagNumber) {
		this(tagClass, isPrimitive, tagNumber, 0);
	}
	
	public ID(int tagClass, boolean isPrimitive, int tagNumber, int tagLength) {
		_tagClass = tagClass;
		_isPrimitive = isPrimitive;
		_tagNumber = tagNumber;
		_tagLength = tagLength;
	}
	
	public int getTagClass() {
		return _tagClass;
	}
	
	public boolean isPrimitive() {
		return _isPrimitive;
	}
	
	public int getTagNumber() {
		return _tagNumber;
	}
	
	public byte[] toBytes() {
		if(_tagNumber <= 30 && _tagLength == 0) {
			int b = _tagClass << 6 | (_isPrimitive ? 0 : 1 << 5) | _tagNumber;
			return new byte[] { (byte) b };
		} else {
			final int nrOfBinDigits = (32 - Integer.numberOfLeadingZeros(_tagNumber));
			int nrOfBlocks = nrOfBinDigits / 7 + (nrOfBinDigits % 7 != 0 ? 1 : 0);
			nrOfBlocks = Integer.max(nrOfBlocks, _tagLength);

			byte[] result = new byte[nrOfBlocks + 1];
			result[0] = (byte) (_tagClass << 6 | (_isPrimitive ? 0 : 1 << 5) | 0x1F);
			for(int i = 1; i <= nrOfBlocks; i++) {
				final byte blockData = (byte) ((_tagNumber >> ((nrOfBlocks - i) * 7)) & 0x7F);
				result[i] = (byte) ((i != nrOfBlocks ? (byte)0x80 : (byte)0) | blockData);
			}
			
			return result;
		}
	}
	
	// TODO correct exception type
	public long toLong() {
		byte[] bytes = toBytes();
		if(bytes.length > 8)
			throw new RuntimeException("can not convert ID to long, values too big");
		long result = 0;
		for(int n = 0; n < bytes.length; n++)
			result = result << 8 | (bytes[n] & 0xff);
		return result;
	}
	
	// TODO protect against overflow
	public static ID parseID(ByteBuffer octets) {
		byte first = (byte) octets.get();
		
		final int tagClass = (first >> 6) & 0x3;
		final boolean isPrimitive = (first & (1 << 5)) == 0;
		int tagNumber = first & 0x1F;
		
		if(tagNumber == 0x1F) {
			tagNumber = 0;
			byte piece;
			do {
				piece = octets.get();
				tagNumber = (tagNumber << 7) + (piece & 0x7F);
			} while((piece & 0x80) != 0);
		}
		
		return new ID(tagClass, isPrimitive, tagNumber);
	}
	
	public boolean equals(final Object other) {
		if(!(other instanceof ID))
			return false;
		
		return this._tagClass == ((ID)other)._tagClass
				&& this._isPrimitive == ((ID)other)._isPrimitive
				&& this._tagNumber == ((ID)other)._tagNumber
				&& this._tagLength == ((ID)other)._tagLength;
	}
	
	public boolean equalContents(final Object other) {
		if(!(other instanceof ID))
			return false;
	
		return this._tagClass == ((ID)other)._tagClass
				&& this._isPrimitive == ((ID)other)._isPrimitive
				&& this._tagNumber == ((ID)other)._tagNumber;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("ID(");
		str.append(_tagClass);
		str.append(", ");
		str.append(_isPrimitive);
		str.append(", ");
		str.append(_tagNumber);
		str.append(")");
		
		return str.toString();
	}
}
