package at.innovative_solutions.tlvvisualizer.views;

import java.util.Arrays;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
// TODO capture length range
// TODO use schema for verification of format
public class TagInfo {
	public long _id;
	public String _name;
	public int[] _templates;
	public boolean _isPrimitive;
	public String _format;
	public String _description;
	public String _length;
	public String _source;
	
	public TagInfo(long id, String name, int[] templates, boolean isPrimitive, String format, String description, String length, String source) {
		_id = id;
		_name = name;
		_templates = templates;
		_isPrimitive = isPrimitive;
		_format = format;
		_description = description;
		_length = length;
		_source = source;
	}
	
	protected TagInfo() {}
	
	// TODO test
	public boolean equals(Object other) {
		if(!(other instanceof TagInfo))
			return false;
		
		return _id == ((TagInfo) other)._id
				&& _name.equals(((TagInfo) other)._name)
				&& Arrays.equals(_templates, ((TagInfo) other)._templates)
				&& _isPrimitive == ((TagInfo) other)._isPrimitive
				&& _format.equals(((TagInfo) other)._format)
				&& _description.equals(((TagInfo) other)._description)
				&& _length.equals(((TagInfo) other)._length)
				&& _source.equals(((TagInfo) other)._source);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TagInfo(");
		sb.append(_id).append(", ");
		sb.append(_name).append(", ");
		
		sb.append("{");
		for(int n = 0; _templates != null && n < _templates.length; n++) {
			if(n > 0)
				sb.append(", ");
			sb.append(_templates[n]);
		}
		sb.append("}, ");
		sb.append(_format).append(", ");
		sb.append(_description).append(", ");
		sb.append(_length).append(", ");
		sb.append(_source).append(")");
		
		return sb.toString();
	}
	
	public static TagInfo createFromNode(Node n) {
		TagInfo ti = new TagInfo();
		NodeList tagNode = n.getChildNodes();
		
		ti._templates = new int[0];

		for(int i = 0; i < tagNode.getLength(); i++) {
			Node node = tagNode.item(i);
			switch(node.getNodeName()) {
				case "id":
					ti._id = Long.parseLong(node.getTextContent(), 16);
					break;
				case "name":
					ti._name = node.getTextContent();
					break;
				case "templates":
					ti._templates = new int[0];
					break;
				case "primitive":
					ti._isPrimitive = node.getTextContent().equals("True");
					break;
				case "format":
					ti._format = node.getTextContent();
					break;
				case "description":
					ti._description = node.getTextContent();
					break;
				case "length":
					ti._length = node.getTextContent();
					break;
				case "source":
					ti._source = node.getTextContent();
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
			result.put(ti._id, ti);
		}
		
		return result;
	}
}
