package at.innovative_solutions.tlv;

public interface Formattable {
	public <T> T accept(final Formatter<T> formatter);
}
