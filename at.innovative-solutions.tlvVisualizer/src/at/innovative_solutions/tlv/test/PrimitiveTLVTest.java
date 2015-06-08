package at.innovative_solutions.tlv.test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mock;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.Formatter;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;
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
		FormatterMock f = new FormatterMock(result);
		PrimitiveTLV ref = new PrimitiveTLV(null, null);
		
		String retVal = ref.accept(f);
		
		assertTrue("retval correct", retVal == result);
		assertTrue("formatter param", f._param == ref);
	}
	
	class FormatterMock implements Formatter<String> {
		String _retVal;
		public TLV _param = null;
		
		public FormatterMock(String retVal) {
			_retVal = retVal;
		}
		
		@Override
		public String format(TLV tlv) {
			throw new RuntimeException("called invalid method");
		}

		@Override
		public String format(PrimitiveTLV tlv) {
			if(_param != null)
				throw new RuntimeException("second call to format is not allowed");
			_param = tlv;
			return _retVal;
		}

		@Override
		public String format(ConstructedTLV tlv) {
			throw new RuntimeException("called invalid method");
		}
		
	}
	
	@Test
	public void test_toString_simple() {
		ID id = mock(ID.class);
		when(id.toString()).thenReturn("{id}");
		PrimitiveTLV ref = new PrimitiveTLV(id, new byte[] {0x11, 0x22}, false);
		assertEquals("PrimitiveTLV({id}, [0x11,0x22,], false)", ref.toString());
	}
}
