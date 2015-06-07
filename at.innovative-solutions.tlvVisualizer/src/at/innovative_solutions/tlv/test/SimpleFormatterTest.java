package at.innovative_solutions.tlv.test;

import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import at.innovative_solutions.tlv.SimpleFormatter;
import at.innovative_solutions.tlv.TLV;
import junit.framework.TestCase;

public class SimpleFormatterTest extends TestCase {
	@Test
	public void test_formatting_simple() {
		final byte[] data = DatatypeConverter.parseHexBinary("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E");
		final ByteBuffer input = ByteBuffer.wrap(data);
		final TLV parsed = TLV.parseTLV(input);
		
		SimpleFormatter formatter = new SimpleFormatter(" ");
		String expected = "6F\n"
				        + " 84 > 315041592E5359532E4444463031\n"
				        + " A5\n"
				        + "  88 > 02\n"
				        + "  5F2D > 656E\n";
		final String got = formatter.format(parsed);
		assertEquals("output", got, expected);
	}
}
