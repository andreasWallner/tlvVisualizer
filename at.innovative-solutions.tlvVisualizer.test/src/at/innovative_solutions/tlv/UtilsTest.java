package at.innovative_solutions.tlv;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;

import org.junit.Test;

import at.innovative_solutions.tlv.Utils;

public class UtilsTest {
	@Test
	public void test_bytesToHex_standard() {
		final String conv = Utils.bytesToHexString(new byte[] { 0x00, (byte) 0x80, (byte) 0xff });
		assertEquals("converted", "0080FF", conv);
	}

	@Test
	public void test_hexStringToBytes() {
		assertThat(Utils.hexStringToBytes("00A0BF"), equalTo(new byte[] {0x00, (byte) 0xA0, (byte) 0xBF}));
		assertThat(Utils.hexStringToBytes("11 22   33"), equalTo(new byte[] {0x11, 0x22, 0x33}));
		assertThat(Utils.hexStringToBytes("111"), equalTo(new byte[] {0x01, 0x11}));
		assertThat(Utils.hexStringToBytes("1"), equalTo(new byte[] {0x1}));
	}

	@Test
	public void test_bytesToBinString() {
		assertThat(Utils.bytesToBinString(new byte[] { 0x57, 0x75 }), equalTo("0101011101110101"));
	}

	@Test
	public void test_repeat_string() {
		assertThat(Utils.repeat("asdf", 3), equalTo("asdfasdfasdf"));
		assertThat(Utils.repeat("bd", 0), equalTo(""));
		assertThat(Utils.repeat("♥", 2), equalTo("♥♥"));
	}
	
	@Test
	public void test_repeat_char() {
		assertThat(Utils.repeat('c', 5), equalTo("ccccc"));
		assertThat(Utils.repeat('c', 0), equalTo(""));
		assertThat(Utils.repeat('♥', 2), equalTo("♥♥"));
	}
}
