package at.innovative_solutions.tlv;

import java.util.List;

public class SimpleFormatter implements Formatter<String> {
	final String _indentChars;
	int _indentLevel;
	
	public SimpleFormatter(final String indentChars) {
		_indentChars = indentChars;
		_indentLevel = 0;
	}
	
	public String format(final List<TLV> tlvs) {
		final StringBuilder sb = new StringBuilder();
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
		sb.append(Utils.repeat(_indentChars, _indentLevel));
		sb.append(Utils.bytesToHexString(tlv.getID().toBytes()));
		sb.append(" > ");
		sb.append(Utils.bytesToHexString(tlv.getData()));
		sb.append("\n");
		return sb.toString();
	}
	
	@Override
	public String format(final ConstructedTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.repeat(_indentChars, _indentLevel));
		sb.append(Utils.bytesToHexString(tlv.getID().toBytes()));
		sb.append("\n");
		_indentLevel += 1;
		
		for(TLV child : tlv.getTLVs())
			sb.append(child.accept(this));
		_indentLevel -= 1;
		return sb.toString();
	}
	
	@Override
	public String format(final ErrorTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.repeat(_indentChars, _indentLevel));
		sb.append("ERR ! ");
		sb.append(tlv.getError());

		if(tlv.getStage().order() > ErrorTLV.ParseStage.ParsingID.order()) {
			sb.append("\n");
			sb.append(Utils.repeat(_indentChars, _indentLevel));
			sb.append(Utils.repeat(_indentChars, 3));
			sb.append(" ! ID: ").append(Utils.bytesToHexString(tlv.getID().toBytes()));
		}
		if(tlv.getStage().order() > ErrorTLV.ParseStage.ParsingLength.order())
			sb.append(" LEN: ").append(tlv.getLength());
		if(tlv.getStage().order() >= ErrorTLV.ParseStage.FindingEnd.order()) {
			sb.append("\n");
			sb.append(Utils.repeat(_indentChars, _indentLevel));
			sb.append(Utils.repeat(_indentChars, 3));
			sb.append(" ! DATA: ").append(Utils.bytesToHexString(((ErrorTLV) tlv).getRemainingData()));
			sb.append(" (" + tlv.getRemainingData().length + " bytes)");
		}
		sb.append("\n");
		return sb.toString();
	}
}
