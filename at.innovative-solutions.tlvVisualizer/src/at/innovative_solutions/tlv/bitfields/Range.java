package at.innovative_solutions.tlv.bitfields;

public class Range {
	final public int fStart;
	final public int fStop;
	
	public Range(int start, int stop) {
		fStart = start;
		fStop = stop;
	}
	
	public Range(long mask) {
		fStart = Long.SIZE - 1 - Long.numberOfLeadingZeros(mask);
		fStop = (int) (Long.SIZE - Long.numberOfLeadingZeros(~(mask | (-1L << fStart))));
	}
	
	public String toString() {
		return "Range(" + fStart + ", " + fStop + ")";
	}
	
	@Override
	public boolean equals(final Object r) {
		if(r == null || !(r instanceof Range))
			return false;
		
		Range other = (Range)r;
		
		return fStart == other.fStart && fStop == other.fStop;
	}
}
