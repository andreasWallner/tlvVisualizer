package at.innovative_solutions.tlv;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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
		assertThat("s1", ref.getTLVs().get(0), sameInstance(s1));
		assertThat("s2", ref.getTLVs().get(1), sameInstance(s2));
		assertThat("s1 parent", ref.getTLVs().get(0).getParent(), sameInstance(ref));
		assertThat("s2 parent", ref.getTLVs().get(1).getParent(), sameInstance(ref));
	}

	@Test
	public void test_equals_simple() {
		ConstructedTLV ref = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 }),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 })));

		assertThat(ref, not(new String()));

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

		assertThat(ref, is(not("ConstructedTLV(ID(1, false, 1), [PrimitiveTLV(ID(2, true, 2), [0x11,0x22,], false), PrimitiveTLV(ID(2, true, 3), [0x33,0x44,], false), ], false)")));
	}

	@Test
	public void test_getLength_simple() {
		ConstructedTLV tlv = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 })));
		assertThat(tlv.getLength(), is(8));
	}

	@Test
	public void test_getSerializedLength_simple() {
		ConstructedTLV tlv = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] { 0x33, 0x44 }),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] { 0x11, 0x22 })));
		assertThat(tlv.getSerializedLength(), is(10));
	}

	@Test
	public void test_accept_simple() {
		String result = "asdf";
		@SuppressWarnings("unchecked")
		Formatter<String> f = mock(Formatter.class);
		ConstructedTLV ref = new ConstructedTLV(new ID(ID.CLASS_UNIVERSAL, false, 1), new LinkedList<TLV>());
		when(f.format(ref)).thenReturn(result);

		String retVal = ref.accept(f);

		verify(f).format(ref);
		assertTrue("retval correct", retVal == result);
	}

	@Test
	public void test_toBytes_someTLV() {
		ConstructedTLV tlv = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11}),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] {0x22})));
		byte[] expected = Utils.hexStringToBytes("6106 830111 840122");
		assertThat(tlv.toBytes(), is(expected));
	}

	@Test
	public void test_toBytes_constructedInConstructed() {
		ConstructedTLV tlv = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new ConstructedTLV(
								new ID(ID.CLASS_APPLICATION, false, 1),
								Arrays.asList(
										new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11}),
										new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] {0x22})))));
		byte[] expected = Utils.hexStringToBytes("6108 6106 830111 840122");
		assertThat(tlv.toBytes(), is(expected));
	}

	@Test
	public void test_remove_someTlv() {
		PrimitiveTLV toRemove = new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] {0x22});
		ConstructedTLV tlv = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11}),
						toRemove));
		tlv.removeChild(toRemove);

		ConstructedTLV expected = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11})));
		assertThat(tlv, is(expected));
	}

	@Test
	public void test_remove_removeOfNonExistentTlv() {
		PrimitiveTLV toRemove = new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] {0x22});
		ConstructedTLV tlv = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11}),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] {0x22})));
		tlv.removeChild(toRemove);

		ConstructedTLV expected = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11}),
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 4), new byte[] {0x22})));
		assertThat(tlv, is(expected));
	}

	@Test
	public void test_appendChild_simple() {
		ConstructedTLV tlv = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				new LinkedList<TLV>());
		tlv.appendChild(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11}));

		ConstructedTLV expected = new ConstructedTLV(
				new ID(ID.CLASS_APPLICATION, false, 1),
				Arrays.asList(
						new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {0x11})));
		assertThat(tlv, is(expected));
	}
	
	@Test
	public void test_replaceChild_simple() {
		ConstructedTLV tlv = new ConstructedTLV(new ID(ID.CLASS_APPLICATION, false, 1));
		tlv.appendChild(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 1), new byte[] {} ));
		tlv.appendChild(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 2), new byte[] {} ));
		tlv.appendChild(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {} ));
		
		TLV toReplace = (TLV) tlv.getTLVs().toArray()[1];
		tlv.replaceChild(toReplace, new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 99), new byte[] {} ));
		
		ConstructedTLV expected = new ConstructedTLV(new ID(ID.CLASS_APPLICATION, false, 1));
		expected.appendChild(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 1), new byte[] {} ));
		expected.appendChild(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 99), new byte[] {} ));
		expected.appendChild(new PrimitiveTLV(new ID(ID.CLASS_CONTEXT, true, 3), new byte[] {} ));
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

		assertEquals("ConstructedTLV({id}, [{s1}, {s2}, ], false)", ref.toString());
	}
}
