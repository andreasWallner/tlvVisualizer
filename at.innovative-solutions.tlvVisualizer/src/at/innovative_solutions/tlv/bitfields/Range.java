package at.innovative_solutions.tlv.bitfields;

public class Range {
	final public int fStart;
	final public int fStop;
	final public int fLength;
	
	public Range(int start, int stop) {
		fStart = start;
		fStop = stop;
		fLength = fStart - fStop + 1;
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
