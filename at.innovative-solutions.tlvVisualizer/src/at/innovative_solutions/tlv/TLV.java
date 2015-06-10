package at.innovative_solutions.tlv;

import java.util.LinkedList;
import java.util.List;
import java.nio.ByteBuffer;

abstract public class TLV implements Formattable {
	protected ID _id;
	protected boolean _lengthIndefinite;
	protected TLV _parent;
	
	TLV(final ID id, boolean lengthIndefinite) {
		_id = id;
		_lengthIndefinite = lengthIndefinite;
		_parent = null;
	}
	
	public boolean equals(final Object other) {
		if(!(other instanceof TLV))
			return false;
		
		return _id.equals(((TLV) other)._id)
				&& _lengthIndefinite == ((TLV) other)._lengthIndefinite;
	}
	
	public ID getID() {
		return _id;
	}
	
	public boolean isIndefiniteLength() {
		return _lengthIndefinite;
	}
	
	public abstract int getLength();
		
	// TODO check for leftover bytes at the end
	// error in case we are extracting too much
	public static TLV parseTLV(ByteBuffer octets) {
		TLV result;
		final ID id = ID.parseID(octets);
		Integer length = parseLength(octets);

		boolean indefiniteLength = false;
		int additionalBytes = 0;

		if(length == null) {
			length = findEnd(octets.duplicate());
			additionalBytes = 2;
			indefiniteLength = true;
		}
		
		if(length > octets.remaining())
			throw new ParseError("tlv", "Frame too short for expected data length (" + length + " bytes)");
		
		if(id.isPrimitive()) {
			byte[] data = new byte[length];
			octets.get(data);
			result = new PrimitiveTLV(id, data, indefiniteLength);
		} else {
			final ByteBuffer subOctets = octets.duplicate();
			subOctets.position(octets.position());
			subOctets.limit(octets.position() + length);
			List<TLV> subTLVs = new LinkedList<TLV>();
			while(subOctets.hasRemaining())
				subTLVs.add(parseTLV(subOctets));
			result = new ConstructedTLV(id, subTLVs, indefiniteLength);
			
			octets.position(octets.position() + length);
		}
		
		octets.position(octets.position() + additionalBytes);
		return result;
	}
	
	public static List<TLV> parseTLVs(ByteBuffer octets) {
		final List<TLV> tlvs = new LinkedList<TLV>();
		
		while(octets.remaining() != 0)
			tlvs.add(parseTLV(octets));
		
		return tlvs;
	}
	
	public static Integer parseLength(ByteBuffer octets) {
		byte first = octets.get();
		
		if(first == (byte) 0xff) {
			throw new ParseError("length", "Invalid length byte (first byte 0xff)");
		}
		
		// short form
		if((first & 0x80) == 0)
			return (int)first;
		
		// indefinite form
		if(first == (byte) 0x80)
			return null;
		
		// long form
		first = (byte) (first & 0x7f);
		
		int length = 0;
		while(first-- != 0) {
			byte piece = octets.get();
			if((length & (0xff << 7*8)) != 0 || (length & (0x80 << 6*8)) != 0)
				throw new ParseError("internal", "length too big to fit into an integer");
			length = (length << 8) + java.lang.Byte.toUnsignedInt(piece);
		}
		return length;
	}
	
	static int findEnd(final ByteBuffer octets) {
		byte last = 0x01;
		int startOffset = octets.position();
		while(true) {
			if(!octets.hasRemaining())
				throw new ParseError("end", "could not find end marker for TLV with indefinite length");
			
			byte current = octets.get();
			if(last == 0 && current == 0) {
				return octets.position() - startOffset - 2;
			}
			last = current;
		}		
	}
	
	public void setParent(final TLV parent) {
		_parent = parent;
	}
	
	public TLV getParent() {
		return _parent;
	}
}
