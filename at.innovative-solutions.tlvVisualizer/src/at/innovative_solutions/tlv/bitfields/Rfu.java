package at.innovative_solutions.tlv.bitfields;

public class Rfu implements IBitfieldEncoding {
	final long fMask;
	
	public Rfu(long mask) {
		fMask = mask;
	}
	
	@Override
	public boolean isValid(long value) {
		return (value & fMask) == 0;
	}

	@Override
	public Range getRange() {
		return new Range(fMask);
	}

	@Override
	public String getDescription(long value) {
		if ((value & fMask) != 0)
			return "RFU - must be 0";
		else
			return "RFU";
	}

	@Override
	public String toString() {
		return "Rfu(" + Long.toHexString(fMask) + ", " + getRange().toString() + ")";
	}
	
	@Override
	public void accept(IBitfieldProcessor processor, byte[] data, Object context) {
		processor.visit(this, data, context);
	}
}
