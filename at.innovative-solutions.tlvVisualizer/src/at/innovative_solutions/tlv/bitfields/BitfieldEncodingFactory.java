package at.innovative_solutions.tlv.bitfields;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Node;

import static at.innovative_solutions.tlv.Utils.iterate;

public class BitfieldEncodingFactory {
	public static Collection<IBitfieldEncoding> loadEncoding(Node node) {
		Collection<IBitfieldEncoding> result = new ArrayList<IBitfieldEncoding>();
		
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
			}
			if(e != null)
				result.add(e);
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
			throw new RuntimeException("mask & description must be present for flag: " + node.toString());
		
		return new Flag(bit, name, enabled, disabled, concat);
	}
	
	public static Selection loadSelection(Node node) {
		Long mask = null;
		Collection<SelectionOption> options = new ArrayList<SelectionOption>();
		
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
	
	public static String toString(Collection<IBitfieldEncoding> encoding) {
		StringBuffer str = new StringBuffer();
		for(IBitfieldEncoding e : encoding) {
			str.append(e.toString() + "\n");
		}
		return str.toString();
	}
}
