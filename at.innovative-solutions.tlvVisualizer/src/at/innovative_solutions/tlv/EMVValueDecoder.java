package at.innovative_solutions.tlv;

import java.io.UnsupportedEncodingException;

// TODO character set checks
// TODO trailing character check on cn
public class EMVValueDecoder {
	public static String asString(final byte[] data, String format) throws UnsupportedEncodingException {
		format = format.split(" ")[0];
		
		long longVal = 0;
		StringBuilder sb;
		switch(format) {
		case "a":
		case "an":
		case "ans":
			return new String(data, "ASCII");
		case "b":
			for(int n = 0; n < data.length; ++n)
				longVal = (longVal << 8) | (data[n] & 0xff);
			return Long.toString(longVal);
		case "cn":
			sb = new StringBuilder();
			for(int n = 0; n < data.length * 2; ++n) {
				int nibble = n % 2 == 0 ? (data[n/2] & 0xff) >>> 4 : data[n/2] & 0xf;
				if(nibble == 0xf)
					break;
				sb.append(nibble);
			}
			return sb.toString();
		case "n":
			sb = new StringBuilder();
			boolean leadingZeros = true;
			for(int n = 0; n < data.length * 2; ++n) {
				int nibble = n % 2 == 0 ? (data[n/2] & 0xff) >>> 4 : data[n/2] & 0xf;
				if(nibble == 0 && leadingZeros)
					continue;
				leadingZeros = false;
				sb.append(nibble);
			}
			return sb.toString();
		default:
			return "";
		}
	}
}
