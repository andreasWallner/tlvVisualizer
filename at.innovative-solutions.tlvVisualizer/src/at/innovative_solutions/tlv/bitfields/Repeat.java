package at.innovative_solutions.tlv.bitfields;

import java.util.Arrays;
import java.util.Collection;

import at.innovative_solutions.tlv.Utils;

public class Repeat implements IBitfieldEncoding {
	final int fSize;
	final String fName;
	final Collection<IBitfieldEncoding> fEncoding;
	
	public Repeat(int size, String name, Collection<IBitfieldEncoding> encoding) {
		fSize = size;
		fName = name;
		fEncoding = encoding;
	}
	
	@Override
	public boolean isValid(long value) {
		
		return false;
	}
	
	public boolean isValid(byte[] value) {
		if(value.length % fSize != 0)
			return false;
		
		for(int sliceStart = 0; sliceStart < value.length; sliceStart += fSize) {
			byte[] slice = Arrays.copyOfRange(value, sliceStart, fSize);
			long longSlice = Utils.bytesToLong(slice);
			for(IBitfieldEncoding e : fEncoding) {
				if(!e.isValid(longSlice))
					return false;
			}
		}
		
		return true;
	}

	@Override
	public Range getRange() {
		return null;
	}
	
	public Range getRange(byte[] value) { // TODO incorrect < range is exclusive
		return new Range(0, value.length * 8);
	}

	@Override
	public String getDescription(long value) {
		return fName;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Repeat(").append(fSize).append(", ").append(fName);
		for(IBitfieldEncoding e : fEncoding)
			str.append(",\n  ").append(e.toString());
		str.append(")");
		return str.toString();
	}

	@Override
	public void accept(IBitfieldProcessor processor, byte[] data, Object context) {
		processor.visit(this, data, context);
	}
}
