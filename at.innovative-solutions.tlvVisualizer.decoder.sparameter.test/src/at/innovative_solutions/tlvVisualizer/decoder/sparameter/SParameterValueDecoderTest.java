package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Test;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

public class SParameterValueDecoderTest {
	@Test
	public void test_getSimpleDecoded() {
		final ConstructedTLV t = (ConstructedTLV)TLV.parseTLV(ByteBuffer.wrap(Utils.hexStringToBytes("A204 80021900")));
		final TLV decTlv = t.getTLVs().get(0);
		SParameterValueDecoder d = new SParameterValueDecoder();
		assertThat(d.getSimpleDecoded(decTlv), not(nullValue()));
	}
	
	// bug in v1.0
	@Test
	public void checkingLookupWithNullParent() {
		ID id = new ID(2, false, 0);
		
		ValueInfo vi = new ValueInfo(id, null, "asdf", 0, null);
		ArrayList<ValueInfo> list = new ArrayList<>();
		list.add(vi);
		
		assertThat(ValueInfo.findByIds(id, id, list), is(nullValue()));
	}
}
