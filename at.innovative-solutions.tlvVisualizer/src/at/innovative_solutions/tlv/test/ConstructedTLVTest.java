package at.innovative_solutions.tlv.test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Test;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.Formatter;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

public class ConstructedTLVTest {
	@Test
	public void test_ConstructedTLV_simple() {
		final ID id = new ID(ID.CLASS_APPLICATION, false, 2);
		final ID s1ID = new ID(ID.CLASS_CONTEXT, true, 3);
		final ID s2ID = new ID(ID.CLASS_PRIVATE, true, 4);
		
		final PrimitiveTLV s1 = new PrimitiveTLV(s1ID, Utils.hexStringToBytes("1122"));
		final PrimitiveTLV s2 = new PrimitiveTLV(s2ID, Utils.hexStringToBytes("3344"));
		
		final ConstructedTLV ref = new ConstructedTLV(id, Arrays.asList(s1, s2));
		
		assertEquals("id", id, ref.getID());
		assertTrue("s1", s1 == ref.getTLVs().get(0));
		assertTrue("s2", s2 == ref.getTLVs().get(1));
		assertTrue("s1 parent", ref == ref.getTLVs().get(0).getParent());
		assertTrue("s2 parent", ref == ref.getTLVs().get(1).getParent());
	}
	
	@Test
	public void test_equals_simple() {
		ConstructedTLV ref = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 }),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 })));
		
		assertEquals("same",
			ref,
			new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 }),
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }))));
		
		assertNotEquals("other root id",
			ref,
			new ConstructedTLV(
				new ID(ID.CLASS_CONTEXT, false, 1),
				Arrays.asList(
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 }),
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }))));

		assertNotEquals("other sub tlvs",
			ref,
			new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] { 0x11, 0x22 }),
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }))));

		assertNotEquals("only one sub tlv",
			ref,
			new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }))));
		
		assertNotEquals("more sub tlv",
			ref,
			new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 }),
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }),
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] { 0x33, 0x44 }))));

		assertNotEquals("swapped sub tlvs",
			ref,
			new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }),
					new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 }))));
	}
	
	@Test
	public void test_accept_simple() {
		String result = "asdf";
		FormatterMock f = new FormatterMock(result);
		ConstructedTLV ref = new ConstructedTLV(null, new LinkedList<TLV>());
		
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
			throw new RuntimeException("called invalid method");
		}

		@Override
		public String format(ConstructedTLV tlv) {
			if(_param != null)
				throw new RuntimeException("second call to format is not allowed");
			_param = tlv;
			return _retVal;
		}
	}
	
	@Test
	public void test_toString_simple() {
		ID id = mock(ID.class);
		when(id.toString()).thenReturn("{id}");
		PrimitiveTLV s1 = mock(PrimitiveTLV.class);
		PrimitiveTLV s2 = mock(PrimitiveTLV.class);
		when(s1.toString()).thenReturn("{s1}");
		when(s2.toString()).thenReturn("{s2}");
		
		ConstructedTLV ref = new ConstructedTLV(id, Arrays.asList(s1, s2), false);
		
		assertEquals("ConstructedTLV({id}, <{s1}, {s2}, >, false)", ref.toString());
	}
}
