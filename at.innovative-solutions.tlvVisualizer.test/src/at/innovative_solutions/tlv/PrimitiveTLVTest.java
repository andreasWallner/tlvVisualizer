package at.innovative_solutions.tlv;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import at.innovative_solutions.tlv.ChangeEvent;
import at.innovative_solutions.tlv.ChangeListener;
import at.innovative_solutions.tlv.Formatter;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.Utils;

public class PrimitiveTLVTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
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
		assertThat(ref, is(not("PrimitiveTLV(ID(1, true, 20), [0x11,0x22,], false)")));
	}
	
	@Test
	public void test_getData_simple() {
		PrimitiveTLV ref = new PrimitiveTLV(null, Utils.hexStringToBytes("1122"));
		assertArrayEquals(new byte[] {0x11, 0x22}, ref.getData());
	}
	
	@Test
	public void test_getLength_simple() {
		PrimitiveTLV ref = new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 1), Utils.hexStringToBytes("112233"));
		assertEquals(3, ref.getLength());
	}
	
	@Test
	public void test_getSerializedLength_simple() {
		PrimitiveTLV ref = new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 1), Utils.hexStringToBytes("112233"));
		assertThat(ref.getSerializedLength(), is(5));
	}
	
	@Test
	public void test_setData_simple() {
		PrimitiveTLV ref = new PrimitiveTLV(null, Utils.hexStringToBytes("112233"));
		byte[] newData = Utils.hexStringToBytes("1234567890"); 
		ref.setData(newData);
		assertEquals(newData, ref.getData());
	}
	
	@Test
	public void test_accept_simple() {
		String result = "asdf";
		@SuppressWarnings("unchecked")
		Formatter<String> f = (Formatter<String>) mock(Formatter.class);
		PrimitiveTLV ref = new PrimitiveTLV(new ID(ID.CLASS_UNIVERSAL, true, 1), null);
		when(f.format(ref)).thenReturn(result);
		
		String retVal = ref.accept(f);
		
		verify(f).format(ref);
		assertThat(retVal, is(result));
	}
	
	@Test
	public void test_toBytes_someTLV() {
		PrimitiveTLV tlv = new PrimitiveTLV(
				new ID(ID.CLASS_UNIVERSAL, true, 7),
				Utils.hexStringToBytes("112233"));
		byte[] expected = Utils.hexStringToBytes("0703112233");
		assertThat(tlv.toBytes(), is(expected));
	}

	@Test
	public void test_toString_simple() {
		ID id = mock(ID.class);
		when(id.toString()).thenReturn("{id}");
		PrimitiveTLV ref = new PrimitiveTLV(id, new byte[] {0x11, 0x22, (byte)0xff}, false);
		assertEquals("PrimitiveTLV({id}, [0x11,0x22,0xff,], false)", ref.toString());
	}
	
	@Test
	public void test_changeEvent_getsRaised() {
		final ID id = new ID(ID.CLASS_APPLICATION, true, 1);
		PrimitiveTLV tlv = new PrimitiveTLV(id, new byte[] {0x11, 0x22}, false);
		ChangeListener cl = mock(ChangeListener.class);
		
		tlv.addChangeListener(cl);
		tlv.setData(new byte[] {0x12, 0x34});
		
		ChangeEvent expected = new ChangeEvent(tlv);
		verify(cl, times(1)).changed(eq(expected));
	}
	
	@Test
	public void test_changeEvent_addIsUnique() {
		final ID id = new ID(ID.CLASS_APPLICATION, true, 1);
		PrimitiveTLV tlv = new PrimitiveTLV(id, new byte[] {0x11, 0x22}, false);
		ChangeListener cl = mock(ChangeListener.class);
		
		tlv.addChangeListener(cl);
		tlv.addChangeListener(cl);
		tlv.setData(new byte[] {0x12, 0x34});
		
		ChangeEvent expected = new ChangeEvent(tlv);
		verify(cl, times(1)).changed(eq(expected));		
	}
	
	@Test
	public void test_changeEvent_notRaisedAfterRemove() {
		final ID id = new ID(ID.CLASS_APPLICATION, true, 1);
		PrimitiveTLV tlv = new PrimitiveTLV(id, new byte[] {0x11, 0x22}, false);
		ChangeListener cl = mock(ChangeListener.class);
		
		tlv.addChangeListener(cl);
		tlv.removeChangeListener(cl);
		tlv.setData(new byte[] {0x12, 0x34});
		
		verifyZeroInteractions(cl);
	}
}
