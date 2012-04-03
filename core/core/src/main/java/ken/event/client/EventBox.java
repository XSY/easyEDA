/**
 * 
 */
package ken.event.client;

import java.util.concurrent.LinkedBlockingQueue;

import ken.event.meta.AtomicE;

/**
 * @author KennyZJ
 * 
 */
public class EventBox extends LinkedBlockingQueue<AtomicE> {

	private static final long serialVersionUID = -1803921189366775678L;

	public EventBox() {
		super();
	}

	public EventBox(int capacity) {
		super(capacity);
	}

}
