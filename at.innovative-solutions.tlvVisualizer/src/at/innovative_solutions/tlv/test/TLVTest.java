package at.innovative_solutions.tlv.test;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.ErrorTLV;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.ParseError;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.SimpleFormatter;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

public class TLVTest {	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void test_parseTLV_simplePrimitive() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x9F, 0x06, 0x02, 0x12, 0x34});
		final TLV parsed = TLV.parseTLV(input);
		
		assertTrue("is primitive", parsed instanceof PrimitiveTLV);
		assertTrue("id", new ID(ID.CLASS_CONTEXT, true, 6).equalContents(parsed.getID()));
		assertEquals("length", 2, parsed.getLength());
		assertTrue("data", Arrays.equals(new byte[] {0x12, 0x34}, ((PrimitiveTLV)parsed).getData()));
	}
	
	@Test
	public void test_parseTLV_emptyPrimitive() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x80, 0x00});
		final TLV parsed = TLV.parseTLV(input);
		
		assertTrue("is primitive", parsed instanceof PrimitiveTLV);
		assertTrue("id", new ID(ID.CLASS_CONTEXT, true, 0).equalContents(parsed.getID()));
		assertEquals("length", 0, parsed.getLength());
		assertTrue("data", Arrays.equals(new byte[] {}, ((PrimitiveTLV)parsed).getData()));
	}
	
	@Test
	public void test_parseTLV_simpleConstructed() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0xA6, 0x03, 0x45, 0x01, (byte) 0xED});
		final TLV parsed = TLV.parseTLV(input);
		
		assertTrue("is constructed", parsed instanceof ConstructedTLV);
		assertTrue("id", new ID(ID.CLASS_CONTEXT, false, 6).equalContents(parsed.getID()));
		//assertEquals("length", 3, parsed.getLength());
		final ConstructedTLV cp = (ConstructedTLV)parsed;
		assertEquals("1 inner tlv", 1, cp.getTLVs().size());
		final TLV inner = cp.getTLVs().get(0);
		assertTrue("inner: is primitive", inner instanceof PrimitiveTLV);
		assertTrue("inner: id", new ID(ID.CLASS_APPLICATION, true, 5).equalContents(inner.getID()));
		assertEquals("inner: length", 1, inner.getLength());
		assertTrue("inner: data", Arrays.equals(new byte[] {(byte) 0xED}, ((PrimitiveTLV)inner).getData()));
	}
	
	@Test
	public void test_parseTLV_multipleConstructed() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0xA7, 0x07, 0x45, 0x01, 0x00, 0x46, 0x02, (byte) 0xAF, (byte) 0xFE});
		final TLV parsed = TLV.parseTLV(input);
		
		assertTrue("is constructed", parsed instanceof ConstructedTLV);
		assertEquals("id", new ID(ID.CLASS_CONTEXT, false, 7), parsed.getID());
		//assertEquals("length", 1, parsed.getLength)
		
		final ConstructedTLV cp = (ConstructedTLV)parsed;
		assertEquals("2 inner tlvs", 2, cp.getTLVs().size());
		
		final TLV inner1 = cp.getTLVs().get(0);
		assertTrue("inner 1: is primitive", inner1 instanceof PrimitiveTLV);
		assertEquals("inner 1: id", new ID(ID.CLASS_APPLICATION, true, 5), inner1.getID());
		assertEquals("inner 1: length", 1, inner1.getLength());
		assertTrue("inner 1: data", Arrays.equals(new byte[] {0x00}, ((PrimitiveTLV)inner1).getData()));

		final TLV inner2 = cp.getTLVs().get(1);
		assertTrue("inner 2: is primitive", inner2 instanceof PrimitiveTLV);
		assertEquals("inner 2: id", new ID(ID.CLASS_APPLICATION, true, 6), inner2.getID());
		assertEquals("inner 2: length", 2, inner2.getLength());
		assertTrue("inner 2: data", Arrays.equals(new byte[] {(byte) 0xAF, (byte) 0xFE}, ((PrimitiveTLV)inner2).getData()));
	}
	
	@Test
	public void test_parseTLV_dataTooShort() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0xA0, 0x02, 0x00});
		thrown.expect(ParseError.class);
		thrown.expectMessage("Frame too short for expected data length (2 bytes)");
		TLV.parseTLV(input);
	}
	
	@Test
	public void test_parseTLV_indefiniteLength() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {0x02, (byte) 0x80, 0x01, 0x02, 0x03, 0x00, 0x00});
		final PrimitiveTLV parsed = (PrimitiveTLV)TLV.parseTLV(input);
		
		assertEquals("id", new ID(ID.CLASS_UNIVERSAL, true, 2), parsed.getID());
		assertTrue("data", Arrays.equals(new byte[] {0x01, 0x02, 0x03}, parsed.getData()));
		assertEquals("indefinite", true, parsed.isIndefiniteLength());
	}
	
	@Test
	public void test_parseTLV_multipleHierarchies() {
		final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes("A10C8202AFFEA3068401DE8501AD"));
		final ConstructedTLV parsed = (ConstructedTLV)TLV.parseTLV(input);
		
		assertEquals("root id", new ID(ID.CLASS_CONTEXT, false, 1), parsed.getID());
		
		assertEquals("root # children", 2, parsed.getTLVs().size());
		assertEquals("child 0 id", new ID(ID.CLASS_CONTEXT, true, 2), parsed.getTLVs().get(0).getID());
		assertEquals("child 1 id", new ID(ID.CLASS_CONTEXT, false, 3), parsed.getTLVs().get(1).getID());
		final PrimitiveTLV c0 = (PrimitiveTLV)parsed.getTLVs().get(0);
		assertArrayEquals("child 0 data", new byte[] {(byte) 0xaf, (byte) 0xfe}, c0.getData());
		
		final ConstructedTLV c1 = (ConstructedTLV)parsed.getTLVs().get(1);
		assertEquals("child 1 # children", 2, c1.getTLVs().size());
		assertEquals("child 1.0 id", new ID(ID.CLASS_CONTEXT, true, 4), c1.getTLVs().get(0).getID());
		assertEquals("child 1.1 id", new ID(ID.CLASS_CONTEXT, true, 5), c1.getTLVs().get(1).getID());
		
		final PrimitiveTLV c10 = (PrimitiveTLV)c1.getTLVs().get(0);
		final PrimitiveTLV c11 = (PrimitiveTLV)c1.getTLVs().get(1);
		assertArrayEquals("child 1.0 data", new byte[] {(byte) 0xde}, c10.getData());
		assertArrayEquals("child 1.1 data", new byte[] {(byte) 0xad}, c11.getData());	
	}

	@Test
	public void test_parseTLVs_single() {
		final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes("A10C8202AFFEA3068401DE8501AD"));
		final List<TLV> parsed = TLV.parseTLVs(input);
		
		assertEquals("tlv count", 1, parsed.size());
		
		assertEquals("tlv",
			new ConstructedTLV(new ID(ID.CLASS_CONTEXT, false, 1),
				Arrays.asList(
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), Utils.hexStringToBytes("AFFE")),
					new ConstructedTLV(new ID(ID.CLASS_CONTEXT, false, 3),
						Arrays.asList(
							new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] {(byte) 0xde}),
							new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 5), new byte[] {(byte) 0xad}))))),
			parsed.get(0));
	}
	
	@Test
	public void test_parseTLVs_multiple() {
		final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes("8407A0000000041010A50F500A4D617374657243617264870101"));
		final List<TLV> parsed = TLV.parseTLVs(input);
		
		assertEquals("tlv count", 2, parsed.size());
		
		assertEquals("tlv 1",
			         new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 0x04), Utils.hexStringToBytes("A0000000041010")),
			         parsed.get(0));
		assertEquals("tlv 2",
				new ConstructedTLV(new ID(ID.CLASS_CONTEXT, false, 0x05),
					Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 0x10), Utils.hexStringToBytes("4D617374657243617264")),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 0x7), new byte[] {0x01}))),
				parsed.get(1));
	}
	
	@Test
	public void test_parseTLVWithErrors_invalidID() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { (byte) 0x9f });
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ErrorTLV.class));
		assertThat(((ErrorTLV) parsed).getError(), is("Not enough bytes to extract ID"));
	}

	@Test
	public void test_parseTLVWithErrors_cutoffData() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { 0x01, 0x03, 0x11, 0x22 });
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ErrorTLV.class));
		final ErrorTLV errorTLV = (ErrorTLV) parsed;
		assertThat(errorTLV.getError(), is("Frame too short for expected data length (3 bytes)"));
		assertThat(errorTLV.getID(), is(new ID(ID.CLASS_UNIVERSAL, true, 1)));
		assertThat(errorTLV.getLength(), is(3));
	}
	
	@Test
	public void test_parseTLVWithErrors_cutoffLength() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { (byte) 0x82, (byte) 0x82, 0x11 });
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ErrorTLV.class));
		final ErrorTLV errorTLV = (ErrorTLV) parsed;
		assertThat(errorTLV.getError(), is("Not enough bytes to extract length"));
		assertThat(errorTLV.getID(), is(new ID(ID.CLASS_CONTEXT, true, 2)));
		assertThat(errorTLV.getLength(), is(0));
	}
	
	@Test
	public void test_parseTLVWithErrors_invalidLength() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {0x08, (byte) 0xff});
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ErrorTLV.class));
		final ErrorTLV errorTLV = (ErrorTLV) parsed;
		assertThat(errorTLV.getError(), is("Invalid length byte (first byte 0xff)"));
		assertThat(errorTLV.getID(), is(new ID(ID.CLASS_UNIVERSAL, true, 8)));
		assertThat(errorTLV.getLength(), is(0));
	}
	
	@Test
	public void test_parseTLVWithErrors_lengthMissing() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { 0x44 });
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ErrorTLV.class));
		final ErrorTLV errorTLV = (ErrorTLV) parsed;
		assertThat(errorTLV.getError(), is("Not enough bytes to extract length"));
		assertThat(errorTLV.getID(), is(new ID(ID.CLASS_APPLICATION, true, 4)));
		assertThat(errorTLV.getLength(), is(0));
	}
	
	@Test
	public void test_parseTLVWithErrors_cutoffId() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { (byte) 0x9F });
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ErrorTLV.class));
		final ErrorTLV errorTLV = (ErrorTLV) parsed;
		assertThat(errorTLV.getError(), is("Not enough bytes to extract ID"));
		assertThat(errorTLV.getID(), is(nullValue()));
		assertThat(errorTLV.getLength(), is(0));
	}
	
	@Test
	public void test_parseTLVWithErrors_errorInHierarchy() {
		final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes("210401060000"));
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ConstructedTLV.class));
		assertThat(((ConstructedTLV) parsed).getTLVs().get(0), instanceOf(ErrorTLV.class));
		
		final ErrorTLV inside = (ErrorTLV) ((ConstructedTLV) parsed).getTLVs().get(0);
		assertThat(inside.getError(), is("Frame too short for expected data length (6 bytes)"));
		assertThat(inside.getID(), is(new ID(ID.CLASS_UNIVERSAL, true, 1)));
		assertThat(inside.getLength(), is(6));
	}
	
	@Test
	public void test_parseTLVWithErrors_lengthErrorAtFirstChild() {
		final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes("2108 08FF01 0603112233"));
		final TLV parsed = TLV.parseTLVWithErrors(input);
		
		assertThat(parsed, instanceOf(ConstructedTLV.class));
		assertThat(((ConstructedTLV) parsed).getTLVs().size(), is(1));
		assertThat(((ConstructedTLV) parsed).getTLVs().get(0), instanceOf(ErrorTLV.class));
		
		final ErrorTLV inside = (ErrorTLV) ((ConstructedTLV) parsed).getTLVs().get(0);
		assertThat(inside.getError(), is("Invalid length byte (first byte 0xff)"));
		assertThat(inside.getID(), is(new ID(ID.CLASS_UNIVERSAL, true, 8)));
		assertThat(inside.getLength(), is(0));
	}
	
	@Test
	public void test_parseTLVsWithError_twoErrors() {
		final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes("6204 01ff1122  05053344"));
		final List<TLV> parsed = TLV.parseTLVsWithErrors(input);
		
		assertThat(parsed.size(), is(2));
		assertThat(parsed.get(0), instanceOf(ConstructedTLV.class));
		assertThat(parsed.get(1), instanceOf(ErrorTLV.class));
		
		final ConstructedTLV first = (ConstructedTLV) parsed.get(0);

		assertThat(first.getID(), is(new ID(ID.CLASS_APPLICATION, false, 2)));
		assertThat(first.getTLVs().size(), is(1));
		assertThat(first.getTLVs().get(0), instanceOf(ErrorTLV.class));
		
		final ErrorTLV child = (ErrorTLV) first.getTLVs().get(0);
		
		assertThat(
				child,
				is(new ErrorTLV(
						ErrorTLV.ParseStage.ParsingLength,
						new ID(ID.CLASS_UNIVERSAL, true, 1),
						"ff stuff",
						0,
						false,
						Utils.hexStringToBytes("1122"))));
		
		final ErrorTLV second = (ErrorTLV) parsed.get(1);
		
		assertThat(
				second,
				is(new ErrorTLV(
						ErrorTLV.ParseStage.GettingData,
						new ID(ID.CLASS_UNIVERSAL, true, 5),
						"end stuff",
						5,
						false,
						Utils.hexStringToBytes("3344"))));
	}
	
	@Test
	public void test_parseLength_shortForm() {
		// clause 8.1.3.4
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {0x26});
		final int parsed = TLV.parseLength(input);
		assertEquals("length", 38, parsed);
		assertEquals("position", 1, input.position());
	}
	
	@Test
	public void test_parseLength_longForm1Byte() {
		// clause 8.1.3.5
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x81, (byte) 0xC9});
		final int parsed = TLV.parseLength(input);
		assertEquals("length", 201, parsed);
		assertEquals("position", 2, input.position());
	}
	
	@Test
	public void test_parseLength_longForm2Bytes() {
		// clause 8.1.3.5
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x82, 0x01, 0x01});
		final int parsed = TLV.parseLength(input);
		assertEquals("length", 257, parsed);
		assertEquals("position", 3, input.position());
	}
	
	@Test
	public void test_parseLength_highBits() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x82, (byte) 0x80, (byte) 0x80});
		final int parsed = TLV.parseLength(input);
		assertEquals("length", 0x8080, parsed);
		assertEquals("position", 3, input.position());
	}
	
	@Test
	public void test_parseLength_longForm4Bytes() {
		// clause 8.1.3.5 Note 2
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x84, 0x0, 0x0, 0x0, 0x1});
		final int parsed = TLV.parseLength(input);
		assertEquals("length", 1, parsed);
		assertEquals("position", 5, input.position());
	}
	
	@Test
	public void test_parseLength_longFormInvalidLength() {
		// clause 8.1.3.5 c)
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte)0xff});
	    thrown.expect(ParseError.class);
	    TLV.parseLength(input);
	}
	
	@Test
	public void test_parseLength_manyBytes() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x89, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01});
		final int ret = TLV.parseLength(input);
		assertEquals(1, ret);
	}
	
	@Test
	public void test_parseLength_tooBigForSignedInt() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x88, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
		thrown.expect(ParseError.class);
		thrown.expectMessage("length too big to fit into an integer");
		TLV.parseLength(input);
	}
	
	@Test
	public void test_parseLength_cutoffLength() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte) 0x83, 0x00, 0x00});
		thrown.expect(ParseError.class);
		thrown.expectMessage("Not enough bytes to extract length");
		TLV.parseLength(input);
	}
	
	@Test
	public void test_parseLength_indefiniteForm() {
		// clause 8.1.3.6
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {(byte)0x80});
		final Integer parsed = TLV.parseLength(input);
		assertNull("length", parsed);
		assertEquals("position", 1, input.position());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void test_findEnd_normal() throws Exception {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {0x01, 0x00, 0x01, 0x00, 0x00});
		final Class cls = TLV.class;
		final Method method = cls.getDeclaredMethod("findEnd", new Class[]{ByteBuffer.class});
		method.setAccessible(true);
		
		final int end = (int) method.invoke(null, input);
		assertEquals("end", 3, end);
		assertEquals("pos", 5, input.position());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void test_findEnd_offset() throws Exception {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {0x11, 0x11, 0x01, 0x01, 0x00, 0x00});
		input.position(2);
		final Class cls = TLV.class;
		final Method method = cls.getDeclaredMethod("findEnd", new Class[]{ByteBuffer.class});
		method.setAccessible(true);
		
		final int end = (int) method.invoke(null, input);
		assertEquals("end", 2, end);
		assertEquals("pos", 6, input.position());
	}
	
	// TODO check exception
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void test_findEnd_noEnd() throws Exception {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {0x01, 0x01, 0x00});
		final Class cls = TLV.class;
		final Method method = cls.getDeclaredMethod("findEnd", new Class[]{ByteBuffer.class});
		method.setAccessible(true);
		
		thrown.expectCause(IsInstanceOf.<Throwable>instanceOf(ParseError.class));
		method.invoke(null, input);
	}
}
