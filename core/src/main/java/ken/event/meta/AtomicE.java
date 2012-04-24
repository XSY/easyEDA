package ken.event.meta;

/**
 * @author KennyZJ
 * 
 */
public class AtomicE implements java.io.Serializable{

	private static final long serialVersionUID = 6079481280942205371L;
	
	private final String _typeof;

	public AtomicE(String typeof) {
		_typeof = typeof;
	}
	
	public String getType(){
		return _typeof;
	}
}
