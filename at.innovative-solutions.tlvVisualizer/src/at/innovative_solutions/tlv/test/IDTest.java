package at.innovative_solutions.tlv.test;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.junit.Test;

import at.innovative_solutions.tlv.ID;

public class IDTest extends TestCase {
	@Test
	public void test_ID_noLen() {
		final ID ref1 = new ID(ID.CLASS_APPLICATION, true, 1);
		assertEquals("1 class", ID.CLASS_APPLICATION, ref1.getTagClass());
		assertEquals("1 prim", true, ref1.isPrimitive());
		assertEquals("1 num", 1, ref1.getTagNumber());
		
		final ID ref2 = new ID(ID.CLASS_CONTEXT, false, 2);
		assertEquals("2 class", ID.CLASS_CONTEXT, ref2.getTagClass());
		assertEquals("2 prim", false, ref2.isPrimitive());
		assertEquals("2 num", 2, ref2.getTagNumber());
	}
	
	@Test
	public void test_equals_noLen() {
		final ID ref = new ID(ID.CLASS_CONTEXT, false, 20);
		assertEquals(new ID(ID.CLASS_CONTEXT, false, 20), ref);
		assertNotEquals(new ID(ID.CLASS_APPLICATION, false, 20), ref);
		assertNotEquals(new ID(ID.CLASS_CONTEXT, true, 20), ref);
		assertNotEquals(new ID(ID.CLASS_CONTEXT, false, 21), ref);
		assertNotEquals(new ID(ID.CLASS_CONTEXT, false, 20, 2), ref);
	}
	
	@Test
	public void test_equals_withLen() {
		final ID ref = new ID(ID.CLASS_CONTEXT, false, 20, 2);
		assertEquals(new ID(ID.CLASS_CONTEXT, false, 20, 2), ref);
		assertNotEquals(new ID(ID.CLASS_APPLICATION, false, 20, 2), ref);
		assertNotEquals(new ID(ID.CLASS_CONTEXT, true, 20, 2), ref);
		assertNotEquals(new ID(ID.CLASS_CONTEXT, false, 21, 2), ref);
		assertNotEquals(new ID(ID.CLASS_CONTEXT, false, 20, 3), ref);
		assertNotEquals(new ID(ID.CLASS_CONTEXT, false, 20), ref);
	}
	
	@Test
	public void test_equalContents_all() {
		final ID ref = new ID(ID.CLASS_CONTEXT, false, 20);
		assertTrue(ref.equalContents(new ID(ID.CLASS_CONTEXT, false, 20)));
		assertTrue(ref.equalContents(new ID(ID.CLASS_CONTEXT, false, 20, 4)));
		assertFalse(ref.equalContents(new ID(ID.CLASS_CONTEXT, false, 21)));
		assertFalse(ref.equalContents(new ID(ID.CLASS_CONTEXT, true, 20)));
		assertFalse(ref.equalContents(new ID(ID.CLASS_APPLICATION, false, 20)));
	}
	
	@Test
	public void test_parseID_under30() {
		// clause 8.1.2.3
		final ByteBuffer input = ByteBuffer.wrap(new byte[] {0x10});
		final ID parsed = ID.parseID(input);
		assertEquals("position", 1, input.position());
		assertEquals("class", ID.CLASS_UNIVERSAL, parsed.getTagClass());
		assertEquals("primitive", true, parsed.isPrimitive());
		assertEquals("tag number", 16, parsed.getTagNumber());
	}
	
	@Test
	public void test_parseID_over30() {
		// clause 8.1.2.4
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { 0x7f, 0x3a });
		final ID parsed = ID.parseID(input);
		assertEquals("position", 2, input.position());
		assertEquals("class", ID.CLASS_APPLICATION, parsed.getTagClass());
		assertEquals("primitive", false, parsed.isPrimitive());
		assertEquals("tag number", 0x3a, parsed.getTagNumber());
	}
	
	@Test
	public void test_parseID_over127() {
		// clause 8.1.2.4
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { (byte) 0xbf, (byte) 0xa7, 0x20 });
		final ID parsed = ID.parseID(input);
		assertEquals("position", 3, input.position());
		assertEquals("class", ID.CLASS_CONTEXT, parsed.getTagClass());
		assertEquals("primitive", false, parsed.isPrimitive());
		assertEquals("tag number", 5024, parsed.getTagNumber());
	}
	
	@Test
	public void test_parseID_oversized() {
		final ByteBuffer input = ByteBuffer.wrap(new byte[] { (byte) 0x9f, 0x07 });
		final ID parsed = ID.parseID(input);
		
		assertEquals("position", 2, input.position());
		assertEquals("class", ID.CLASS_CONTEXT, parsed.getTagClass());
		assertEquals("primitive", true, parsed.isPrimitive());
		assertEquals("tag number", 7, parsed.getTagNumber());
		assertEquals("tag length", 1, parsed.getLongFormByteCnt());
	}
	
	@Test
	public void test_toBytes_under30() {
		final ID id = new ID(ID.CLASS_UNIVERSAL, true, 0x2);
		assertArrayEquals("id bytes", new byte[] { 0x02 }, id.toBytes());
	}
	
	@Test
	public void test_toBytes_under30OverrideLength() {
		final ID id = new ID(ID.CLASS_APPLICATION, false, 0x8, 1);
		assertArrayEquals("id bytes", new byte[] {(byte) 0x7F, 0x08}, id.toBytes());
	}
	
	@Test
	public void test_toBytes_over127() {
		final ID id = new ID(ID.CLASS_PRIVATE, true, 128);
		assertArrayEquals("id bytes", new byte[] {(byte) 0xDF, (byte) 0x81, 0x00}, id.toBytes());
	}
	
	@Test
	public void test_toBytes_under16384OverrideLength() {
		final ID id = new ID(ID.CLASS_CONTEXT, false, 30, 4);
		assertArrayEquals("id bytes", new byte[] {(byte) 0xBF, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x1E}, id.toBytes());
	}
	
	@Test
	public void test_toLong_over127() {
		final ID id = new ID(ID.CLASS_PRIVATE, true, 128);
		assertEquals("id long", 0xDF8100L, id.toLong());
	}
}
