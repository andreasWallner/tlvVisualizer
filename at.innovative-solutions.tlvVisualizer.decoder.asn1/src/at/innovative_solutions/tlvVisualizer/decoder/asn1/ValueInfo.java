package at.innovative_solutions.tlvVisualizer.decoder.asn1;

import java.util.function.Function;

import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.TLV;

public class ValueInfo {
	public final ID fId;
	public final String fName;
	public final Function<TLV, String> fToStringFunc;
	
	public ValueInfo(ID id, String name, Function<TLV, String> toStringFunc) {
		fId = id;
		fName = name;
		fToStringFunc = toStringFunc;
	}
}
