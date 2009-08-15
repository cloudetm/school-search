package gisdcl.schools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

public class XmlUtils {
	private static final Logger log = Logger.getLogger(XmlUtils.class.getName());

	private String _xmlData ="";
	
	public String get_xmlData() {
		return _xmlData;
	}

	public void set_xmlData(String data) {
		_xmlData = data;
	}

	
	public ArrayList<DistrictIdToZip> convertXmltoDistrictIdToZip() throws Exception{
		ArrayList<DistrictIdToZip> distToZipList = new ArrayList<DistrictIdToZip>(); //ArrayList to hold series of DistrictIdToZip object
		log.info("Starting Zip to XML object creation function");
		log.info("XML Raw data received from front end: "+ _xmlData);
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = null;

        try {
        	InputStream is = new ByteArrayInputStream(_xmlData.getBytes("UTF-8"));
            doc = builder.parse(is);
            //XPath Operation
            XPathFactory factory = new org.apache.xpath.jaxp.XPathFactoryImpl(); //Google fix 
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile("DISTTOZIP/SCHOOLTOZIP");
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList mapNodeList = (NodeList) result;
            log.info("length of result mapNodeList.getLength(): " + mapNodeList.getLength());
            
            for (int i = 0; i < mapNodeList.getLength(); i++) {
            	NamedNodeMap namedNodeMap = mapNodeList.item(i).getAttributes();
            	log.info("i == " + i);
            	log.info("namedNodeMap.getNamedItem(\"ZIP\").getNodeValue():  " + namedNodeMap.getNamedItem("ZIP").getNodeValue());	
            	log.info("namedNodeMap.getNamedItem(\"SCHOOLDISTID\").getNodeValue():  " + namedNodeMap.getNamedItem("SCHOOLDISTID").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"SCHOOLNAME\").getNodeValue():  " + namedNodeMap.getNamedItem("SCHOOLNAME").getNodeValue());
            	DistrictIdToZip distToZip = new DistrictIdToZip();
            	distToZip.setZip(namedNodeMap.getNamedItem("ZIP").getNodeValue());
            	distToZip.setSchoolid(namedNodeMap.getNamedItem("SCHOOLDISTID").getNodeValue());
            	distToZip.setSchoolname(namedNodeMap.getNamedItem("SCHOOLNAME").getNodeValue());
            	distToZipList.add(distToZip);
            }

            return distToZipList;
               
        }catch (Exception e){
        	log.warning("error while trying to convert XML encoded DistToZip to DistToZip Object!!!");
        	log.warning(e.getMessage());
        	throw e;
        }
		
		
	}

}
