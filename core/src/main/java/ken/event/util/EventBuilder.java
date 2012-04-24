package ken.event.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ken.event.Event;
import ken.event.meta.AtomicE;

/**
 * @author KennyZJ
 * 
 */
public class EventBuilder {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Event buildEvent(AtomicE raw_evt) throws UnknownHostException {
		String ipAddress = InetAddress.getLocalHost().getHostAddress();
		Event evt = new Event(raw_evt.getType(), System.currentTimeMillis(),
				ipAddress, "testsource", "Default Key", raw_evt, null);
		String id = genID(evt.getEvtType());
		evt.setEventID(id);// currently inject e_type to id
		return evt;
	}

	/**
	 * TODO refactor: event ID must be well-designed
	 * 
	 * @return
	 */
	private static String genID(String word) {
		return java.util.UUID.randomUUID().toString() + "-" + word;
	}

}
