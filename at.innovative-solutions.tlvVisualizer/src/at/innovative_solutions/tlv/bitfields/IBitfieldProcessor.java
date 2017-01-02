package at.innovative_solutions.tlv.bitfields;

public interface IBitfieldProcessor {	
	public void process(byte[] data);
	
	public void visit(Flag encoding, byte[] data, Object context);
	public void visit(Repeat encoding, byte[] data, Object context);
	public void visit(Rfu encoding, byte[] data, Object context);
	public void visit(Selection encoding, byte[] data, Object context);
}
