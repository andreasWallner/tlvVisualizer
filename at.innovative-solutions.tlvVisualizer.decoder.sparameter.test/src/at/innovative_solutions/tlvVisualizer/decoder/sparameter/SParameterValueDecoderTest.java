package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;

import at.innovative_solutions.tlv.ConstructedTLV;
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
}
