package at.innovative_solutions.tlv.test;

import static org.junit.Assert.*;

import org.junit.Test;

import at.innovative_solutions.tlv.EMVValueDecoder;
import at.innovative_solutions.tlv.Utils;

public class EMVValueDecoderTest {
	@Test
	public void test_asString_simple() throws Exception {
		assertEquals("a", "foo", EMVValueDecoder.asString(Utils.hexStringToBytes("666F6F"), "a"));
		assertEquals("a 5", "fop", EMVValueDecoder.asString(Utils.hexStringToBytes("666F70"), "a 5"));
		assertEquals("an", "bar", EMVValueDecoder.asString(Utils.hexStringToBytes("626172"), "an"));
		assertEquals("an 5", "baz", EMVValueDecoder.asString(Utils.hexStringToBytes("62617A"), "an 5"));
		assertEquals("ans", "egg", EMVValueDecoder.asString(Utils.hexStringToBytes("656767"), "ans"));
		assertEquals("b 2", "19", EMVValueDecoder.asString(Utils.hexStringToBytes("0013"), "b"));
		assertEquals("cn", "1234567890123", EMVValueDecoder.asString(Utils.hexStringToBytes("1234567890123FFF"), "cn"));
		assertEquals("cn empty", "", EMVValueDecoder.asString(Utils.hexStringToBytes(""), "cn"));
		assertEquals("n", "12345", EMVValueDecoder.asString(Utils.hexStringToBytes("000000012345"), "n"));
		assertEquals("n", "10000", EMVValueDecoder.asString(Utils.hexStringToBytes("000000010000"), "n"));
		assertEquals("invalid", "", EMVValueDecoder.asString(Utils.hexStringToBytes("001100"), "foo"));
	}
}
