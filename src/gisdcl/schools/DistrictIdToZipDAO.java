package gisdcl.schools;

import gisdcl.PMF;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * TODO: Make this DAO as Static or Singleton after further reading.
 * 
 * @author iyusuf
 *
 */
public class DistrictIdToZipDAO {
	
	private static final Logger log = Logger.getLogger(DistrictIdToZipDAO.class.getName());
	
	public String getDistrictsByAZip(String pZipId){
		log.info(" starting getDistrictsByAZip method, working with zip--> " + pZipId);
		StringBuffer districtIds = new StringBuffer();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(DistrictIdToZip.class);
	    query.setFilter("zip == pZipId");
	    //query.setOrdering("hireDate desc");
	    query.declareParameters("String pZipId");

	    try {
	        List<DistrictIdToZip> results = (List<DistrictIdToZip>) query.execute(pZipId);
	        if (results.iterator().hasNext()) {
	            for (DistrictIdToZip e : results) {
	            	log.info("..................");
	                log.info("e.getSchoolid()   : " + e.getSchoolid());
	                log.info("e.getSchoolname() : " + e.getSchoolname());
	                log.info("e.getZip()		: " + e.getZip());
	                districtIds.append(e.getSchoolid() + " , ");
	            }
	            return districtIds.toString();
	        } else {
	            log.info("No Result found for the given zipid == " + pZipId);
	            return ("No Result found for the given zipid == " + pZipId);
	        }
	    } catch (Exception e){
	    	log.info("Exception at DAO DistrictIdToZip");
	    	return "Exception at DAO DistrictIdToZip";
	    }finally {
	        query.closeAll();
	        pm.close();
	    }
	}
	
	/**
	 * Saves DistrictIdToZip to BigTable
	 * 
	 * @param distToZipList
	 * @return a descriptive text back to browser
	 * @throws Exception
	 */
	public String saveDisttoZipList(ArrayList<DistrictIdToZip> distToZipList) throws Exception{
		log.info("Starting persistence operation on DistrictIdToZip");
		log.info("Size of Collection: " + distToZipList.size());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
        	//Save All DistrictToZip java object in List at once
           	pm.makePersistentAll(distToZipList);
            log.info("Transaction committed");
            return "transaction committed for distToZipList";
        } catch(Exception e){
        	log.warning("Error saving DistrictIdToZip collection");
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
