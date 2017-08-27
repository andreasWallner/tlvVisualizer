package at.innovative_solutions.tlv;

public interface ValueDecoder {
	/**
	 * get short name for the tag
	 * 
	 * The name should not be dependent on the current value of the TLV.
	 * It should provide a short description (e.g. 1-2 words) of the
	 * passed field.
	 * 
	 * If the field can't be recognized or parsed, null shall be returned
	 *  
	 * @param tlv TLV to decode
	 * @return name or null
	 */
	public String getName(final TLV tlv);
	
	/**
	 * Get value decoded to string
	 * 
	 * Should return a human-readable representation of the data in
	 * the TLV. Representation shall match with {@link #toValue(String, TLV)}.
	 * 
	 * If the field can't be recognized or parsed, null shall be returned
	 * 
	 * @param tlv TLV to decode
	 * @return decoded string or null
	 */
	public String toString(final TLV tlv); // TODO better name
	
	/**
	 * Encode string representation to bytes
	 * 
	 * Should convert the human readable representation to data bytes
	 * for update. Representation shall match with {@link #toString(TLV)}.
	 * 
	 * @param str human readable representation
	 * @param tlv TLV to encode for
	 * @return encoded bytes or null
	 */
	public byte[] toValue(final String str, final TLV tlv);

	// TODO does this belong here?
	/**
	 * Get format information for serializations specifying these
	 * 
	 * Should return a short formatting description if available, like used in EMV.
	 * Think along the lines of e.g. "I5" for a 5 digit integer, or similar.
	 * 
	 * Return null if no such thing is available/known for the given TLV.
	 * 
	 * @param tlv TLV to supply format for
	 * @return format hint or null
	 */
	public String getFormat(final TLV tlv);
	
	/**
	 * Check if TLV data matches expected data
	 * 
	 * @param tlv TLV to check
	 * @return check result
	 */
	public boolean isValueParsable(final TLV tlv);
	
	/**
	 * Decode TLV data and return interpretation
	 * 
	 * Return e.g. parsed and decoded bitfield data.
	 * 
	 * @param tlv TLV to parse data of
	 * @return multi-line string showing data interpretation or null
	 */
	public String getSimpleDecoded(final TLV tlv);
	
	public boolean isValid(final TLV tlv);
}
