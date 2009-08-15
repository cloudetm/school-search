package gisdcl.administrativearea;

import gisdcl.PMF;
import gisdcl.schools.SchoolDistrictDAO;
import gisdcl.schools.SchoolDistrictResource;
import gisdcl.schools.SchoolDistrictGeom;
import gisdcl.schools.XmlUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

import com.google.appengine.api.datastore.Text;

public class CountyResource extends ServerResource {
	private static final Logger log = Logger.getLogger(CountyResource.class.getName());
	
	@Get
	public Representation getCountyByFips(Representation entity){
		return new StringRepresentation("GET function not yet implemented for County", MediaType.TEXT_PLAIN);	
	}
	
	@Post
	public Representation postCountyGeometry(Representation entity){
		Representation rep = null;
		try {
			ArrayList<CountyGeom> countyGeomList = this.createCountyGeomFromFile();
			/*
			 * Important!!!
			 * I've intentionally commented this line out to prevent accidental insertion of Geometry Data from XML file.
			 * If we need to insert new data later, then we will need to put a new 
			 * "us_counties.xml"
			 * with new DATA under /war/WEB-INF/classes folder and
			 * Un-comment the following line.
			 */
			this.saveCountyGeom(countyGeomList);
			rep = new StringRepresentation("Successfully stored County Geometry entities from xml file data", MediaType.TEXT_PLAIN);
			return rep;
		}catch(Exception e){
			rep = new StringRepresentation("There was a problem trying to convert County Geometry XML file to DataStore",MediaType.TEXT_PLAIN);
			return rep;
		}
	}
	
	// TODO : Refactor method into a generic worker class using Interface and Reflection. Use Reflection to dynamically populate Domain (Value Objects) Objects
	/**
	 * Converts School Spatial from XML to Custom Object. This custom object is a JDO persistent enabled object.
	 * @throws Exception 
	 */
	public ArrayList<CountyGeom> createCountyGeomFromFile() throws Exception{
		ArrayList<CountyGeom> countyGeomList = new ArrayList<CountyGeom>();
		try{
			log.info("createCountyGeomFromFile");
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	        domFactory.setNamespaceAware(false); // never forget this!
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document doc = null;
	        log.info("after doc");
    
	        InputStream is = getClass().getClassLoader().getResourceAsStream("us_counties.xml");
    		log.info("after inputstream is ");
    		doc = builder.parse(is);
            
    		//XPath Operation
            XPathFactory factory = new org.apache.xpath.jaxp.XPathFactoryImpl(); //Google fix 
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile("USCOUNTIES/COUNTY");
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList mapNodeList = (NodeList) result;
            log.info("length of result mapNodeList.getLength(): " + mapNodeList.getLength());
            
            for (int i = 0; i < mapNodeList.getLength(); i++) {
            	NamedNodeMap namedNodeMap = mapNodeList.item(i).getAttributes();
            	log.info("i == " + i);
            	log.info("namedNodeMap.getNamedItem(\"COUNTYID\").getNodeValue():  " + namedNodeMap.getNamedItem("COUNTYID").getNodeValue());	
            	log.info("namedNodeMap.getNamedItem(\"NAME\").getNodeValue():  " + namedNodeMap.getNamedItem("NAME").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"FIPS\").getNodeValue():  " + namedNodeMap.getNamedItem("FIPS").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"STATEID\").getNodeValue():  " + namedNodeMap.getNamedItem("STATEID").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"STATECODE\").getNodeValue():  " + namedNodeMap.getNamedItem("STATECODE").getNodeValue());
            	log.info("namedNodeMap.getNamedItem(\"GEOM\").getNodeValue():  " + namedNodeMap.getNamedItem("GEOM").getNodeValue());
            	
            	CountyGeom countyGeom  = new CountyGeom();
            	countyGeom.setCountyid(namedNodeMap.getNamedItem("COUNTYID").getNodeValue().trim());
            	countyGeom.setName(namedNodeMap.getNamedItem("NAME").getNodeValue().trim());
            	countyGeom.setFips(namedNodeMap.getNamedItem("FIPS").getNodeValue().trim());
            	countyGeom.setStateid(namedNodeMap.getNamedItem("STATEID").getNodeValue().trim());
            	countyGeom.setStatecode(namedNodeMap.getNamedItem("STATECODE").getNodeValue().trim());
            	countyGeom.setGeom(new Text(namedNodeMap.getNamedItem("GEOM").getNodeValue().trim()));
            	
            	countyGeomList.add(countyGeom);
            }
           	return countyGeomList;
        }catch(Exception e){
        	log.warning("Exception" + e.getMessage());
        	throw e;
        }	
	}
	
	/**
	 * Save County data to BigTable
	 * 
	 * @param schoolDistrictGeomList
	 * @return
	 * @throws Exception
	 */
	public void saveCountyGeom(ArrayList<CountyGeom> countyGeomList) throws Exception{
		log.info("Starting persistence operation on countyGeom");
		log.info("Size of County Collection: " + countyGeomList.size());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
        	//Save All DistrictToZip java object in List at once
           	pm.makePersistentAll(countyGeomList);
            
        } catch(Exception e){
        	log.warning("Error saving countyGeom collection");
        	log.warning(e.getMessage());
        	throw e;
        }finally {
            //Close Persistence Manager if it is not closed
            if(!pm.isClosed()){ 
            	pm.close();
            }
        }
	}

}
