package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.Utils;
import static at.innovative_solutions.tlv.Utils.iterate;

class ValueInfo {
	final public ID fId;
	final public ValueInfo fParent;
	final public String fName;
	final public Integer fLength;
	final public Boolean fIsSelection;

	ValueInfo(ID id, ValueInfo parent, String name, Integer length, Boolean isSelection) {
		fId = id;
		fParent = parent;
		fName = name;
		fLength = length;
		fIsSelection = isSelection;
	}

	public static ValueInfo fromNode(Node tagNode, ArrayList<ValueInfo> knownNodes) {
		ID id = null;
		ValueInfo parent = null;
		String name = null;
		Integer length = null;
		Boolean isSelection = tagNode.getAttributes().getNamedItem("selection").getTextContent() == "true";

		NodeList children = tagNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			switch (node.getNodeName()) {
			case "id":
				id = ID.parseID(ByteBuffer.wrap(Utils.hexStringToBytes(node.getTextContent())));
				break;
			case "name":
				name = node.getTextContent();
				break;
			case "length":
				length = Integer.parseInt(node.getTextContent());
				break;
			case "parent":
				ID parentId = ID.parseID(ByteBuffer.wrap(Utils.hexStringToBytes(node.getTextContent())));
				ArrayList<ValueInfo> parentCandidates = findById(parentId, knownNodes);
				if(parentCandidates.size() > 1)
					throw new RuntimeException("invalid file format, more than one possible parent");
				parent = findFirstById(parentId, knownNodes);
			default:
				// ignore unknown tags
				break;
			}
		}
		
		if(id == null || name == null)
			throw new RuntimeException("invalid file format");
		
		return new ValueInfo(id, parent, name, length, isSelection);
	}
	
	public static ArrayList<ValueInfo> fromResource(Document doc) {
		ArrayList<ValueInfo> result = new ArrayList<ValueInfo>();
		doc.getDocumentElement().normalize();
		
		NodeList nList = doc.getElementsByTagName("tag");
		for(Node n : iterate(nList)) {
			ValueInfo info = fromNode(n, result);
			result.add(info);
		}
		
		return result;
	}
	
	public static ValueInfo findFirstById(ID id, ArrayList<ValueInfo> list) {
		for(ValueInfo v : list) {
			if(id.equals(v.fId))
				return v;
		}
		return null;
	}
	
	public static ValueInfo findByIds(ID id, ID parentId, ArrayList<ValueInfo> list) {
		for(ValueInfo v : list) {
			if(id.equals(v.fId) && (parentId == null || parentId.equals(v.fParent.fId)))
				return v;
		}
		return null;
	}
	
	public static ArrayList<ValueInfo> findById(ID id, ArrayList<ValueInfo> list) {
		ArrayList<ValueInfo> result = new ArrayList<ValueInfo>();
		for(ValueInfo v : list) {
			if(id.equals(v.fId))
				result.add(v);
		}
		return result;
	}
}