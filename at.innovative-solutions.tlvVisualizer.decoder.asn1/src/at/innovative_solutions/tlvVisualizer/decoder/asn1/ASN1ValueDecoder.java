package at.innovative_solutions.tlvVisualizer.decoder.asn1;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;
import at.innovative_solutions.tlv.ValueDecoder;

public class ASN1ValueDecoder implements ValueDecoder {
	private static HashMap<ID, ValueInfo> fInfos;
	
	static {
		fInfos = new HashMap<ID, ValueInfo>();
		
		addInfo(true, 0, "End-of-Content (EOC)", ASN1ValueDecoder::null_toString);
		addInfo(true, 1, "boolean", ASN1ValueDecoder::bool_toString);
		addInfo(true, 2, "integer", ASN1ValueDecoder::int_toString);
		addInfo(true, 3, "bitstring", ASN1ValueDecoder::bitstring_toString);
		addInfo(false, 3, "bitstring", ASN1ValueDecoder::bitstring_toString);
		addInfo(true, 4, "octetstring", ASN1ValueDecoder::octetstring_toString);
		addInfo(false, 4, "octetstring", ASN1ValueDecoder::octetstring_toString);
		addInfo(true, 5, "null", ASN1ValueDecoder::null_toString);
		addInfo(false, 16, "seqence", ASN1ValueDecoder::sequence_toString);
		addInfo(false, 17, "set", ASN1ValueDecoder::set_toString);
	}
	
	private static void addInfo(boolean encoding, int tag, String name, Function<TLV, String> toStringFunc) {
		ID id = new ID(0, encoding, tag);
		fInfos.put(id, new ValueInfo(id, name, toStringFunc));
	}
	
	private static String bool_toString(TLV tlv) {
		if (!tlv.getID().isPrimitive())
			return null;
		PrimitiveTLV ptlv = (PrimitiveTLV)tlv;
		byte[] bytes = ptlv.getData();
		if (bytes.length != 1)
			return null;
		if (bytes[0] == 0)
			return "false";
		else
			return "true";
	}
	
	private static String int_toString(TLV tlv) {
		if (!tlv.getID().isPrimitive())
			return null;
		
		PrimitiveTLV ptlv = (PrimitiveTLV)tlv;
		byte[] bytes = ptlv.getData();
		
		if (bytes.length == 0)
			return null;
		if (bytes.length > 1 &&
				((bytes[0] == 0 && (bytes[1] & 0x080) == 0) ||
				 ((bytes[0] & 0xff) == 0xff && (bytes[1] & 0x080) == 0x080)))
			return null;
		
		
		BigInteger integer = new BigInteger(bytes);
		return integer.toString();
	}
	
	private static String byteToBinaryString(byte b) {
		char result[] = {'0', '0', '0', '0', '0', '0', '0', '0'};
		byte mask = (byte)0x80;
		for(int idx = 0; idx < 8; idx++) {
			if((b & mask) != 0)
				result[idx] = '1';
			mask = (byte)((mask & 0xff) >> 1);
		}
		return new String(result);
	}
	
	// TODO MSB first / LSB first? (8.6.2.1)
	private static String bitstring_toString(TLV tlv) {
		StringBuilder sb = new StringBuilder();

		if (tlv instanceof PrimitiveTLV) {
			PrimitiveTLV ptlv = (PrimitiveTLV)tlv;
			byte[] data = ptlv.getData();

			if (data.length < 1 || (data.length == 1 && data[0] != 0)) // 8.6.2.3
				return null;
			else if (data[0] > 7)
				return null;
			else if (data.length == 1)
				return "";
			
			// loop over all except last
			for(int i = 1; i < data.length - 1; i++) {
				sb.append(byteToBinaryString(data[i])).append(" ");
			}
			
			String lastByte = byteToBinaryString(data[data.length - 1]);
			sb.append(lastByte.substring(0, 8 - data[0]));
			return sb.toString();
		}
		else {
			ConstructedTLV ctlv = (ConstructedTLV)tlv;
			final List<TLV> contained = ctlv.getTLVs();
			
			if(contained.size() == 0)
				return "";
			
			TLV lastTlv = contained.get(contained.size() - 1);
			for(TLV subtlv : contained) {
				ID subid = subtlv.getID();
				if ((subid.getTagClass() != ID.CLASS_UNIVERSAL) || (subid.getTagNumber() != 3))
					return null;
					
				String subresult = bitstring_toString(subtlv);
				sb.append(subresult);
				if(subtlv != lastTlv && subresult.length() != 0) {
					if((subresult.length() + 1) % 9 != 0)
						return null;
					sb.append(" ");
				}
			}
		}

		return sb.toString();
	}
	
	private static String octetstring_toString(TLV tlv) {
		StringBuilder sb = new StringBuilder();

		if (tlv instanceof PrimitiveTLV) {
			PrimitiveTLV ptlv = (PrimitiveTLV)tlv;
			byte[] data = ptlv.getData();

			return Utils.bytesToHexString(data);
		}
		else {
			ConstructedTLV ctlv = (ConstructedTLV)tlv;
			
			for(TLV subtlv : ctlv.getTLVs()) {
				ID subid = subtlv.getID();
				if ((subid.getTagClass() != ID.CLASS_UNIVERSAL) || (subid.getTagNumber() != 4))
					return null;
					
				String substr = octetstring_toString(subtlv);
				if(substr == null)
					return null;
				
				sb.append(substr);
			}
		}

		return sb.toString();
	}
	
	private static String null_toString(TLV tlv) {
		if (!tlv.getID().isPrimitive())
			return null;
		PrimitiveTLV ptlv = (PrimitiveTLV)tlv;
		
		if(ptlv.getData().length != 0)
			return null;
		
		return "null";
	}
	
	private static String sequence_toString(TLV tlv) {
		return "sequence";
	}
	
	private static String set_toString(TLV tlv) {
		return "set";
	}
	
	public ASN1ValueDecoder() {}

	@Override
	public String getName(TLV tlv) {
		ValueInfo info = fInfos.get(tlv.getID());
		return info != null ? info.fName : null;
	}

	@Override
	public String toString(TLV tlv) {
		ValueInfo info = fInfos.get(tlv.getID());
		if(info == null)
			return null;
		
		return info.fToStringFunc.apply(tlv);
	}

	@Override
	public byte[] toValue(String str, TLV tlv) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFormat(TLV tlv) {
		return null;
	}

	@Override
	public boolean isValueParsable(TLV tlv) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSimpleDecoded(TLV tlv) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid(TLV tlv) {
		// TODO Auto-generated method stub
		return false;
	}

}
