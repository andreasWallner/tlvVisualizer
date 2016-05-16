package at.innovative_solutions.tlv;

public class InvalidEncodedValueException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	InvalidEncodedValueException(String str) {
		super(str);
	}
}
