package at.innovative_solutions.tlv.test;

import static org.junit.Assert.*;

import org.junit.Test;

import at.innovative_solutions.tlv.Utils;

public class UtilsTest {
	@Test
	public void test_bytesToHex_standard() {
		final String conv = Utils.bytesToHexString(new byte[] { 0x00, (byte) 0x80, (byte) 0xff });
		assertEquals("converted", "0080FF", conv);
	}
	
	@Test
	public void test_hexStringToBytes_simple() {
		final byte[] conv = Utils.hexStringToBytes("00A0BF");
		assertArrayEquals("converted", new byte[] {0x00, (byte) 0xA0, (byte) 0xBF}, conv);
	}
	
	@Test
	public void test_hexStringToBytes_spaces() {
		final byte[] conv = Utils.hexStringToBytes("11 22   33");
		assertArrayEquals("converted", new byte[] {0x11, 0x22, 0x33}, conv);
	}
	
	@Test
	public void test_printChars_normal() {
		final String c = Utils.printChars("asdf", 3);
		assertEquals("characters", c, "asdfasdfasdf");
	}
	
	@Test
	public void test_printChars_zero() {
		final String c = Utils.printChars("bd", 0);
		assertEquals("characters", c, "");
	}
}
