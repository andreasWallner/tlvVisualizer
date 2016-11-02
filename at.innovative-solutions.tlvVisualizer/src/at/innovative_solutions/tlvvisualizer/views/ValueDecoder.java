package at.innovative_solutions.tlvvisualizer.views;

import at.innovative_solutions.tlv.TLV;

public interface ValueDecoder {
	public String getName(final TLV tlv);
	public String toString(final TLV tlv); // TODO better name
	public byte[] toValue(final String str, final TLV tlv);
	public String getFormat(final TLV tlv);
	public boolean isValueParsable(final TLV tlv);
}
