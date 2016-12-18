package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.ValueDecoder;

public class SParameterValueDecoder implements ValueDecoder {
	private ArrayList<ValueInfo> fTags;

	public SParameterValueDecoder() {
		/*Bundle bundle = Platform
				.getBundle("at.innovative-solutions.tlvVisualizer.decoder.sparameter");
		URL fileURL = bundle.getEntry("resources/sparam.xml");*/
		URL fileURL = null;
		try {
			fileURL = new URL("file:///C:/work/git/tlvVisualizer/at.innovative-solutions.tlvVisualizer.decoder.sparameter/resources/sparam.xml");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			File file = new File(FileLocator.resolve(fileURL).toURI());

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);

			fTags = ValueInfo.fromResource(doc);
		} catch (Exception e) {
			throw new RuntimeException("could not load resources", e);
		}
	}

	@Override
	public String getName(TLV tlv) {
		ID parentId = tlv.getParent() != null ? tlv.getParent().getID() : null;
		ValueInfo vi = ValueInfo.findByIds(tlv.getID(), parentId, fTags);
		if(vi != null)
			return vi.fName;
		return null;
	}

	@Override
	public String toString(TLV tlv) {
		if(!(tlv instanceof PrimitiveTLV))
			return null;
		return Utils.bytesToHexString(((PrimitiveTLV)tlv).getData());
	}

	@Override
	public byte[] toValue(String str, TLV tlv) {
		System.out.println(getSimpleDecoded(tlv));
		return Utils.hexStringToBytes(str);
	}

	@Override
	public String getFormat(TLV tlv) {
		return null;
	}

	@Override
	public boolean isValueParsable(TLV tlv) {
		// TODO do we want a length check here?
		return true;
	}
	
	public String getSimpleDecoded(final TLV tlv) {
		if(!(tlv instanceof PrimitiveTLV) || tlv.getID() == null)
			return null;
		PrimitiveTLV ptlv = (PrimitiveTLV)tlv;
		ValueInfo info = ValueInfo.findById(ptlv.getID(), fTags).get(0);
		if(info == null)
			return null;
		
		if(ptlv.getData().length != info.fLength)
			return "Error: Invalid Length";
		
		Long value = Utils.bytesToLong(ptlv.getData());
		
		StringBuilder builder = new StringBuilder();
		
		final int bitLength = 8 * info.fLength;
		String valueString = Long.toBinaryString(value);
		String valuePadding = Utils.printChars("0", bitLength - valueString.length());
		builder.append(valuePadding + valueString + "\n");
		
		for(Encoding e : info.fEncodings) {
			Range range = e.getRange();
			String bitString = Long.toBinaryString((value & e.fMask) >> range.fStop);
			String padding = Utils.printChars("0", range.fLength - bitString.length());
			
			builder.append(Utils.printChars(".", bitLength - 1 - range.fStart));
			builder.append(padding + bitString);
			builder.append(Utils.printChars(".", range.fStop));
			builder.append(" ");
			builder.append(e.getDescription(value));
			builder.append("\n");
		}
		
		return builder.toString();
	}
	
	public boolean isValid(final TLV tlv) {
		// TODO
		return true;
	}
}