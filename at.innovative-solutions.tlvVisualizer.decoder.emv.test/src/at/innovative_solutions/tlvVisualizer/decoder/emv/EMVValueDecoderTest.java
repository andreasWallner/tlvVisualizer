package at.innovative_solutions.tlvVisualizer.decoder.emv;

import static at.innovative_solutions.tlvVisualizer.decoder.emv.ThrownMatcher.thrown;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import at.innovative_solutions.tlv.Utils;
import at.innovative_solutions.tlvVisualizer.decoder.emv.EMVValueDecoder;
import at.innovative_solutions.tlvvisualizer.views.InvalidEncodedValueException;

// Data formats are given in EMV Book 3, Chapter "Data Element Format Conventions" (4.3, p. 29 in v4.2)
public class EMVValueDecoderTest {
	@Test
	public void test_asString_simple() throws Exception {
		assertEquals("a", "foo", EMVValueDecoder.asString(Utils.hexStringToBytes("666F6F"), "a"));
		assertEquals("a 5", "fop", EMVValueDecoder.asString(Utils.hexStringToBytes("666F70"), "a 5"));
		assertEquals("an", "bar", EMVValueDecoder.asString(Utils.hexStringToBytes("626172"), "an"));
		assertEquals("an 5", "baz", EMVValueDecoder.asString(Utils.hexStringToBytes("62617A"), "an 5"));
		assertEquals("ans", "egg", EMVValueDecoder.asString(Utils.hexStringToBytes("656767"), "ans"));

		assertThat(EMVValueDecoder.asString(Utils.hexStringToBytes("13"), "b"), equalTo("13"));
		assertThat(EMVValueDecoder.asString(Utils.hexStringToBytes("0013"), "b 2"), equalTo("0013"));

		assertEquals("cn", "1234567890123", EMVValueDecoder.asString(Utils.hexStringToBytes("1234567890123FFF"), "cn"));
		assertEquals("cn empty", "", EMVValueDecoder.asString(Utils.hexStringToBytes(""), "cn"));
		assertEquals("n", "12345", EMVValueDecoder.asString(Utils.hexStringToBytes("000000012345"), "n"));
		assertEquals("n", "10000", EMVValueDecoder.asString(Utils.hexStringToBytes("000000010000"), "n"));
		assertEquals("invalid", "", EMVValueDecoder.asString(Utils.hexStringToBytes("001100"), "foo"));
	}

	// TODO: consider lengths
	@Test
	public void test_toValue_simple() throws Exception {
		assertThat(EMVValueDecoder.asValue("abcd", "a"), equalTo(Utils.hexStringToBytes("61626364")));
		assertThat(EMVValueDecoder.asValue("", "a"), equalTo(Utils.hexStringToBytes("")));
		assertThat(() -> EMVValueDecoder.asValue("a1b2", "a"), thrown(InvalidEncodedValueException.class));
		assertThat(() -> EMVValueDecoder.asValue(" ", "a"), thrown(InvalidEncodedValueException.class));

		assertThat(EMVValueDecoder.asValue("ab12", "an"), equalTo(Utils.hexStringToBytes("61623132")));
		assertThat(EMVValueDecoder.asValue("", "an"), equalTo(Utils.hexStringToBytes("")));
		assertThat(() -> EMVValueDecoder.asValue("ab_12", "an"), thrown(InvalidEncodedValueException.class));
		assertThat(() -> EMVValueDecoder.asValue("ab 12", "an"), thrown(InvalidEncodedValueException.class));

		assertThat(EMVValueDecoder.asValue("a b~1^", "ans"), equalTo(Utils.hexStringToBytes("6120627e315e")));
		assertThat(() -> EMVValueDecoder.asValue("ä", "ans"), thrown(InvalidEncodedValueException.class));
		// chars that are not in 8859-1
		assertThat(() -> EMVValueDecoder.asValue("€Š", "ans"), thrown(InvalidEncodedValueException.class));

		assertThat(EMVValueDecoder.asValue("0013", "b"), equalTo(Utils.hexStringToBytes("0013")));
		assertThat(() -> EMVValueDecoder.asValue("no", "b"), thrown(InvalidEncodedValueException.class));
		assertThat(() -> EMVValueDecoder.asValue("013", "b"), thrown(InvalidEncodedValueException.class));

		assertThat(EMVValueDecoder.asValue("123456", "cn"), equalTo(Utils.hexStringToBytes("123456")));
		assertThat(EMVValueDecoder.asValue("12345", "cn"), equalTo(Utils.hexStringToBytes("12345F")));
		assertThat(() -> EMVValueDecoder.asValue("text", "cn"), thrown(InvalidEncodedValueException.class));

		assertThat(EMVValueDecoder.asValue("123456", "n"), equalTo(Utils.hexStringToBytes("123456")));
		assertThat(EMVValueDecoder.asValue("12345", "n"), equalTo(Utils.hexStringToBytes("012345")));
		assertThat(() -> EMVValueDecoder.asValue("text", "n"), thrown(InvalidEncodedValueException.class));
	}
}
