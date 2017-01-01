package at.innovative_solutions.tlv;

import java.util.List;

public class DecodingFormatter implements Formatter<String> {
	final String fIndentChars;
	int fIndentLevel;
	final ValueDecoder fDecoder;
	
	public DecodingFormatter(final String indentChars, ValueDecoder decoder) {
		fIndentChars = indentChars;
		fIndentLevel = 0;
		fDecoder = decoder;
	}
	
	public String format(final List<TLV> tlvs) {
		StringBuilder sb = new StringBuilder();
		for(TLV t : tlvs)
			sb.append(format(t));
		return sb.toString();
	}
	
	@Override
	public String format(final TLV tlv) {
		return tlv.accept(this);
	}
	
	@Override
	public String format(final PrimitiveTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		
		String idString = Utils.bytesToHexString(tlv.getID().toBytes());
		String encoded = Utils.bytesToHexString(tlv.getData());
		
		sb.append(Utils.repeat(fIndentChars, fIndentLevel));
		sb.append(idString);
		sb.append(" > ");
		String name = fDecoder.getName(tlv);
		if(name != null) {
			sb.append(name);
			sb.append("\n");
			sb.append(Utils.repeat(fIndentChars, fIndentLevel));
			sb.append(Utils.repeat(" ", idString.length() + 3));
			
			String decoded = fDecoder.toString(tlv);
			
			if(!decoded.equals(""))
				sb.append(decoded).append(" (").append(encoded).append(")");
			else
				sb.append(encoded);
		} else {
			sb.append(encoded);
		}
		
		sb.append("\n");
		return sb.toString();
	}
	
	@Override
	public String format(final ConstructedTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		
		sb.append(Utils.repeat(fIndentChars, fIndentLevel));
		sb.append(Utils.bytesToHexString(tlv.getID().toBytes()));
		
		sb.append(" : ").append(fDecoder.getName(tlv));
		
		sb.append("\n");
		fIndentLevel += 1;
		
		for(TLV child : tlv.getTLVs())
			sb.append(child.accept(this));
		fIndentLevel -= 1;
		return sb.toString();
	}
	
	// TODO implement
	@Override
	public String format(final ErrorTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.repeat(fIndentChars, fIndentLevel));
		sb.append("ERROR");
		return sb.toString();
	}
}
