package at.innovative_solutions.tlv.bitfields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Node;

import at.innovative_solutions.tlv.Utils;

import static at.innovative_solutions.tlv.Utils.iterate;

public class BitfieldEncodingFactory {
	public static List<IBitfieldEncoding> loadEncoding(Node node) {
		List<IBitfieldEncoding> result = new ArrayList<IBitfieldEncoding>();
		
		try {
			for(Node child : iterate(node.getChildNodes())) {
				IBitfieldEncoding e = null;
				switch(child.getNodeName()) {
				case "flag":
					e = loadFlag(child);
					break;
				case "selection":
					e = loadSelection(child);
					break;
				case "rfu":
					e = loadRfu(child);
					break;
				case "repeat":
					e = loadRepeat(child);
					break;
				}
				if(e != null)
					result.add(e);
			}
		} catch(Exception e) {
			throw new RuntimeException("Error while processing encoding node '" + Utils.nodeToString(node) + "'", e);
		}

		return result;
	}

	public static Rfu loadRfu(Node node) {
		Long mask = null;

		for(Node c : iterate(node.getChildNodes())) {
			switch(c.getNodeName()) {
			case "mask":
				mask = Long.decode(c.getTextContent());
				break;
			}
		}

		if(mask == null)
			throw new RuntimeException("mask must be present for RFU: " + node.toString());

		return new Rfu(mask);
	}

	public static Flag loadFlag(Node node) {
		Integer bit = null;
		String name = null;
		String enabled = null;
		String disabled = null;
		boolean concat = true;

		Node concatAttrib = node.getAttributes().getNamedItem("concat");
		if(concatAttrib != null)
			concat = Boolean.parseBoolean(concatAttrib.getNodeValue());
		
		for(Node c : iterate(node.getChildNodes())) {
			switch(c.getNodeName()) {
			case "bit":
				bit = Integer.decode(c.getTextContent());
				break;
			case "name":
				name = c.getTextContent();
				break;
			case "enabled":
				enabled = c.getTextContent();
				break;
			case "disabled":
				disabled = c.getTextContent();
				break;
			}
		}
		
		if(bit == null || name == null || enabled == null || disabled == null)
			throw new RuntimeException("bit, name and enabled/disabled texts must be present for flag: " + node.toString());
		
		return new Flag(bit, name, enabled, disabled, concat);
	}
	
	public static Selection loadSelection(Node node) {
		Long mask = null;
		List<SelectionOption> options = new ArrayList<SelectionOption>();
		
		for(Node c : iterate(node.getChildNodes())) {
			switch(c.getNodeName()) {
			case "mask":
				mask = Long.decode(c.getTextContent());
				break;
			case "option":
				options.add(loadOption(c));
				break;
			}
		}
		
		if(mask == null)
			throw new RuntimeException("mask must be present for selection: " + node.toString());
		
		return new Selection(mask, options);
	}
	
	public static SelectionOption loadOption(Node node) {
		Long value = null;
		String name = null;
		
		for(Node c : iterate(node.getChildNodes())) {
			switch(c.getNodeName()) {
			case "value":
				value = Long.decode(c.getTextContent());
				break;
			case "name":
				name = c.getTextContent();
				break;
			}
		}
		
		if(value == null || name == null)
			throw new RuntimeException("value & name must be present for option: " + node.toString());
		
		return new SelectionOption(value, name);
	}
	
	public static Repeat loadRepeat(Node node) {
		Integer size = Integer.parseInt(node.getAttributes().getNamedItem("size").getNodeValue());
		String name = null;
		Collection<IBitfieldEncoding> encoding = null;
		
		try {
			for(Node child : iterate(node.getChildNodes())) {
				switch(child.getNodeName()) {
				case "name":
					name = child.getTextContent();
					break;
				}
			}
			
			encoding = loadEncoding(node);
		} catch(Exception e) {
			throw new RuntimeException("Error while processing encoding node '" + Utils.nodeToString(node) + "'", e);
		}
		
		if(size == null || name == null || encoding == null)
			throw new RuntimeException("size, name and encoding are mandatory for repeats");
		
		return new Repeat(size, name, encoding);
	}
	
	public static String toString(List<IBitfieldEncoding> encoding) {
		StringBuffer str = new StringBuffer();
		for(IBitfieldEncoding e : encoding) {
			str.append(e.toString() + "\n");
		}
		return str.toString();
	}
}
