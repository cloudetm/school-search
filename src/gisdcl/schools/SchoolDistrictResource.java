package gisdcl.schools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.appengine.api.datastore.Text;

public class SchoolDistrictResource extends ServerResource {
	private static final Logger log = Logger.getLogger(SchoolDistrictResource.class.getName());

	@Get
	public Representation getMethod(Representation entity){
		String distId = getRequestAttributes().get("schooldistrictid").toString();
		log.info("schooldistrictid: " + distId);
		try {
			SchoolDistrictDAO dao = new SchoolDistrictDAO();
			SchoolDistrictGeom geom = dao.getSchoolDistrictGeomByID(distId);
			return new StringRepresentation(geom.getGeom().getValue());
		}catch(Exception e){
			log.warning("Exception at Get for School District Data");
			return new StringRepresentation("exception getting school district");
		}

	}
	
	@Post
	public Representation postSchoolDistrictGeometry(Representation entity){
		Representation rep = null;
		try {
			XmlUtils xmlutils = new XmlUtils();
			//Read XML data file and create a List of objects of type SchoolDistrictGeom
			ArrayList<SchoolDistrictGeom> schoolDistrictGeomList = this.createSchoolDistGeomFromFile();
			//Persist List to DataStore
			SchoolDistrictDAO dao = new SchoolDistrictDAO();
			/*
			 * Important!!!
			 * I've intentionally commented this line out to prevent accidental insertion of Geometry Data from XML file.
			 * If we need to insert new data later, then we will need to put a new 
			 * "mi_oakland_school_district.xml"
			 * with new DATA under /war/WEB-INF/classes folder and
			 * Un-comment the following line.
			 */
			dao.saveSchoolDistrictGeom(schoolDistrictGeomList);
			rep = new StringRepresentation("Successfully stored School District Geometry entities from xml file data", MediaType.TEXT_PLAIN);
			return rep;
		}catch(Exception e){
			rep = new StringRepresentation("There was a problem trying to convert Geometry XML file to DataStore",MediaType.TEXT_PLAIN);
			return rep;
		}
	}
	

	// TODO : Refactor method into a generic worker class using Interface and Reflection. Use Reflection to dynamically populate Domain (Value Objects) Objects
	/**
	 * Converts School Spatial from XML to Custom Object. This custom object is a JDO persistent enabled object.
	 * @throws Exception 
	 */
	public ArrayList<SchoolDistrictGeom> createSchoolDistGeomFromFile() throws Exception{
		ArrayList<SchoolDistrictGeom> schoolDistrictGeomList = new ArrayList<SchoolDistrictGeom>();
		try{
			log.warning("createSchoolDistGeomFromFile");
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	        domFactory.setNamespaceAware(false); // never forget this!
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document doc = null;
	        log.warning("after doc");
    
	        InputStream is = getClass().getClassLoader().getResourceAsStream("mi_oakland_school_district.xml");
    		log.warning("after inputstream is ");
    		doc = builder.parse(is);
            
    		//XPath Operation
            XPathFactory factory = new org.apache.xpath.jaxp.XPathFactoryImpl(); //Google fix 
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile("SCHOOLDISTRICTS/SCHOOLDISTRICT");
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList mapNodeList = (NodeList) result;
            log.info("length of result mapNodeList.getLength(): " + mapNodeList.getLength());
            
            for (int i = 0; i < mapNodeList.getLength(); i++) {
            	NamedNodeMap namedNodeMap = mapNodeList.item(i).getAttributes();
            	log.info("i == " + i);
            	log.info("namedNodeMap.getNamedItem(\"ID\").getNodeValue():  " + namedNodeMap.getNamedItem("ID").getNodeValue());	
            	log.info("namedNodeMap.getNamedItem(\"FIPS\").getNodeValue():  " + namedNodeMap.getNamedItem("FIPS").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"UNIFIED\").getNodeValue():  " + namedNodeMap.getNamedItem("UNIFIED").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"NAME\").getNodeValue():  " + namedNodeMap.getNamedItem("NAME").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"GEOM\").getNodeValue():  " + namedNodeMap.getNamedItem("GEOM").getNodeValue());
            	
            	SchoolDistrictGeom schoolDistrictGeom  = new SchoolDistrictGeom();
            	schoolDistrictGeom.setId(namedNodeMap.getNamedItem("ID").getNodeValue().trim());
            	schoolDistrictGeom.setFips(namedNodeMap.getNamedItem("FIPS").getNodeValue().trim());
            	schoolDistrictGeom.setUnified(namedNodeMap.getNamedItem("UNIFIED").getNodeValue().trim());
            	schoolDistrictGeom.setName(namedNodeMap.getNamedItem("NAME").getNodeValue().trim());
            	schoolDistrictGeom.setGeom(new Text(namedNodeMap.getNamedItem("GEOM").getNodeValue().trim()));
            	
            	schoolDistrictGeomList.add(schoolDistrictGeom);
            }
           	return schoolDistrictGeomList;
        }catch(Exception e){
        	log.warning("Exception" + e.getMessage());
        	throw e;
        }	
	}
	
}
