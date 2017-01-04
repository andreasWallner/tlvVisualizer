package at.innovative_solutions.tlvVisualizer.decoder.emv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import at.innovative_solutions.tlvVisualizer.decoder.emv.TagInfo;

public class TagInfoTest {
	@Test
	public void test_createFromNode_standard() throws Exception {
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(XML_oneNode));
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);

		TagInfo result = TagInfo.createFromNode(doc.getChildNodes().item(0));
		TagInfo expected = new TagInfo(0x9F01,
				                       "Acquirer Identifier",
				                       new int[0],
				                       true,
				                       "n 6-11",
				                       "Uniquely identifies the acquirer within each payment system",
				                       "6",
				                       "Terminal",
				                       null);
		assertTrue("tag", expected.equals(result));
	}
	
	@Test
	public void test_loadXML_singleTag() throws Exception {
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(XML_singleTag));
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		
		HashMap<Long, TagInfo> result = TagInfo.loadXML(doc);
		
		assertEquals("tag count", 1, result.size());
		assertTrue("tag key", result.containsKey(0x9F01L));
		TagInfo tag = result.get(0x9F01L);
		assertEquals("tag id", 0x9F01L, tag.fId);
		assertEquals("tag name", "Acquirer Identifier", tag.fName);
		assertArrayEquals("tag templates", new int[0], tag.fTemplates);
		assertEquals("tag primitive", true, tag.fIsPrimitive);
		assertEquals("tag format", "n 6-11", tag.fFormat);
		assertEquals("tag description", "Uniquely identifies the acquirer within each payment system", tag.fDescription);
		assertEquals("tag length", "6", tag.fLength);
		assertEquals("tag source", "Terminal", tag.fSource);
	}
	
	// TODO test with templates
	String XML_oneNode = "<tag>"
					   + "  <id>9F01</id>"
					   + "  <name>Acquirer Identifier</name>"
					   + "  <templates/>"
					   + "  <primitive>True</primitive>"
					   + "  <format>n 6-11</format>"
					   + "  <description>Uniquely identifies the acquirer within each payment system</description>"
					   + "  <length>6</length>"
					   + "  <source>Terminal</source>"
					   + "</tag>";

	String XML_singleTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                         + "<tags version=\"1\">"
                         + "  <tag>"
						 + "    <id>9F01</id>"
						 + "    <name>Acquirer Identifier</name>"
						 + "    <templates/>"
						 + "    <primitive>True</primitive>"
						 + "    <format>n 6-11</format>"
						 + "    <description>Uniquely identifies the acquirer within each payment system</description>"
						 + "    <length>6</length>"
						 + "    <source>Terminal</source>"
						 + "  </tag>"
						 + "</tags>";
}
