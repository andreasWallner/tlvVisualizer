package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import java.util.Collection;

public class Selection extends Encoding {
	Collection<SelectionOption> fOptions;
	
	public Selection(long mask, Collection<SelectionOption> options) {
		super(mask);
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
}
