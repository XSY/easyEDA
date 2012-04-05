package ken.event.client.adapter;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import ken.event.client.EventBox;
import ken.event.client.feeder.BasicFeeder;
import ken.event.client.follower.BasicFollower;
import ken.event.client.follower.RichFollower;

import org.apache.log4j.Logger;

/**
 * Adapter takes event data into/out of the third-party
 * 
 * @author KennyZJ
 * 
 */
public class Adapter extends Thread implements IAdapter {

	public static Logger LOG = Logger.getLogger(Adapter.class);

	EventBox _box;
	private boolean _isSingle = true; // default handle only one kind of event

	public Adapter() {
		super();// must super() since extending class Thread
		this.setDaemon(true);
	}

	public Adapter(boolean isSingle) {
		super();
		this._isSingle = isSingle;
	}

	public Adapter(Map<String, Object> conf) {
		super();
	}

	public void startAdapter() {
		super.start();
	}

	@Override
	public void run() {
		try {
			doAdapt();
		} catch (InterruptedException e) {
			LOG.error(e.getMessage());
		}
	}

	public void doAdapt() throws InterruptedException {
	}

	public void setFollowStream(EventBox box) {

		// check whether the caller is BasicFollower of RichFollower, only these
		// two are allowed
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		String callerName = stack[1].getClassName();// stack[0] is Adaptor
													// itself

		LOG.debug("caller is:" + callerName);
		if (callerName.equals(BasicFollower.class.getName())
				|| callerName.equals(RichFollower.class.getName())) {
			this._box = box;
		} else {
			LOG.warn("you are not allowed to invoke this method!");
		}
	}

	@Override
	public void setFeedStream(EventBox box) {
		// check whether the caller is BasicFollower of RichFollower, only these
		// two are allowed
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		String callerName = stack[1].getClassName();// stack[0] is Adaptor
													// itself

		LOG.debug("caller is:" + callerName);
		if (callerName.equals(BasicFeeder.class.getName())) {
			this._box = box;
		} else {
			LOG.warn("you are not allowed to invoke this method!");
		}

	}

}
