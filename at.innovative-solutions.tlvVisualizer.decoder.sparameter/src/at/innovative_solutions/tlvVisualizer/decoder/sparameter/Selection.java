package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import java.util.Collection;

public class Selection implements IBitfieldEncoding {
	final long fMask;
	Collection<SelectionOption> fOptions;
	
	public Selection(long mask, Collection<SelectionOption> options) {
		fMask = mask;
		fOptions = options;
	}
	
	@Override
	public boolean isValid(long value) {
		for(SelectionOption option : fOptions) {
			if((value & fMask) == option.fValue)
				return true;
		}
		return false;
	}

	@Override
	public Range getRange() {
		int start = Long.SIZE - 1 - Long.numberOfLeadingZeros(fMask);
		int stop = (int) (Long.SIZE - Long.numberOfLeadingZeros(~(fMask | (-1L << start))));
		return new Range(start, stop);
	}

	@Override
	public String getDescription(long value) {
		for(SelectionOption option : fOptions) {
			if((value & fMask) == option.fValue)
				return option.fName;
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Selection(" + Long.toHexString(fMask));
		for(SelectionOption o : fOptions)
			str.append(",\n  " + o.toString());
		str.append(")");
		return str.toString();
	}

	@Override
	public void accept(IBitfieldProcessor processor, byte[] data, Object context) {
		processor.visit(this, data, context);
	}
}
