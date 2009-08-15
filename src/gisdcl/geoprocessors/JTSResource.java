package gisdcl.geoprocessors;


import java.util.logging.Logger;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.vividsolutions.jts.io.ParseException;

public class JTSResource extends ServerResource {
	private static final Logger log = Logger.getLogger(JTSResource.class.getName());
	
	
	@Get
	public String testJTS(){
		String operationName = (String) this.getRequest().getAttributes().get("operation");
		
		JTSEngine myJts = new JTSEngine();
		
		if (operationName.equals("polygonarea")){
			try {
				return myJts.testPointInPolygon();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.warning("exception thrown at polygonarea op");
				return "error Polygon op";
			}
		}else if (operationName.equals("linelength")){
			try {
				return myJts.testJTS();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.warning("ParseException thrown : "+ e.getMessage());
				return "error JTS";
			}	
		}else{
			return "can't find op name";
		}
	}
}
