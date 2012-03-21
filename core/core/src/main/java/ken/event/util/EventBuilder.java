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
		System.out.println("raw_evt.typeof = "+raw_evt.getType());
		Event evt = new Event(raw_evt.getType(), System.currentTimeMillis(),
				ipAddress, "testsource", "Default Key", raw_evt, null);
		return evt;
	}

}
