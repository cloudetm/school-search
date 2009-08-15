package gisdcl.schools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class DistrictIdToZipResource extends ServerResource {
	private static final Logger log = Logger.getLogger(DistrictIdToZipResource.class.getName());
	
	@Get
	public Representation getMethod(Representation entity){
		String zipid = getRequestAttributes().get("zipid").toString().trim();
		log.info("zipcode supplied by client side: " + zipid);
		String retMsg ="";
		try {
			DistrictIdToZipDAO dao = new DistrictIdToZipDAO();
			retMsg = dao.getDistrictsByAZip(zipid);
		}catch (Exception e){
			log.info("Exception at Get trying to get District IDs given a zipid");
		}
		/* Sample Code 
		Set keys = getRequestAttributes().keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()){
			String obj =  (String) it.next();
			log.info("Key  : " + obj);
		}
		log.info("getRequestAttributes().get(\"zipid\").toString(): " + getRequestAttributes().get("zipid").toString());
		log.info("getRequestAttributes().get(\"countyid\").toString(): " + getRequestAttributes().get("countyid").toString());
		log.info("Integer.toString(getRequestAttributes().size()): " + Integer.toString(getRequestAttributes().size()));
		*/
		Representation rep = new StringRepresentation(retMsg);
		return rep;
	}
	@Post
	public Representation postDistToZip(Representation entity)   {
		Representation resp = null;
		try {
			log.warning("School District to Zip raw xml data SiZE: " + entity.getSize());
			String xmlData = entity.getText();
			log.warning("School District to Zip raw xml data : " + xmlData);
			XmlUtils xmlUtils = new XmlUtils();
			xmlUtils.set_xmlData(xmlData);
			ArrayList<DistrictIdToZip> distToZipList = xmlUtils.convertXmltoDistrictIdToZip();
			log.warning("returned geomobj:  " + distToZipList.size());
			DistrictIdToZipDAO dao = new DistrictIdToZipDAO();
			
			String saveStatus = dao.saveDisttoZipList(distToZipList);
			resp = new StringRepresentation("SchoolId to Zip List is saved",	MediaType.TEXT_PLAIN);
			resp.setIdentifier(getRequest().getResourceRef().getIdentifier());
			log.info("getRequest().getResourceRef().getIdentifier(): "+ getRequest().getResourceRef().getIdentifier());
			log.info("resp.getText(): "+ resp.getText());
			return resp;
		}catch(Exception e){
			log.warning("Exception at postDistTozip");
			log.warning(e.getMessage());
			resp = new StringRepresentation("FAILED!! Couldn't save SchoolId to Zip data");
			return resp;
		}
	}
}
