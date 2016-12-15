package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

import java.io.File;
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
		Bundle bundle = Platform
				.getBundle("at.innovative-solutions.tlvVisualizer.decoder.sparameter");
		URL fileURL = bundle.getEntry("resources/sparam.xml");
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
		// TODO do we want a length check here?
		return true;
	}
}
