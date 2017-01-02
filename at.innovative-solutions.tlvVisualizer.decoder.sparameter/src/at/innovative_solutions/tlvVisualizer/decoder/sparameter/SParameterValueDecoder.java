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
import at.innovative_solutions.tlv.bitfields.SimpleBitfieldFormatter;

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
		return Utils.hexStringToBytes(str);
	}

	@Override
	public String getFormat(TLV tlv) {
		return null;
	}

	@Override
	public boolean isValueParsable(TLV tlv) {
		return true;
	}
	
	public String getSimpleDecoded(final TLV tlv) {
		if(!(tlv instanceof PrimitiveTLV) || tlv.getID() == null)
			return null;
		PrimitiveTLV ptlv = (PrimitiveTLV)tlv;
		ID parentId = tlv.getParent() != null ? tlv.getParent().getID() : null;
		ValueInfo info = ValueInfo.findByIds(ptlv.getID(), parentId, fTags);
		if(info == null)
			return null;
		
		if(info.fLength != null && ptlv.getData().length != info.fLength)
			return "Error: Invalid Length (!=" + info.fLength + ")";
		
		SimpleBitfieldFormatter formatter = new SimpleBitfieldFormatter(info.fEncodings);
		formatter.process(ptlv.getData());
		return formatter.getResult();
	}
	
	public boolean isValid(final TLV tlv) {
		// TODO
		return true;
	}
}