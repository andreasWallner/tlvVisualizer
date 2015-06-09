package at.innovative_solutions.tlv;

import java.util.List;

public interface Formatter<T> {
	public T format(final List<TLV> tlvs);
	public T format(final TLV tlv);
	public T format(final PrimitiveTLV tlv);	
	public T format(final ConstructedTLV tlv);
}
