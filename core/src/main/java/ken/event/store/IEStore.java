package ken.event.store;

import ken.event.Event;

/**
 * @author KennyZJ
 *
 */
public interface IEStore {

	public void storeEvent(Event evt);
}
