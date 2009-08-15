import gisdcl.administrativearea.CountyResource;
import gisdcl.geoprocessors.GeoprocessorsTesterResource;
import gisdcl.schools.DistrictIdToZipResource;
import gisdcl.schools.SchoolDistrictResource;
import gisdcl.schools.SchoolDistrictGeoprocessorsResource;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

//
public class RestletMain extends Application {
	
		@Override
		public synchronized Restlet createRoot() {
			Router router = new Router(getContext());
			
			//Post (populate BigTable) School District Geometry data. 
			router.attach("/school/district/gisdata/mi/oakland/", SchoolDistrictResource.class); //Supply input data using file
			
			//Post District ID to Zip Mapping data
			router.attach("/school/district/zip/", DistrictIdToZipResource.class); //supply data using POST body
			//Gets School District IDs as a comma separated String for a given ZIP Code
			router.attach("/school/district/{zipid}", DistrictIdToZipResource.class);
			//GeoProcessor Resources: Find School District by home address
			router.attach("/school/district/mi/oakland/{homeaddress}", SchoolDistrictGeoprocessorsResource.class);
			
			//JTSEngine Point in Polygon Tester
			//Hardcoded Polygon and Point
			router.attach("/geoprocessors/tester/", GeoprocessorsTesterResource.class);
			
			//Counties
			//Get County Geometry by countyfips number
			router.attach("/county/gisdata/{countyfips}",CountyResource.class);
			//Post County Geometry data
			router.attach("/county/gisdata/",CountyResource.class); //supply data using file
			
			return router;
		}
}
