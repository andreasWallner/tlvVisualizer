package at.innovative_solutions.tlvVisualizer.decoder.sparameter;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Node;

import static at.innovative_solutions.tlv.Utils.iterate;

public class EncodingFactory {
	public static Collection<Encoding> loadEncoding(Node node) {
		Collection<Encoding> result = new ArrayList<Encoding>();
		
		for(Node child : iterate(node.getChildNodes())) {
			Encoding e = null;
			switch(child.getNodeName()) {
			case "flag":
				e = loadFlag(child);
				break;
			case "selection":
				e = loadSelection(child);
				break;
			}
			if(e != null)
				result.add(e);
		}
		
		return result;
	}
	
	public static Flag loadFlag(Node node) {
		Long mask = null;
		String name = null;
		String enabled = null;
		String disabled = null;
		
		for(Node c : iterate(node.getChildNodes())) {
			switch(c.getNodeName()) {
			case "bit":
				mask = 1L << Long.decode(c.getTextContent());
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
		
		if(mask == null || name == null || enabled == null || disabled == null)
			throw new RuntimeException("mask & description must be present for flag: " + node.toString());
		
		return new Flag(mask, name, enabled, disabled);
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
}
