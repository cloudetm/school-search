package gisdcl.schools;

import gisdcl.geoprocessors.JTSEngine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

public class SchoolDistrictGeoprocessorsResource extends ServerResource {
		
	private static final Logger log = Logger.getLogger(SchoolDistrictGeoprocessorsResource.class.getName());

	/**
	 * GET REST call to find School District for a Home Address
	 * At this moment only Addresses that is within Oakland county of Michigan state is covered.
	 * 
	 * @param entity
	 * @return
	 */
	@Get
	public Representation getSchoolDistrict(Representation entity){
		try {
			String homeAddress = getRequestAttributes().get("homeaddress").toString();
			//GeoCode Home Address
			String geoCodeResult = "";
			geoCodeResult = doGeoCoding();
			if (geoCodeResult.isEmpty()){
				return new StringRepresentation("Unable to GeoCode for the address : " + homeAddress + "; Stopping Process!!", MediaType.TEXT_PLAIN);
			}
			
			//Parse Google Geo Encoder result
			HashMap<String,String> parsedResult = parseResult(geoCodeResult);
			if (parsedResult.get("zip").isEmpty() || parsedResult.get("coordinates").isEmpty()){
				log.info("Parsed Result from geocoder result");
				log.info("Zip 			: " + parsedResult.get("zip"));
				log.info("coordinates 	: " + parsedResult.get("coordinates"));
				return new StringRepresentation("Gecoded address result do not contain Zip and/or Coordinate information! Stoping Process!", MediaType.TEXT_PLAIN);
			}
			
			//Get IDs of School Districts that touches ZIP code we are interested on
			String distIdsfromZip = ""; 
			//Check if we have ZIP code for the given address
			DistrictIdToZipDAO dao = new DistrictIdToZipDAO();
			distIdsfromZip = dao.getDistrictsByAZip(parsedResult.get("zip"));
			if (distIdsfromZip.isEmpty()){
				return new StringRepresentation("The system could not find School Districts for the given Zip code: " + parsedResult.get("zip") +"; Stoping Process!", MediaType.TEXT_PLAIN);
			}
			
			log.info("Start Find School District for the given home address");
			//Find School District for the given home address
			SchoolDistrictGeom schoolDistrict= this.districtByHomeAdressGeoProcessor(distIdsfromZip, parsedResult.get("coordinates"));
			if (schoolDistrict != null){
				String districtName = "School District is " + schoolDistrict.getName();
				log.info("School District is " + schoolDistrict.getName());
				return new StringRepresentation( districtName , MediaType.TEXT_PLAIN);	
			}else {
				return new StringRepresentation ("Failed to locate School District for address: "+ homeAddress, MediaType.TEXT_PLAIN);
			}
			
		}catch (IOException e){
			log.warning(" IOException : " + e.getMessage());
			return new StringRepresentation( " IOException  : " + e.getMessage() , MediaType.TEXT_PLAIN);
		} catch (ParserConfigurationException e) {
			log.warning(" ParserConfigurationException : " + e.getMessage());
			return new StringRepresentation( " ParserConfigurationException : " + e.getMessage(), MediaType.TEXT_PLAIN);
		} catch (SAXException e) {
			log.warning(" SAXException : " + e.getMessage());
			return new StringRepresentation( " SAXException : " + e.getMessage(), MediaType.TEXT_PLAIN);
		} catch (XPathExpressionException e) {
			log.warning(" XPathExpressionException : " + e.getMessage());
			return new StringRepresentation( " XPathExpressionException : " + e.getMessage(), MediaType.TEXT_PLAIN);
		} catch (Exception e){
			log.warning(" Exception : " + e.getMessage());
			return new StringRepresentation( " Exception : " + e.getMessage(), MediaType.TEXT_PLAIN);
		}
	}
	
	private SchoolDistrictGeom districtByHomeAdressGeoProcessor(String pDistrictIds, String pCoordinates) throws Exception{
		log.info("Starting districtByHomeAddressGeoProcessor with param pDistrictIds : " + pDistrictIds);
		SchoolDistrictGeom ret = null;
		ArrayList<SchoolDistrictGeom> districts = new ArrayList<SchoolDistrictGeom>();
		SchoolDistrictDAO dao = new SchoolDistrictDAO();
		//Check if District ID has content
		districts = dao.getSchoolDistrictGeomByIDs(pDistrictIds);
		log.info("districts " + districts.size());
		if(districts.size()>0){
			//Start testing Point in Polygon
			log.info("found districts size is " + districts.size());
			for (int i=0; i<districts.size(); i++){
				log.info("Before split , this.resultCoordinates value : " + pCoordinates );
				String[] xy = pCoordinates.split(",");
				log.info("resultCoordinates split array size " + xy.length);
				if (xy.length != 3){
					log.info("Format of 'coordinates' is not as expected. Can not continue with 'Address point in School District polygon' geoprocessor");
					throw new Exception("Format of 'coordinates' is not as expected. Can not continue with 'Address point in School District polygon' geoprocessor");
				}else{
					//Google Geocode watch!!! Coordinates first element at position 0 is Longitude, position 1 is Latitude
					double lat = Double.parseDouble(xy[1]);;
					log.info("lat : " + lat);
					double lon = Double.parseDouble(xy[0]);;
					log.info("lon : " + lon);
					log.info("districts.get(i).getGeom().getValue().length(): " + districts.get(i).getGeom().getValue().length());
					JTSEngine engine = new JTSEngine();
					boolean isAddressInPolygon = engine.isAddressInDistrict(lat, lon, districts.get(i).getGeom().getValue());
					log.info("i : " + i );
					if (isAddressInPolygon){
						ret = districts.get(i);
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * Parse Google Encoder result and extracts out
	 * Postal Code (ZIP) and
	 * Address coordinates as (lat,lon) format
	 * 
	 * TODO: Extract to Class?? 
	 * 
	 * @param geocodeResult
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private HashMap<String,String> parseResult(String geocodeResult) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		HashMap<String,String> ret = new HashMap<String,String>(2);
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = null;
		InputStream is = new ByteArrayInputStream(geocodeResult.getBytes("UTF-8"));
		doc = builder.parse(is);
		
		Element root = doc.getDocumentElement();
		log.info("Element root " + root.getTagName());
		
		NodeList postalCodeNodeList = root.getElementsByTagName("PostalCodeNumber");
		log.info("nodeList.getLength()  " +  postalCodeNodeList.getLength());
		log.info("nodeList.item(0).getNodeName() " +postalCodeNodeList.item(0).getNodeName());
		log.info("nodeList.item(0).getNodeType() " +postalCodeNodeList.item(0).getNodeType());
		log.info("nodeList.item(0).getTextContent() " +postalCodeNodeList.item(0).getTextContent());
		ret.put("zip", postalCodeNodeList.item(0).getTextContent());
		NodeList coordinatesNodeList = root.getElementsByTagName("coordinates");
		ret.put("coordinates", coordinatesNodeList.item(0).getTextContent());
		return ret;
	}
	
	/**
	 * Calls GeoCoding service from Google.
	 * TODO: Extract to Class
	 * @return
	 * @throws IOException
	 */
	private String doGeoCoding() throws IOException {
		String homeaddress = getRequestAttributes().get("homeaddress").toString();
		log.info("home address : " + homeaddress);
		log.info("home address encoded: " + URLEncoder.encode(homeaddress,"UTF-8"));
		
		String googleurl = "http://maps.google.com/maps/geo?";
		String q = "q=" + homeaddress; // Street Address of a House
		String key ="&key=" + "ABQIAAAATZqK2OxtQ68KSBUULc6czBT-wriDVgaQOyaSzYKjK9LMkL9lAhTFbcr_iMgtkXFnx6k0ayYLEYIxgg"; //Google Map Api key
		String sensor = "&sensor=false"; //
		String output = "&output=" + "xml"; // xml, kml, csv, json (default)
		String oe = "&oe=utf8"; // output encoding format
		String ll = ""; //The {latitude,longitude} of the viewport center expressed as a comma-separated string (e.g. "ll=40.479581,-117.773438" )
		String spn = ""; //The "span" of the viewport expressed as a comma-separated string of {latitude,longitude} (e.g. "spn=11.1873,22.5" ). 
		String gl = ""; //The country code, specified as a ccTLD ("top-level domain") t
		
		//build a string with returned stream from Google geoencoder
		StringBuffer sbf=null;
		BufferedReader reader=null;
		try {
			String gEncoderRequest = googleurl + q + output + sensor + key;

			log.info("Google Encoder request full url: " + gEncoderRequest);
			URL url = new URL(gEncoderRequest);
			sbf = new StringBuffer();
			// URL url = new URL("http://ww2.iparissa.com/shaeeta");
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;

			log.info("Reading lines from reader.readLine()");
			while ((line = reader.readLine()) != null) {
				log.warning(line.toString());
				sbf.append(line.toString());
			}
			reader.close();
			return sbf.toString();
		} catch (UnsupportedEncodingException e) {
			log.warning(e.getMessage());
			throw e;
		} catch (MalformedURLException e) {
			log.warning(e.getMessage());
			throw e;
		} catch (IOException e) {
			log.warning(e.getMessage());
			throw e;
		} 

	}

}
