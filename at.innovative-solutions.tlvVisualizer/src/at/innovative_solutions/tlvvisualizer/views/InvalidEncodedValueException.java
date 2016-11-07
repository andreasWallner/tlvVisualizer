package at.innovative_solutions.tlvvisualizer.views;

public class InvalidEncodedValueException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidEncodedValueException(String str) {
		super(str);
	}
}
