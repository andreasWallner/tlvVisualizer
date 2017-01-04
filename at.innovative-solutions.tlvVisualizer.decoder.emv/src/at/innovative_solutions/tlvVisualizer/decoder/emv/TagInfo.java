package at.innovative_solutions.tlvVisualizer.decoder.emv;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.innovative_solutions.tlv.bitfields.BitfieldEncodingFactory;
import at.innovative_solutions.tlv.bitfields.IBitfieldEncoding;
// TODO capture length range
// TODO use schema for verification of format
public class TagInfo {
	// TODO use ID class
	public long fId;
	public String fName;
	public int[] fTemplates;
	public boolean fIsPrimitive;
	public String fFormat;
	public String fDescription;
	public String fLength;
	public String fSource;
	public List<IBitfieldEncoding> fEncoding;
	
	public TagInfo(long id, String name, int[] templates, boolean isPrimitive, String format, String description, String length, String source, List<IBitfieldEncoding> encoding) {
		fId = id;
		fName = name;
		fTemplates = templates;
		fIsPrimitive = isPrimitive;
		fFormat = format;
		fDescription = description;
		fLength = length;
		fSource = source;
		fEncoding = encoding;
	}
	
	protected TagInfo() {}
	
	// TODO test
	public boolean equals(Object other) {
		if(!(other instanceof TagInfo))
			return false;
		
		return fId == ((TagInfo) other).fId
				&& fName.equals(((TagInfo) other).fName)
				&& Arrays.equals(fTemplates, ((TagInfo) other).fTemplates)
				&& fIsPrimitive == ((TagInfo) other).fIsPrimitive
				&& fFormat.equals(((TagInfo) other).fFormat)
				&& fDescription.equals(((TagInfo) other).fDescription)
				&& fLength.equals(((TagInfo) other).fLength)
				&& fSource.equals(((TagInfo) other).fSource);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TagInfo(");
		sb.append(fId).append(", ");
		sb.append(fName).append(", ");
		
		sb.append("{");
		for(int n = 0; fTemplates != null && n < fTemplates.length; n++) {
			if(n > 0)
				sb.append(", ");
			sb.append(fTemplates[n]);
		}
		sb.append("}, ");
		sb.append(fFormat).append(", ");
		sb.append(fDescription).append(", ");
		sb.append(fLength).append(", ");
		sb.append(fSource).append(")");
		
		return sb.toString();
	}
	
	public static TagInfo createFromNode(Node n) {
		TagInfo ti = new TagInfo();
		NodeList tagNode = n.getChildNodes();
		
		ti.fTemplates = new int[0];

		for(int i = 0; i < tagNode.getLength(); i++) {
			Node node = tagNode.item(i);
			switch(node.getNodeName()) {
				case "id":
					ti.fId = Long.parseLong(node.getTextContent(), 16);
					break;
				case "name":
					ti.fName = node.getTextContent();
					break;
				case "templates":
					ti.fTemplates = new int[0];
					break;
				case "primitive":
					ti.fIsPrimitive = node.getTextContent().equals("True");
					break;
				case "format":
					ti.fFormat = node.getTextContent();
					break;
				case "description":
					ti.fDescription = node.getTextContent();
					break;
				case "length":
					ti.fLength = node.getTextContent();
					break;
				case "source":
					ti.fSource = node.getTextContent();
					break;
				case "encoding":
					ti.fEncoding = BitfieldEncodingFactory.loadEncoding(node);
					break;
			}
		}
		return ti;
	}
		
	public static HashMap<Long, TagInfo> loadXML(Document doc) {
		HashMap<Long, TagInfo> result = new HashMap<Long, TagInfo>();
		doc.getDocumentElement().normalize();
		
		NodeList nList = doc.getElementsByTagName("tag");
		for(int n = 0; n < nList.getLength(); n++) {
			TagInfo ti = TagInfo.createFromNode(nList.item(n));
			result.put(ti.fId, ti);
		}
		
		return result;
	}
}
