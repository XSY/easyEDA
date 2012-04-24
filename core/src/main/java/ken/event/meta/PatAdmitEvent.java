package ken.event.meta;

/**
 * @author KennyZJ
 * 
 */
public class PatAdmitEvent extends AtomicE implements java.io.Serializable {

	private static final long serialVersionUID = 2154245454165165732L;
	
	private String pName;
	private String pCard;
	private String dept;
	private long admitTS;
	private String optID;

	public PatAdmitEvent() {
		super(EventConstants.PA_E_TYPE);
	}

	public PatAdmitEvent(String pName, String pCard, String dept, long admitTS,
			String optID) {
		super(EventConstants.PA_E_TYPE);
		this.pName = pName;
		this.pCard = pCard;
		this.dept = dept;
		this.admitTS = admitTS;
		this.optID = optID;
	}

	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}

	public String getpCard() {
		return pCard;
	}

	public void setpCard(String pCard) {
		this.pCard = pCard;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public long getAdmitTS() {
		return admitTS;
	}

	public void setAdmitTS(long admitTS) {
		this.admitTS = admitTS;
	}

	public String getOptID() {
		return optID;
	}

	public void setOptID(String optID) {
		this.optID = optID;
	}

}
