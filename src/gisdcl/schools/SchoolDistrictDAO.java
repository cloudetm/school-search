package gisdcl.schools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import gisdcl.PMF;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class SchoolDistrictDAO {
	
	private static final Logger log = Logger.getLogger(SchoolDistrictDAO.class.getName());
	
	
	public String saveSchoolDistrictGeom(ArrayList<SchoolDistrictGeom> schoolDistrictGeomList) throws Exception{
		log.info("Starting persistence operation on DistrictIdToZip");
		log.info("Size of Collection: " + schoolDistrictGeomList.size());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
        	//Save All DistrictToZip java object in List at once
           	pm.makePersistentAll(schoolDistrictGeomList);
            log.info("Transaction committed");
            return "transaction committed for schoolDistrictGeomList";
        } catch(Exception e){
        	log.warning("Error saving schoolDistrictGeomList collection");
        	log.warning(e.getMessage());
        	throw e;
        }finally {
            //Close Persistence Manager if it is not closed
            if(!pm.isClosed()){ 
            	pm.close();
            }
        }
	}


	public SchoolDistrictGeom getSchoolDistrictGeomByID(String pDistrictID) throws Exception{
		SchoolDistrictGeom ret = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(SchoolDistrictGeom.class);
		/**
		 * TODO:	We need to create a composite ID for SchoolDistrictGeom BigTable.
		 * 			The ID we have right now for SchoolDistrictGeom table is unique only for Oakland county.
		 * 			We need to come up with a ID that will be UNIQUE to all over the USA.
		 * 			Probable solution ID = FIPS + ID 
		 */
	    query.setFilter("id == pDistrictID");
	    //query.setOrdering("hireDate desc");
	    query.declareParameters("String pDistrictID");

	    try {
	        List<SchoolDistrictGeom> results = (List<SchoolDistrictGeom>) query.execute(pDistrictID.trim());
	        if (results.iterator().hasNext()) {
	            for (SchoolDistrictGeom e : results) {
	            	log.info("..................");
	                log.info("e.getId()   : " + e.getId());
	                log.info("e.getFips() : " + e.getFips());
	                log.info("e.getName()		: " + e.getName());
	                log.info("e.getUnified()		: " + e.getUnified());
	                log.info("Length of TEXT		: " + e.getGeom().getValue().length());
	                log.info("e.getGeometry().toString()		: " + e.getGeom().toString());
	                
	                //set to ret
	                ret = e;
	            }
	            return ret;
	        } else {
	            log.info("No School District Geometry found for the given School District ID' == " + pDistrictID.toString());
	            return ret;
	        }
	    } catch (Exception e){
	    	log.info("Exception at ");
	    	throw e;
	    }finally {
	        query.closeAll();
	        pm.close();
	    }
		
	}
	/**
	 * DAO method to read School District Geometry objects from BigTable.
	 * TODO: Promote to CLASS
	 * 
	 * @param pDistrictIDs
	 * @return
	 * @throws Exception
	 */
	public ArrayList<SchoolDistrictGeom> getSchoolDistrictGeomByIDs(String pDistrictIDs) throws Exception{
		log.info("starting getSchoolDistrictGeomByIDs"  );
		
		ArrayList<SchoolDistrictGeom> schoolDistricts = new ArrayList<SchoolDistrictGeom>();
		String[] districtIDs = pDistrictIDs.split(",");
		log.info("Starting loop");
		for (int i=0; i<districtIDs.length - 1; i++){
			log.info("i= "+  i +" : value is : " + districtIDs[i]);
			SchoolDistrictGeom geom = this.getSchoolDistrictGeomByID(districtIDs[i].trim());
			log.info("geom.getName(): " + geom.getName());
			schoolDistricts.add(geom);
			
		}
		return schoolDistricts;
	}
}
