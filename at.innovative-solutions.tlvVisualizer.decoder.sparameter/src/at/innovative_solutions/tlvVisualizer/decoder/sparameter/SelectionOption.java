package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

class SelectionOption {
	final public long fValue;
	final public String fName;
	
	public SelectionOption(long value, String name) {
		fValue = value;
		fName = name;
	}
	
	@Override
	public String toString() {
		return "Option(" + Long.toHexString(fValue) + ", " + fName + ")";
	}
}
