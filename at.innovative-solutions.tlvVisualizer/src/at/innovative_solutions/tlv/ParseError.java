package at.innovative_solutions.tlv;

public class ParseError extends RuntimeException {
	private static final long serialVersionUID = 1L;
	final String _parseStep;
	
	public ParseError(final String parseStep, final String message) {
		this(parseStep, message, null);
	}
	
	public ParseError(final String parseStep, final String message, final Throwable cause) {
		super(message, cause);
		_parseStep = parseStep;
	}
}
