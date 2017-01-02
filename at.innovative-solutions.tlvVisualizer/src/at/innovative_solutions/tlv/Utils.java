package at.innovative_solutions.tlv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils {
	/**
	 * conversion function courtesy of maybeWeCouldStealAVan @ Stackoverflow
	 * http://stackoverflow.com/a/9855338
	 */
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for(int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	// TODO optimize?
	public static String bytesToBinString(byte[] bytes) {
		char[] binChars = new char[bytes.length * 8];
		int idx = 0;
		for(int i = 0; i < bytes.length; i++) {
			for(int bit = 7; bit >= 0; bit--, idx++)
				binChars[idx] = (((bytes[i] & 0xff) >> bit) & 0x01) != 0 ? '1' : '0'; 
		}
		return new String(binChars);
	}

	// TODO implement checks for characters
	public static byte[] hexStringToBytes(String s) {
		s = s.replaceAll(" ", "");
		boolean odd = s.length() % 2 != 0;

	    int byteLen = s.length() / 2 + (odd ? 1 : 0);
	    byte[] data = new byte[byteLen];

	    int charIdx = 0;
	    int byteIdx = 0;
	    if(odd) {
			data[0] = (byte) Character.digit(s.charAt(0), 16);
			charIdx = 1;
			byteIdx = 1;
	    }

	    while(byteIdx < byteLen) {
			data[byteIdx] = (byte) ((Character.digit(s.charAt(charIdx), 16) << 4)
			                       + Character.digit(s.charAt(charIdx + 1), 16));
			byteIdx += 1;
			charIdx += 2;
	    }
	    return data;
	}

	public static String repeat(String str, int count) {
		if(count < 0)
			throw new IllegalArgumentException("count must be positive");
		
		byte[] chars = str.getBytes();
		byte[] result = new byte[chars.length * count];
		int resultIdx = 0;
		for(int i = 0; i < count; i++, resultIdx += chars.length)
			System.arraycopy(chars, 0, result, resultIdx, chars.length);
		return new String(result);
	}
	
	public static String repeat(char c, int count) {
		if(count < 0)
			throw new IllegalArgumentException("count must be positive");
		
		char[] result = new char[count];
		Arrays.fill(result, c);
		return new String(result);
	}

	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc),
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}
	
	public static Iterable<Node> iterate(NodeList nl) {
		return new Iterable<Node>() {
			@Override
			public Iterator<Node> iterator() {
				return new Iterator<Node>() {
					int fIdxNext = 0;
					
					@Override
					public boolean hasNext() {
						return fIdxNext < nl.getLength();
					}

					@Override
					public Node next() {
						if(!hasNext())
							throw new NoSuchElementException();
						
						return nl.item(fIdxNext++);
					}
				};
			}
		};
	}
	
	public static Node getSingleChild(String name, Node parent) {
		Node result = null;
		for(Node node : iterate(parent.getChildNodes())) {
			if(!name.equals(parent.getNodeName()))
				continue;
			if(result != null)
				throw new RuntimeException("found second '" + name + "' node where only one was expected");
			result = node;
		}
		return result;
	}

	public static Long bytesToLong(byte[] data) {
		if(data.length > 8)
			throw new RuntimeException("array too long to convert to long");
		long result = 0;
		for(byte x : data)
			result = (result << 8) | (x & 0xff);
		return result;
	}
}
