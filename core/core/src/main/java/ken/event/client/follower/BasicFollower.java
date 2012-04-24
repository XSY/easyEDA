package ken.event.client.follower;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.bus.SocketID;
import ken.event.client.EventBox;
import ken.event.client.QueuedEventBox;
import ken.event.client.adapter.IAdapter;
import ken.event.meta.AtomicE;
import ken.event.util.JDKSerializeUtil;
import ken.event.util.ZMQUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

public class BasicFollower extends Thread implements IFollower {
	public static Logger LOG = Logger.getLogger(BasicFollower.class);
	private static Map<String, Object> conf = EConfig.loadAll();

	private String _key;
	private String _dest;
	private EventBox box;
	private List<IAdapter> adapters;

	private ZMQ.Context context;
	private ZMQ.Socket socket;
	private SocketID sid;

	private String ask;
	private String report;

	

	protected BasicFollower(String key) throws IOException {
		super();
		// initialize, get the following by follower key automatically
		init(key);
	}

	private void init(String key) throws IOException {
		this._key = key;
		_dest = "tcp://" + conf.get(EConfig.EDA_ROUTER_OUTGOING_HOST) + ":"
				+ conf.get(EConfig.EDA_ROUTER_OUTGOING_PORT);

		ask = (String) conf.get(EConfig.EDA_ROUTER_HAND_ASK);
		report = (String) conf.get(EConfig.EDA_ROUTER_HAND_REPORT);
		box = new QueuedEventBox();// default set capacity to Integer.MAX_VALUE
		adapters = new ArrayList<IAdapter>();
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
		sid = new SocketID(_key, "1");
		socket.setIdentity(JDKSerializeUtil.getBytes(sid));
	}

	public void startFollowing() throws Exception {
		if (this.isAlive()) {
			LOG.warn("This thread is already started!");
		} else {
			// check whether there is at least one adapter attached to the
			// follower
			if (adapters == null || adapters.size() < 1) {
				throw new Exception("no adapter attached to me!");
			}
			prepareAdapters();
			socket.connect(_dest);
			// socket.send("READY".getBytes(), 0);
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
	 * allow duplicate adapters to be added
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
		// byte[] payload;
		// while (!Thread.currentThread().isInterrupted()) {
		// String client_addr = new String(socket.recv(0));
		// LOG.debug("client_addr:" + client_addr);
		// String empty = new String(socket.recv(0));
		// assert empty.length() == 0 | true;
		//
		// payload = socket.recv(0);
		// LOG.debug("payload.length = " + payload.length);
		// Event<AtomicE> e = (Event<AtomicE>) JDKSerializeUtil
		// .getObject(payload);
		// box.put(e.getEvtData());// payload, the real target
		//
		// socket.send(client_addr.getBytes(), ZMQ.SNDMORE);
		// socket.send(empty.getBytes(), ZMQ.SNDMORE);
		// socket.send((_key).getBytes(), 0);// _key as reply
		// }

		ZMQUtil.report(socket, report);
		Poller poller = context.poller(1);
		int count = 0;

		while (!Thread.currentThread().isInterrupted()) {

			poller.register(socket, Poller.POLLIN);
			poller.poll(3000 * 1000);
			if (poller.pollin(0)) {
				// received
				LOG.debug(Thread.currentThread().getName() + "[Worker]-[fkey:"
						+ _key + "|tid=" + 1 + "] is waiting to recv...");
				String client_addr = new String(socket.recv(0));
				if (ask.equals(client_addr)) {
					LOG.debug(Thread.currentThread().getName()
							+ "[Worker]-[fkey:" + _key + "|tid=" + 1
							+ "] I am asked! Going to report I am alive...");
					ZMQUtil.report(socket, report);
				} else if (socket.hasReceiveMore()) {
					String msgId = client_addr; // part1 is auto-matched with my
												// socketID, part2 is msg_id

					String empty = new String(socket.recv(0));
					assert empty.length() == 0 | true;

					// String request = new String(socket.recv(0));// payload,
					// the
					// real
					// target

					try {
						Event<AtomicE> e = (Event) JDKSerializeUtil
								.getObject(socket.recv(0));

						box.put(e.getEvtData());
						LOG.debug(Thread.currentThread().getName()
								+ "[Worker]-[fkey:" + _key + "|tid=" + 1
								+ "] recv ok");

						socket.send(msgId.getBytes(), 0);// send msgId for
															// representing
															// recv ok!
						LOG.debug(Thread.currentThread().getName()
								+ "[Worker]-[fkey:" + _key + "|tid=" + 1
								+ "] since follower started, received [" + count++
								+ "] events!");
					} catch (IOException e) {
						LOG.error(e);
					} catch (ClassNotFoundException e) {
						LOG.error(e);
					}
				}
				poller.unregister(socket);
			} else {
				// no response from router
				LOG.debug(Thread.currentThread().getName() + "[Worker]-[fkey:"
						+ _key + "|tid=" + 1
						+ "] no response from router server.");
				LOG.debug(Thread.currentThread().getName() + "[Worker]-[fkey:"
						+ _key + "|tid=" + 1 + "] reconnecting...");
				socket.setLinger(0);
				socket.close();
				poller.unregister(socket);
				socket = context.socket(ZMQ.REQ);
				try {
					socket.setIdentity(JDKSerializeUtil.getBytes(sid));
				} catch (IOException e) {
					e.printStackTrace();
				}
				socket.connect(_dest);
				ZMQUtil.report(socket, report);
			}
		}
		// we never get here if everything is ok.
		socket.close();
		context.term();
	}
}
