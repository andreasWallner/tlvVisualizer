package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

public class Flag implements IBitfieldEncoding {
	final long fMask;
	final String fName;
	final String fEnabled;
	final String fDisabled;
	
	public Flag(long mask, String name, String enabled, String disabled, boolean concat) {
		fMask = mask;
		fName = name;
		if(concat) {
			fEnabled = name + " " + enabled;
			fDisabled = name + " " + disabled;
		} else {
			fEnabled = enabled;
			fDisabled = disabled;
		}
		
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
	
	@Override
	public String toString() {
		return "Flag(" + Long.toHexString(fMask) + ", " + fName + ", " + fEnabled + ", " + fDisabled + ", " + getRange().toString() + ")";
	}

	@Override
	public void accept(IBitfieldProcessor processor, byte[] data, Object context) {
		processor.visit(this, data, context);
	}
}
