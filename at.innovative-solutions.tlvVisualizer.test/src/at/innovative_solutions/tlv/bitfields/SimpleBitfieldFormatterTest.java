package at.innovative_solutions.tlv.bitfields;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class SimpleBitfieldFormatterTest {
	@Test
	public void SimpleBitfieldFormatter_mixed() {
		Collection<IBitfieldEncoding> encoding = new ArrayList<IBitfieldEncoding>();
		encoding.add(new Flag(0x8000, "flag 1", "en", "dis", true));
		encoding.add(new Flag(0x4000, "flag 2", "f2e", "f2d", false));
		encoding.add(new Flag(0x2000, "flag 3", "en", "dis", true));
		encoding.add(new Flag(0x1000, "flag 4", "f4e", "f4d", false));
		Collection<SelectionOption> options = new ArrayList<SelectionOption>();
		options.add(new SelectionOption(0x0, "not this"));
		options.add(new SelectionOption(0x100, "this"));
		encoding.add(new Selection(0x0F00, options));
		encoding.add(new Rfu(0x00E0));
		encoding.add(new Rfu(0x000F));
		
		SimpleBitfieldFormatter sbf = new SimpleBitfieldFormatter(encoding);
		
		sbf.process(new byte[] { 0x31, 0x12 });
		String expected = "0011000100010010\n"
		                + "0............... flag 1 dis\n"
		                + ".0.............. f2d\n"
		                + "..1............. flag 3 en\n"
		                + "...1............ f4e\n"
		                + "....0001........ this\n"
		                + "........000..... RFU\n"
		                + "............0010 RFU - must be 0\n";
		assertThat(sbf.getResult(), equalTo(expected));
	}
	
	@Test
	public void SimpleBitfieldFormatter_repeat() {
		Collection<IBitfieldEncoding> subEnc = new ArrayList<IBitfieldEncoding>();
		subEnc.add(new Flag(0x80, "flag 1", "en", "dis", true));
		subEnc.add(new Flag(0x01, "flag 2", "en", "dis", true));
		Collection<IBitfieldEncoding> enc = new ArrayList<IBitfieldEncoding>();
		enc.add(new Repeat(1, "repeat", subEnc));
		
		SimpleBitfieldFormatter sbf = new SimpleBitfieldFormatter(enc);
		
		sbf.process(new byte[] { (byte)0x81, 0x00});
		String expected = "1000000100000000\n"
		                + "10000001........ repeat 0\n"
		                + "1...............   flag 1 en\n"
		                + ".......1........   flag 2 en\n"
		                + "........00000000 repeat 1\n"
		                + "........0.......   flag 1 dis\n"
		                + "...............0   flag 2 dis\n";
		assertThat(sbf.getResult(), equalTo(expected));
	}
}
