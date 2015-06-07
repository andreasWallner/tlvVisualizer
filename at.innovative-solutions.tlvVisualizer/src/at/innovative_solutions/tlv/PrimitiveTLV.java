package at.innovative_solutions.tlv;

import java.util.Arrays;

public class PrimitiveTLV extends TLV {
	private byte[] _data;
	
	public PrimitiveTLV(final ID id, final byte[] data) {
		this(id, data, false);
	}
	
	public PrimitiveTLV(final ID id, final byte[] data, boolean indefiniteLength) {
		super(id, indefiniteLength);
		_data = data;
	}
	
	public boolean equals(final Object other) {
		if(!(other instanceof PrimitiveTLV))
			return false;
		
		return super.equals(other)
				&& Arrays.equals(_data, ((PrimitiveTLV) other)._data);
	}
	
	public byte[] getData() {
		return _data;
	}

	@Override
	public int getLength() {
		return _data.length;
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("PrimitiveTLV(");
		sb.append(_id).append(", ");
		sb.append(Arrays.toString(_data)).append(")");
		return sb.toString();
	}
	
	@Override
	public String prettyPrint(String indentChars) {
		return prettyPrint(indentChars, 0);
	}
	
	@Override
	public String prettyPrint(final String indentChars, int indentLevel) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.printChars(indentChars, indentLevel));
		sb.append(Utils.bytesToHexString(_id.toBytes()));
		sb.append(" > ");
		sb.append(Utils.bytesToHexString(_data));
		return sb.toString();
	}
	
	public <T> T accept(final Formatter<T> formatter) {
		return formatter.format(this);
	}
}
