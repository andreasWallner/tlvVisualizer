package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import at.innovative_solutions.tlv.Utils;

public class SimpleBitfieldFormatter implements IBitfieldProcessor {
	final ValueInfo fInfo;
	private String fResult;
	
	class Context {
		public Context(StringBuilder builder, String bitString) {
			fBuilder = builder;
			fBitString = bitString;
		}
		public StringBuilder fBuilder;
		public String fBitString;
	}
	
	SimpleBitfieldFormatter(ValueInfo info) {
		fInfo = info;
	}
	
	// TODO check where length check is done
	@Override
	public void process(byte[] data) {
		System.out.println(BitfieldEncodingFactory.toString(fInfo.fEncodings));
		Long value = Utils.bytesToLong(data);
		StringBuilder builder = new StringBuilder();
		
		final int bitLength = 8 * fInfo.fLength;
		String valueString = Long.toBinaryString(value);
		String valuePadding = Utils.repeat("0", bitLength - valueString.length());
		builder.append(valuePadding + valueString + "\n");
		
		String bitString = Utils.bytesToBinString(data);
		Context context = new Context(builder, bitString);
		
		for(IBitfieldEncoding e : fInfo.fEncodings) {
			e.accept(this, data, context);
		}
		
		fResult = builder.toString();
		System.out.println(fResult);
	}
	
	public String getResult() {
		return fResult;
	}
	
	void visitSimple(IBitfieldEncoding e, byte[] data, Object contextObject) {
		Context context = (Context)contextObject;
		StringBuilder builder = context.fBuilder;
		int bitLen = context.fBitString.length();
		
		Long value = Utils.bytesToLong(data);
		Range range = e.getRange();
		
		builder.append(Utils.repeat(".", bitLen - 1 - range.fStart));
		builder.append(context.fBitString.substring(bitLen - 1 - range.fStart, bitLen - range.fStop));
		builder.append(Utils.repeat(".", range.fStop));
		builder.append(" ");
		builder.append(e.getDescription(value));
		builder.append("\n");
	}
	
	public void visit(Flag encoding, byte[] data, Object context) {
		visitSimple(encoding, data, context);
	}
	
	public void visit(Repeat encoding, byte[] data, Object context) {
		visitSimple(encoding, data, context);
	}
	
	public void visit(Rfu encoding, byte[] data, Object context) {
		visitSimple(encoding, data, context);
	}
	
	public void visit(Selection encoding, byte[] data, Object context) {
		visitSimple(encoding, data, context);
	}
}
