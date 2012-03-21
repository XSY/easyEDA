/**
 * 
 */
package ken.event;

/**
 * @author KennyZJ
 * 
 */
public class Follower implements java.io.Serializable {

	private static final long serialVersionUID = -6954474669938217020L;

	private String id;
	private String name;
	private String vendor;

	public Follower() {
	}

	public Follower(String id, String name, String vendor) {
		super();
		this.id = id;
		this.name = name;
		this.vendor = vendor;
	}

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
