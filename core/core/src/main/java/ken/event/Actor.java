/**
 * 
 */
package ken.event;

/**
 * @author KennyZJ
 * 
 */
public class Actor implements java.io.Serializable{
	
	private String actorName;

	/**
	 * look up the actor_base to check if there is an valid actor by the given actorName,
	 * or the actor do have the privilege to produce or consume the specific event
	 * if not, throw exception
	 */
	public Actor(String actorName, String evtType) throws Exception {
		
		boolean permit = true;
		
		//permit = lookUpActor(actorName, evtType);
		
		
		if (permit) {
			// TODO new instance here
			this.actorName = actorName;
		} else {
			throw new Exception("Actor [" + actorName + "] cannot be constructed!");
		}
	}

}
