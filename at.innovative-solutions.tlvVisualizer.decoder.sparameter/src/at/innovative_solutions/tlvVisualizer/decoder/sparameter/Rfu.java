package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

public class Rfu extends Encoding {
	public Rfu(long mask) {
		super(mask);
	}
	
	@Override
	public boolean isValid(long value) {
		return (value & fMask) == 0;
	}

	@Override
	public Range getRange() {
		int start = Long.SIZE - 1 - Long.numberOfLeadingZeros(fMask);
		int stop = (int) (Long.SIZE - Long.numberOfLeadingZeros(~(fMask | (-1L << start))));
		return new Range(start, stop);
	}

	@Override
	public String getDescription(long value) {
		if ((value & fMask) != 0)
			return "RFU - must be 0";
		else
			return "RFU";
	}
}
