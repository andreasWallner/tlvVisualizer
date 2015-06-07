package at.innovative_solutions.tlv;

public interface Formatter<T> {
	public T format(final TLV tlv);
	public T format(final PrimitiveTLV tlv);	
	public T format(final ConstructedTLV tlv);
}
