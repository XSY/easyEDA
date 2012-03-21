package ken.event;

/**
 * @author KennyZJ
 * 
 */
public class Feeder implements java.io.Serializable {

	private static final long serialVersionUID = 598454900120908991L;

	private String id;
	private String name;
	private String vendor;

	public Feeder() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

}
