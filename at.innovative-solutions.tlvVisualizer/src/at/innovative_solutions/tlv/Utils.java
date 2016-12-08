package at.innovative_solutions.tlv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class Utils {
	/**
	 * conversion function courtesy of maybeWeCouldStealAVan @ Stackoverflow
	 * http://stackoverflow.com/a/9855338
	 */
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
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

	public static String printChars(String chars, int count) {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < count; i++)
			b.append(chars);
		return b.toString();
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
}
