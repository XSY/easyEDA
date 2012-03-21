package ken.event.client.follower;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.client.EventBox;
import ken.event.client.adapter.IAdapter;
import ken.event.meta.AtomicE;
import ken.event.util.JDKSerializeUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

/**
 * BasicFollower is to follow one or more kinds of event with only one receive
 * thread running each different event type, but the received event data still
 * can be consumed by multiple adapters
 * 
 * @author KennyZJ
 * 
 */
public class BasicFollower extends Thread implements IFollower {

	public static Logger LOG = Logger.getLogger(BasicFollower.class);

	private String _key;
	private String _dest;
	private EventBox box;
	private List<String> following; // event types which this follower is
									// supposed to follow
	private List<IAdapter> adapters;

	private ZMQ.Context context;
	private ZMQ.Socket socket;

	protected BasicFollower(String key) {
		super();
		// initialize, get the following by follower key automatically
		init(key);
	}

	private void init(String key) {
		this._key = key;
		Map<String, Object> conf = EConfig.loadAll();
		_dest = "tcp://" + conf.get(EConfig.EDA_ROUTER_OUTGOING_HOST) + ":"
				+ conf.get(EConfig.EDA_ROUTER_OUTGOING_PORT);
		box = new EventBox();// default set capacity to Integer.MAX_VALUE
		adapters = new ArrayList<IAdapter>();
		// following = ConfigUtil.lookupFollowerMapping(_key);
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
		socket.setIdentity(_key.getBytes());
	}

	public void startFollowing() throws Exception {
		if (this.isAlive()) {
			LOG.warn("This thread is already working!");
		} else {
			// check whether there is at least one adapter attached to the
			// follower
			if (adapters == null || adapters.size() < 1) {
				throw new Exception("no adapter attached to me!");
			}
			prepareAdapters();
			socket.connect(_dest);
			socket.send("READY".getBytes(), 0);
			Thread.sleep(100);
			super.start();
		}
	}

	private void prepareAdapters() {
		for (IAdapter adapter : adapters) {
			adapter.setFollowStream(box);
			adapter.startAdapter();
		}
	}

	public void stopFollowing() {
		socket.close();
		context.term();
		super.interrupt();
	}

	@Deprecated
	@Override
	/**
	 * not suggested to use this method, use startDining() instead
	 */
	public synchronized void start() {
		LOG.warn("Not supported any more, this method takes no job to do. Please use startFollowing() instead!");
	}

	@Deprecated
	@Override
	/**
	 * not suggested to use this method, use stopDining() instead
	 */
	public void interrupt() {
		LOG.warn("Not supported any more, this method takes no job to do. Please use stopFollowing() instead!");
	}

	/**
	 * allow duplicate adapter to be added
	 */
	public void setAdapter(IAdapter a) {
		adapters.add(a);
	}

	@Override
	public void run() {
		try {
			follow();
		} catch (IOException ioe) {
			LOG.error("ioexception: " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			LOG.error("classnotfoundexception:" + cnfe.getMessage());
		} catch (InterruptedException ire) {
			LOG.error("interruptedexception:" + ire.getMessage());
		}
	}

	private void follow() throws IOException, ClassNotFoundException,
			InterruptedException {
		byte[] payload;
		while (!Thread.currentThread().isInterrupted()) {
			String client_addr = new String(socket.recv(0));
			LOG.debug("client_addr:" + client_addr);
			String empty = new String(socket.recv(0));
			assert empty.length() == 0 | true;

			payload = socket.recv(0);
			LOG.debug("payload.length = " + payload.length);
			Event<AtomicE> e = (Event<AtomicE>) JDKSerializeUtil
					.getObject(payload);
			box.put(e.getEvtData());// payload, the real target

			socket.send(client_addr.getBytes(), ZMQ.SNDMORE);
			socket.send(empty.getBytes(), ZMQ.SNDMORE);
			socket.send((_key).getBytes(), 0);//_key as reply
		}
	}

}
