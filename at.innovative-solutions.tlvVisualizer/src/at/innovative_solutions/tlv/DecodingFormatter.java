package at.innovative_solutions.tlv;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import at.innovative_solutions.tlvvisualizer.views.TagInfo;

public class DecodingFormatter implements Formatter<String> {
	final String _indentChars;
	int _indentLevel;
	final HashMap<Long, TagInfo> _tagInfo;
	
	public DecodingFormatter(final String indentChars, HashMap<Long, TagInfo> tagInfo) {
		_indentChars = indentChars;
		_indentLevel = 0;
		_tagInfo = tagInfo;
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
		if(_tagInfo.containsKey(id)) {
			sb.append(_tagInfo.get(id)._name);
			sb.append("\n");
			sb.append(Utils.printChars(_indentChars, _indentLevel));
			sb.append(Utils.printChars(" ", idString.length() + 3));
			
			String decoded = "";
			try {
				decoded = EMVValueDecoder.asString(tlv.getData(), _tagInfo.get(id)._format);
			} catch(UnsupportedEncodingException e) {}
			
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
		
		if(_tagInfo.containsKey(id))
			sb.append(" : ").append(_tagInfo.get(id)._name);
		
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
