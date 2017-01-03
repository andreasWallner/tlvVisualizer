package at.innovative_solutions.tlv.bitfields;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RangeTest {
	@Test
	public void Range() {
		assertThat(new Range(0x07E0), equalTo(new Range(10, 5)));
		assertThat(new Range(0xffff), equalTo(new Range(15, 0)));
		assertThat(new Range(0x0080), equalTo(new Range(7, 7)));
	}
}
