package at.innovative_solutions.tlv.test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Test;

import at.innovative_solutions.tlv.Formatter;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.Utils;

public class PrimitiveTLVTest {
	@Test
	public void test_PrimitiveTLV_simple() {
		final ID id = new ID(ID.CLASS_APPLICATION, true, 1);
		final byte[] data = { 0x11, 0x22 };
		final PrimitiveTLV definite = new PrimitiveTLV(id, data, false);
		assertEquals("def id", id, definite.getID());
		assertArrayEquals("def data", data, definite.getData());
		assertFalse("def", definite.isIndefiniteLength());
		
		final PrimitiveTLV indefinite = new PrimitiveTLV(id, data, true);
		assertEquals("indef id", id, indefinite.getID());
		assertArrayEquals("indef data", data, indefinite.getData());
		assertTrue("indef", indefinite.isIndefiniteLength());
	}
	
	@Test
	public void test_equals_simple() {
		PrimitiveTLV ref = new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 20), Utils.hexStringToBytes("1122"));
		assertEquals(new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 20), Utils.hexStringToBytes("1122")), ref);
		assertNotEquals(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 20), Utils.hexStringToBytes("1122")), ref);
		assertNotEquals(new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 20), Utils.hexStringToBytes("1121")), ref);
		assertNotEquals(new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 20), Utils.hexStringToBytes("1121"), true), ref);
	}
	
	@Test
	public void test_getData_simple() {
		PrimitiveTLV ref = new PrimitiveTLV(null, Utils.hexStringToBytes("1122"));
		assertArrayEquals(new byte[] {0x11, 0x22}, ref.getData());
	}
	
	@Test
	public void test_getLength_simple() {
		PrimitiveTLV ref = new PrimitiveTLV(null, Utils.hexStringToBytes("112233"));
		assertEquals(3, ref.getLength());
	}
	
	@Test
	public void test_accept_simple() {
		String result = "asdf";
		@SuppressWarnings("unchecked")
		Formatter<String> f = (Formatter<String>) mock(Formatter.class);
		PrimitiveTLV ref = new PrimitiveTLV(null, null);
		when(f.format(ref)).thenReturn(result);
		
		String retVal = ref.accept(f);
		
		verify(f).format(ref);
		assertTrue("retval correct", retVal == result);
	}

	@Test
	public void test_toString_simple() {
		ID id = mock(ID.class);
		when(id.toString()).thenReturn("{id}");
		PrimitiveTLV ref = new PrimitiveTLV(id, new byte[] {0x11, 0x22}, false);
		assertEquals("PrimitiveTLV({id}, [0x11,0x22,], false)", ref.toString());
	}
}
