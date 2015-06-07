package at.innovative_solutions.tlv;

public class SimpleFormatter implements Formatter<String> {
	final String _indentChars;
	int _indentLevel;
	
	public SimpleFormatter(final String indentChars) {
		_indentChars = indentChars;
		_indentLevel = 0;
	}
	
	@Override
	public String format(final TLV tlv) {
		return tlv.accept(this);
	}
	
	@Override
	public String format(final PrimitiveTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.printChars(_indentChars, _indentLevel));
		sb.append(Utils.bytesToHexString(tlv.getID().toBytes()));
		sb.append(" > ");
		sb.append(Utils.bytesToHexString(tlv.getData()));
		sb.append("\n");
		return sb.toString();
	}
	
	@Override
	public String format(final ConstructedTLV tlv) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utils.printChars(_indentChars, _indentLevel));
		sb.append(Utils.bytesToHexString(tlv.getID().toBytes()));
		sb.append("\n");
		_indentLevel += 1;
		
		for(TLV child : tlv.getTLVs())
			sb.append(child.accept(this));
		_indentLevel -= 1;
		return sb.toString();
	}
}
