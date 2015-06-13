package at.innovative_solutions.tlv.test;

import java.nio.ByteBuffer;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import at.innovative_solutions.tlv.SimpleFormatter;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

public class SimpleFormatterTest {
	@Test
	public void test_formatting_simple() {
		final byte[] data = Utils.hexStringToBytes("6F1D 840E315041592E5359532E4444463031 A50B 880102 5F2D02656E 010801");
		final ByteBuffer input = ByteBuffer.wrap(data);
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		SimpleFormatter formatter = new SimpleFormatter(" ");
		String expected = "6F\n"
				        + " 84 > 315041592E5359532E4444463031\n"
				        + " A5\n"
				        + "  88 > 02\n"
				        + "  5F2D > 656E\n"
				        + "  ERR ! Frame too short for expected data length (8 bytes)\n"
				        + "      ! ID: 01 LEN: 8\n"
				        + "      ! DATA: 01 (1 bytes)\n";
		final String got = formatter.format(parsed);
		System.out.println(got);
		assertThat(got, is(expected));
	}
}
