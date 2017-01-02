package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import java.util.Arrays;

import at.innovative_solutions.tlv.Utils;

public class SimpleBitfieldFormatter implements IBitfieldProcessor {
	final ValueInfo fInfo;
	private String fResult;
	
	class Context {
		public Context(StringBuilder builder, String bitString, String indentString, String prefixString, String postfixString) {
			fBuilder = builder;
			fBitString = bitString;
			fIndentString = indentString;
			fPrefixString = prefixString;
			fPostfixString = postfixString;
		}
		public StringBuilder fBuilder;
		public String fBitString;
		public String fIndentString;
		public String fPrefixString;
		public String fPostfixString;
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
		Context context = new Context(builder, bitString, "", "", "");
		
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
		
		builder.append(context.fPrefixString);
		builder.append(Utils.repeat(".", bitLen - 1 - range.fStart));
		builder.append(context.fBitString.substring(bitLen - 1 - range.fStart, bitLen - range.fStop));
		builder.append(Utils.repeat(".", range.fStop));
		builder.append(context.fPostfixString);
		builder.append(" ");
		builder.append(context.fIndentString);
		builder.append(e.getDescription(value));
		builder.append("\n");
	}
	
	public void visit(Flag encoding, byte[] data, Object contextObject) {
		visitSimple(encoding, data, contextObject);
	}
	
	// TODO handle hierarchies
	// TODO handle repeats mixed with other stuff
	// TODO handle repeats with fixed occurrence count
	public void visit(Repeat encoding, byte[] data, Object contextObject) {
		Context context = (Context)contextObject;
		StringBuilder builder = context.fBuilder;
		
		int offset = 0;
		int idx = 0;
		for(; offset < data.length + 1 - encoding.fSize; offset += encoding.fSize, idx++) {
			byte[] dataPiece = Arrays.copyOfRange(data, offset, offset + encoding.fSize);
			String bitString = Utils.bytesToBinString(dataPiece);
			String prefixString = Utils.repeat(".", offset * 8);
			String postfixString = Utils.repeat(".", (data.length - offset - encoding.fSize) * 8);
			
			builder.append(prefixString);
			builder.append(bitString);
			builder.append(postfixString);
			builder.append(" ");
			builder.append(context.fIndentString);
			builder.append(encoding.getDescription(0) + " " + Integer.toString(idx));
			builder.append("\n");
			
			Context subContext = new Context(builder, bitString, "  ", prefixString, postfixString);
			for(IBitfieldEncoding e : encoding.fEncoding) {
				e.accept(this, dataPiece, subContext);
			}
		}
		
		if(offset + encoding.fSize - 1 != data.length) {
			builder.append("ERROR: Invalid length to fill another complete repetition\n");			
		}
	}
	
	public void visit(Rfu encoding, byte[] data, Object contextObject) {
		visitSimple(encoding, data, contextObject);
	}
	
	public void visit(Selection encoding, byte[] data, Object contextObject) {
		visitSimple(encoding, data, contextObject);
	}
}
