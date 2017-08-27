package at.innovative_solutions.tlvVisualizer.decoder.asn1;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;

import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

public class ASN1ValueDecoderTest {
	private TLV p(String s) {
		byte[] b = Utils.hexStringToBytes(s);
		ByteBuffer bb = ByteBuffer.wrap(b);
		return TLV.parseTLV(bb);
	}
	
	@Test
	public void toString_boolean() {
		ASN1ValueDecoder d = new ASN1ValueDecoder();
		
		assertThat(d.toString(p("010100")), is("false"));        // 8.2.2
		assertThat(d.toString(p("0101FF")), is("true"));         // 8.2.2
		assertThat(d.toString(p("010101")), is("true"));         // 8.2.2
		
		assertThat(d.toString(p("0100")), is(nullValue()));      // 8.2.1
		assertThat(d.toString(p("01020000")), is(nullValue()));  // 8.2.1
	}
	
	@Test
	public void toString_integer() {
		ASN1ValueDecoder d = new ASN1ValueDecoder();
		
		assertThat(d.toString(p("020105")), is("5"));                    // 8.3.3
		assertThat(d.toString(p("0201FF")), is("-1"));                   // 8.3.3
		assertThat(d.toString(p("0205 123456789A")), is("78187493530")); // 8.3.3
		
		assertThat(d.toString(p("0200")), is(nullValue()));              // 8.3.1
		assertThat(d.toString(p("02020001")), is(nullValue()));          // 8.3.2
		assertThat(d.toString(p("0202FFFF")), is(nullValue()));          // 8.3.2
	}
	
	@Test
	public void toString_bytestring() {
		ASN1ValueDecoder d = new ASN1ValueDecoder();

		// primitive root
		assertThat(d.toString(p("030100")), is(""));                         // 8.6.2.3
		assertThat(d.toString(p("0303 00 A005")), is("10100000 00000101"));  // 8.6.2.1
		assertThat(d.toString(p("0307 04 0A3B5F291CD0")), is("00001010 00111011 01011111 00101001 00011100 1101")); // 8.6.2.2
		
		assertThat(d.toString(p("0300")), is(nullValue()));                 // 8.6.2.3
		assertThat(d.toString(p("030101")), is(nullValue()));               // 8.6.2.2
		assertThat(d.toString(p("03020801")), is(nullValue()));             // 8.6.2.2

		// constructed root
		assertThat(d.toString(p("2300")), is(""));                                    // 8.6.3
		assertThat(d.toString(p("2305 0303 00 A005")), is("10100000 00000101"));      // 8.6.3
		assertThat(d.toString(p("2308 030200A0 03020005")), is("10100000 00000101")); // 8.6.3
		assertThat(d.toString(p("2380 0303000A3B 0305045F291CD0 0000")), is("00001010 00111011 01011111 00101001 00011100 1101")); // 8.6.4.2
		assertThat(d.toString(p("2307 2305 030300A005")), is("10100000 00000101"));   // 8.6.4.1 NOTE 1
		assertThat(d.toString(p("2308 030200FF 030207FF")), is("11111111 1"));        // 8.6.4
		assertThat(d.toString(p("2307 030100 030200FF")), is("11111111"));            // 8.6.4 NOTE
		
		assertThat(d.toString(p("2305 4303 00 A005")), is(nullValue()));              // 8.6.4.1 NOTE 2
		assertThat(d.toString(p("2305 0403 00 A005")), is(nullValue()));              // 8.6.4.1 NOTE 2
		assertThat(d.toString(p("2308 030201FF 030200FF")), is(nullValue()));         // 8.6.4
	}
	
	@Test
	public void toString_octetstring() {
		ASN1ValueDecoder d = new ASN1ValueDecoder();
		
		assertThat(d.toString(p("0400")), is(""));                                 // 8.7.2
		assertThat(d.toString(p("0402 00ff")), is("00FF"));                        // 8.7.2
		
		assertThat(d.toString(p("2480 04021234 0000")), is("1234"));               // 8.7.3
		assertThat(d.toString(p("2480 0402AA55 040211FF 0000")), is("AA5511FF"));  // 8.7.3
		assertThat(d.toString(p("2406 2404 04029876")), is("9876"));               // 8.7.3.2
		
		assertThat(d.toString(p("2403 030100")), is(nullValue()));                 // 8.7.3.2 NOTE 4
		assertThat(d.toString(p("2403 440100")), is(nullValue()));                 // 8.7.3.2 NOTE 4
	}
	
	@Test
	public void toString_null() {
		ASN1ValueDecoder d = new ASN1ValueDecoder();
		
		assertThat(d.toString(p("0500")), is("null"));           // 8.8 Example
		assertThat(d.toString(p("050100")), is(nullValue()));    // 8.8.2
		assertThat(d.toString(p("2500")), is(nullValue()));      // 8.8.1
	}
	
	@Test
	public void toString_sequence() {
		ASN1ValueDecoder d = new ASN1ValueDecoder();
		
		assertThat(d.toString(p("300A 1605536D697468 0101FF")), is("sequence")); // 8.9 Example
		assertThat(d.toString(p("3000")), is("sequence"));                       // 8.9.2
		assertThat(d.toString(p("1000")), is(nullValue()));                      // 8.9.1
	}
	
	@Test
	public void toString_set() {
		ASN1ValueDecoder d = new ASN1ValueDecoder();

		assertThat(d.toString(p("310A 1605536D697468 0101FF")), is("set")); // 8.11.2
		assertThat(d.toString(p("3100")), is("set"));                       // 8.11.3
		assertThat(d.toString(p("1100")), is(nullValue()));                 // 8.11.1
	}
}
