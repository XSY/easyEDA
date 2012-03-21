package ken.event.client.follower;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.bus.SocketID;
import ken.event.client.adapter.IAdapter;
import ken.event.meta.AtomicE;
import ken.event.util.JDKSerializeUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

/**
 * AdvancedFollower usually works together with customer [thing]s like storm-way
 * 
 * @author KennyZJ
 * 
 */
public class AdvancedFollower extends Thread implements IFollower {

	public static Logger LOG = Logger.getLogger(AdvancedFollower.class);

	private String _fo_id;
	private String _key;
	private LinkedBlockingQueue<Event> _queue;
	private String _dest;

	ZMQ.Context _context;
	ZMQ.Socket _socket;

	public AdvancedFollower(String fid, String key,
			LinkedBlockingQueue<Event> queue) throws IOException {
		super();
		_fo_id = fid;
		_key = key;// follower name(key)
		_queue = queue;
		init();
	}

	private void init() throws IOException {

		Map<String, Object> conf = EConfig.loadAll();
		_dest = "tcp://" + conf.get(EConfig.EDA_ROUTER_OUTGOING_HOST) + ":"
				+ conf.get(EConfig.EDA_ROUTER_OUTGOING_PORT);

		_context = ZMQ.context(1);
		_socket = _context.socket(ZMQ.REQ);
		SocketID sock_id = new SocketID(_key, _fo_id);
		_socket.setIdentity(JDKSerializeUtil.getBytes(sock_id));
	}

	public void startFollowing() throws Exception {
		if (this.isAlive()) {
			LOG.warn("This thread is already working!");
		} else {
			_socket.connect(_dest);
			_socket.send("READY".getBytes(), 0);
			Thread.sleep(100);
			super.start();
		}
	}

	@Override
	public void stopFollowing() {
		_socket.close();
		_context.term();
		super.interrupt();
	}

	@Override
	public void setAdapter(IAdapter a) {
		// do nothing
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
			Thread.currentThread().interrupt();
		}
	}

	@SuppressWarnings("rawtypes")
	private void follow() throws IOException, ClassNotFoundException,
			InterruptedException {
		byte[] payload;
		while (!Thread.currentThread().isInterrupted()) {
			String client_addr = new String(_socket.recv(0));
			LOG.debug("client_addr:" + client_addr);
			String empty = new String(_socket.recv(0));
			assert empty.length() == 0 | true;

			payload = _socket.recv(0);
			LOG.debug("payload.length = " + payload.length);
			Event e = (Event) JDKSerializeUtil.getObject(payload);
			_queue.put(e);// payload,
			// the real
			// target

			_socket.send(client_addr.getBytes(), ZMQ.SNDMORE);
			_socket.send(empty.getBytes(), ZMQ.SNDMORE);
			_socket.send((_key).getBytes(), 0);// _key as reply
		}
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

}
