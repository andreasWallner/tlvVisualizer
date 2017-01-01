package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

public interface IBitfieldEncoding {
	public boolean isValid(long value);
	public Range getRange();
	public String getDescription(long value);
	
	public void accept(IBitfieldProcessor processor, byte[] data, Object context);
}
