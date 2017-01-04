package at.innovative_solutions.tlv.bitfields;

import java.util.Arrays;
import java.util.Collection;

import at.innovative_solutions.tlv.Utils;

public class SimpleBitfieldFormatter implements IBitfieldProcessor {
	final Collection<IBitfieldEncoding> fEncoding;
	private String fResult;
	
	class Context {
		public Context(StringBuilder builder, String bitString) {
			this(builder, bitString, "", "", "");
		}
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
	
	public SimpleBitfieldFormatter(Collection<IBitfieldEncoding> encoding) {
		fEncoding = encoding;
	}
	
	// TODO check where length check is done
	@Override
	public void process(byte[] data) {
		StringBuilder builder = new StringBuilder();
		String bitString = Utils.bytesToBinString(data);
		
		Context context = new Context(builder, bitString);
		
		builder.append(bitString).append("\n");
		for(IBitfieldEncoding e : fEncoding) {
			e.accept(this, data, context);
		}
		
		fResult = builder.toString();
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
		String desc = e.getDescription(value);
		
		builder.append(context.fPrefixString);
		builder.append(Utils.repeat(".", bitLen - 1 - range.fStart));
		builder.append(context.fBitString.substring(bitLen - 1 - range.fStart, bitLen - range.fStop));
		builder.append(Utils.repeat(".", range.fStop));
		builder.append(context.fPostfixString);
		builder.append(" ");
		builder.append(context.fIndentString);
		builder.append(desc != null ? desc : "- invalid");
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
			String prefixString = Utils.repeat('.', offset * 8);
			String postfixString = Utils.repeat('.', (data.length - offset - encoding.fSize) * 8);
			
			builder.append(prefixString);
			builder.append(bitString);
			builder.append(postfixString);
			builder.append(" ");
			builder.append(context.fIndentString);
			builder.append(encoding.getDescription(0)).append(" ").append(idx);
			builder.append("\n");
			
			Context subContext = new Context(builder, bitString, "  ", prefixString, postfixString);
			for(IBitfieldEncoding e : encoding.fEncoding) {
				e.accept(this, dataPiece, subContext);
			}
		}
		
		if(offset != data.length) {
			byte[] rest = Arrays.copyOfRange(data, offset - 1, data.length - 1);
			String bitString = Utils.bytesToBinString(rest);
			builder.append(Utils.repeat('.', offset * 8));
			builder.append(bitString);
			builder.append(" ");
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
