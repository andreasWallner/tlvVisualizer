package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

public class Flag extends Encoding {
	final String fName;
	final String fEnabled;
	final String fDisabled;
	
	public Flag(long mask, String name, String enabled, String disabled) {
		super(mask);
		fName = name;
		fEnabled = name + " " + enabled;
		fDisabled = name + " " + disabled;
		
		if(Long.bitCount(mask) != 1)
			throw new RuntimeException("exactly on bit must be set for flag, but is not for " + name);
	}

	@Override
	public boolean isValid(long value) {
		// flag is always valid, no invalid states possible
		return true;
	}
	
	public boolean isSet(long value) {
		return (value & fMask) != 0;
	}

	@Override
	public Range getRange() {
		int bit = Long.SIZE - 1 - Long.numberOfLeadingZeros(fMask);
		return new Range(bit, bit);
	}

	@Override
	public String getDescription(long value) {
		if((value & fMask) != 0)
			return fEnabled;
		else
			return fDisabled;
	}
}
