package at.innovative_solutions.tlv.bitfields;

public class Flag implements IBitfieldEncoding {
	final int fBit;
	final String fName;
	final String fEnabled;
	final String fDisabled;
	
	public Flag(int bit, String name, String enabled, String disabled, boolean concat) {
		fBit = bit;
		fName = name;
		if(concat) {
			fEnabled = name + " " + enabled;
			fDisabled = name + " " + disabled;
		} else {
			fEnabled = enabled;
			fDisabled = disabled;
		}
	}

	@Override
	public boolean isValid(long value) {
		// flag is always valid, no invalid states possible
		return true;
	}
	
	public boolean isSet(long value) {
		return (value & (1 << fBit)) != 0;
	}
	
	public boolean isSet(byte[] value) {
		int idx = fBit / 8;
		int bit = fBit % 8;
		return (value[idx] & (1 << bit)) != 0;
	}

	@Override
	public Range getRange() {
		return new Range(fBit, fBit);
	}

	@Override
	public String getDescription(long value) {
		return isSet(value) ? fEnabled : fDisabled;
	}
	
	public String getDescription(byte[] value) {
		return isSet(value) ? fEnabled : fDisabled;
	}
	
	@Override
	public String toString() {
		return "Flag(" + getRange().toString() + ", " + fName + ", " + fEnabled + ", " + fDisabled + ")";
	}

	@Override
	public void accept(IBitfieldProcessor processor, byte[] data, Object context) {
		processor.visit(this, data, context);
	}
}
