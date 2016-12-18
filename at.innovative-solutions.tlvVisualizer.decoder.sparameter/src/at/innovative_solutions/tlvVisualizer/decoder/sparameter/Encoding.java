package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

public abstract class Encoding {
	final public long fMask;
	
	public Encoding(long mask) {
		fMask = mask;
	}
	
	abstract public boolean isValid(long value);
	abstract public Range getRange();
	abstract public String getDescription(long value);
}
