package at.innovative_solutions.tlv;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class PrimitiveTLV extends TLV {
	private byte[] _data;
	
	public PrimitiveTLV(final ID id) {
		this(id, new byte[] {}, false);
	}
	
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
	
	// TODO provide more efficient implementation
	@Override
	public int getSerializedLength() {
		ByteArrayOutputStream serializedLength = new ByteArrayOutputStream();
		serializeLength(serializedLength, _data.length);
		return _data.length + _id.toBytes().length + serializedLength.size();
	}

	public void setData(byte[] data) {
		_data = data;
		fireChangeEvent(new ChangeEvent(this));
	}
	
	public byte[] toBytes() {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] serializedID = _id.toBytes();
		result.write(serializedID, 0, serializedID.length);
		TLV.serializeLength(result, _data.length);
		result.write(_data, 0, _data.length);
		
		return result.toByteArray();
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("PrimitiveTLV(");
		sb.append(_id).append(", ");
		sb.append("[");
		for(int i = 0; i < _data.length; i++)
			sb.append("0x").append(Integer.toHexString(_data[i] & 0xff)).append(",");
		sb.append("], ");
		sb.append(_lengthIndefinite).append(")");
		return sb.toString();
	}
	
	public <T> T accept(final Formatter<T> formatter) {
		return formatter.format(this);
	}
}
