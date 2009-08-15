package gisdcl.administrativearea;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class CountyGeom {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;	
	
	@Persistent
	private String countyid;
	@Persistent
	private String name;
	@Persistent
	private String fips;
	@Persistent
	private String stateid;
	@Persistent
	private String statecode;
	@Persistent
	private Text geom;
	
	public Long getId() {
		return id;
	}

	public String getCountyid() {
		return countyid;
	}

	public void setCountyid(String countyid) {
		this.countyid = countyid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFips() {
		return fips;
	}

	public void setFips(String fips) {
		this.fips = fips;
	}

	public String getStateid() {
		return stateid;
	}

	public void setStateid(String stateid) {
		this.stateid = stateid;
	}

	public String getStatecode() {
		return statecode;
	}

	public void setStatecode(String statecode) {
		this.statecode = statecode;
	}

	public Text getGeom() {
		return geom;
	}

	public void setGeom(Text geom) {
		this.geom = geom;
	}
	
	public String toString(){
		return "countyid: " + this.countyid +"; name: " + this.name + "; fips: "+ this.fips;
	}

	
}
