package at.innovative_solutions.tlv;

import java.util.LinkedList;
import java.util.List;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import at.innovative_solutions.tlv.ErrorTLV.ParseStage;

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
	
	
	public static TLV parseTLV(ByteBuffer octets) {
		return realParseTLV(octets, true);
	}
	
	public static TLV parseTLVWithErrors(ByteBuffer octets) {
		return realParseTLV(octets, false);
	}
	
	// TODO check for leftover bytes at the end
	// error in case we are extracting too much
	private static TLV realParseTLV(ByteBuffer octets, boolean throwOnError) {
		ID id = null;
		Integer length = null;
		ErrorTLV.ParseStage stage = ParseStage.ParsingID;
		boolean indefiniteLength = false;
		try {
			TLV result;
			id = ID.parseID(octets);
			stage = ParseStage.ParsingLength;
			length = parseLength(octets);
	
			int additionalBytes = 0;
	
			if(length == null) {
				stage = ParseStage.FindingEnd;
				length = findEnd(octets.duplicate());
				additionalBytes = 2;
				indefiniteLength = true;
			}
			
			stage = ParseStage.GettingData;
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
					subTLVs.add(realParseTLV(subOctets, throwOnError));
				result = new ConstructedTLV(id, subTLVs, indefiniteLength);
				
				octets.position(octets.position() + length);
			}
			
			octets.position(octets.position() + additionalBytes);
			return result;
		} catch(ParseError e) {
			if(throwOnError)
				throw e;
			byte[] remaining = new byte[octets.remaining()];
			octets.get(remaining);
			return new ErrorTLV(stage, id, e.getMessage(), length, indefiniteLength, remaining);
		}
	}
	
	public static List<TLV> parseTLVs(ByteBuffer octets) {
		final List<TLV> tlvs = new LinkedList<TLV>();
		
		while(octets.remaining() != 0)
			tlvs.add(parseTLV(octets));
		
		return tlvs;
	}
	
	public static List<TLV> parseTLVsWithErrors(ByteBuffer octets) {
		final List<TLV> tlvs = new LinkedList<TLV>();
		
		while(octets.remaining() != 0)
			tlvs.add(parseTLVWithErrors(octets));
		
		return tlvs;
	}
	
	public static Integer parseLength(ByteBuffer octets) {
		try {
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
				if((length & 0xff000000) != 0 || (length & 0x800000) != 0)
					throw new ParseError("internal", "length too big to fit into an integer");
				length = (length << 8) + java.lang.Byte.toUnsignedInt(piece);
			}
			return length;
		} catch(BufferUnderflowException e) {
			throw new ParseError("length", "Not enough bytes to extract length", e);
		}
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
