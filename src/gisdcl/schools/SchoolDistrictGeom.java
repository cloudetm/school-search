package gisdcl.schools;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text; //to hold large characters such as WKT

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class SchoolDistrictGeom {

		/*
		 * for more information go to
		 * http://code.google.com/appengine/docs/java/datastore/creatinggettinganddeletingdata.html
		 * how to create and use Key in Google DataStore
		 */
	
		@PrimaryKey
	    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	    private Key key;
	
	    public void setKey(Key key) {
	        this.key = key;
	    }
	    
	    @Persistent 
	    private String id;
	    
	    @Persistent
	    private String fips;
	    
	    @Persistent 
	    private String unified;
	    
		@Persistent
		private String name;
		
		@Persistent
		private Text geom;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFips() {
			return fips;
		}

		public void setFips(String fips) {
			this.fips = fips;
		}

		public String getUnified() {
			return unified;
		}

		public void setUnified(String unified) {
			this.unified = unified;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Text getGeom() {
			return geom;
		}

		public void setGeom(Text geom) {
			this.geom = geom;
		}

		public Key getKey() {
			return key;
		}

		public String toString(){
			return "key.name: " + this.key.getName() +"; name of sd: " + this.name;
		}

		
}
