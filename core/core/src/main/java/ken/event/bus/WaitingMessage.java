package ken.event.bus;

/**
 * @author KennyZJ
 * 
 */
public class WaitingMessage extends Message {

	/**
	 * 10 second to wait
	 */
	public final static long DEFAULT_TIMEOUT = 10000;

	public final static long NEVER_TIMEOUT = 0;

	private long timeout;

	private long begin;

	public WaitingMessage() {
		this.begin = System.currentTimeMillis(); // only millisecond precise
		this.timeout = DEFAULT_TIMEOUT;
	}

	public WaitingMessage(Message msg) {
		this();
		if (msg != null) {
			this.setId(msg.getId());
			this.setPart1(msg.getPart1());
			this.setPart2(msg.getPart2());
			this.setPart3(msg.getPart3());
		}
	}
	public WaitingMessage(long timeout) {
		this();
		if (timeout >= 0) {
			this.timeout = timeout;
		}
	}

	/**
	 * construct a new waiting message
	 * 
	 * @param msg
	 *            copy the common message to build a waiting message
	 * @param timeout
	 *            measured in millisecond, if < 0 set the Default timeout
	 *            instead, if 0 never expired
	 */
	public WaitingMessage(Message msg, long timeout) {
		this(msg);
		if (timeout >= 0) {
			this.timeout = timeout;
		}
	}

	public long getTimeout() {
		return timeout;
	}

	public long getBegin() {
		return begin;
	}

	public boolean isExpired() {
		if (this.timeout == 0) {
			return false;
		}
		return (System.currentTimeMillis() - (begin + timeout)) >= 0;
	}

}
