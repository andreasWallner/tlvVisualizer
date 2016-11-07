package at.innovative_solutions.tlv;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import at.innovative_solutions.tlvvisualizer.views.TagInfo;

public class DecodingFormatter implements Formatter<String> {
	final String _indentChars;
	int _indentLevel;
	final ValueDecoder fDecoder;
	
	public DecodingFormatter(final String indentChars, ValueDecoder decoder) {
		_indentChars = indentChars;
		_indentLevel = 0;
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
		Long id = tlv.getID().toLong();
		String encoded = Utils.bytesToHexString(tlv.getData());
		
		sb.append(Utils.printChars(_indentChars, _indentLevel));
		sb.append(idString);
		sb.append(" > ");
		String name = fDecoder.getName(tlv);
		if(name != null) {
			sb.append(name);
			sb.append("\n");
			sb.append(Utils.printChars(_indentChars, _indentLevel));
			sb.append(Utils.printChars(" ", idString.length() + 3));
			
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
		
		Long id = tlv.getID().toLong();
		
		sb.append(Utils.printChars(_indentChars, _indentLevel));
		sb.append(Utils.bytesToHexString(tlv.getID().toBytes()));
		
		sb.append(" : ").append(fDecoder.getName(tlv));
		
		sb.append("\n");
		_indentLevel += 1;
		
		for(TLV child : tlv.getTLVs())
			sb.append(child.accept(this));
		_indentLevel -= 1;
		return sb.toString();
	}
	
	// TODO implement
	@Override
	public String format(final ErrorTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.printChars(_indentChars, _indentLevel));
		sb.append("ERROR");
		return sb.toString();
	}
}
