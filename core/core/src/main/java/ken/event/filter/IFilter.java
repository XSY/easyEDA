package ken.event.filter;

import ken.event.Event;

/**
 * Generally this is for follower to filter events it received, the events left
 * will be thrown into EventBox.
 * 
 * @author KennyZJ
 * 
 */
public interface IFilter {

	/**
	 * Check if the event is acceptable
	 * @param e
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean accept(Event e);
}
