package ken.event.client.feeder;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.meta.AtomicE;
import ken.event.util.EventBuilder;
import ken.event.util.JDKSerializeUtil;

import ken.event.client.EventBox;
import ken.event.client.QueuedEventBox;
import ken.event.client.adapter.IAdapter;

/**
 * @author KennyZJ
 * 
 */
public class BasicFeeder extends Thread implements IFeeder {
	public static Logger LOG = Logger.getLogger(BasicFeeder.class);

	private String _name;
	private String _dest;
	private EventBox box;
	private List<IAdapter> adapters;

	private ZMQ.Context context;
	private ZMQ.Socket socket;

	protected BasicFeeder(String name) {
		super();
		init(name);
	}

	private void init(String name) {
		_name = name;
		Map<String, Object> conf = EConfig.loadAll();
		_dest = "tcp://" + conf.get(EConfig.EDA_PIVOT_INCOMING_HOST) + ":"
				+ conf.get(EConfig.EDA_PIVOT_INCOMING_PORT);

		box = new QueuedEventBox();// default set capacity to Integer.MAX_VALUE
		adapters = new ArrayList<IAdapter>();
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
	}

	@Override
	public void startFeeding() throws Exception {

		if (this.isAlive()) {
			LOG.warn("This thread is already started!");
		} else {
			if (adapters == null || adapters.size() < 1) {
				throw new Exception("no adapter attached to me!");
			}
			prepareAdapters();
			socket.connect(_dest);
			Thread.sleep(100);
			super.start();
		}
	}

	@Override
	public void stopFeeding() {
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
		LOG.warn("Not supported any more, this method takes no job to do. Please use startFeeding() instead");
	}

	@Deprecated
	@Override
	/**
	 * not suggested to use this method, use stopDining() instead
	 */
	public void interrupt() {
		LOG.warn("Not supported any more, this method takes no job to do. Please use stopFeeding() instead");
	}

	private void prepareAdapters() {
		for (IAdapter adapter : adapters) {
			adapter.setFeedStream(box);
			adapter.startAdapter();
		}
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
			feed();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private void feed() throws InterruptedException, IOException {
		Event evt;
		AtomicE ae;
		while (!Thread.currentThread().isInterrupted()) {
			ae = box.take();
			evt = EventBuilder.buildEvent(ae);
			// evt.setProducer(new Actor());
			
			byte[] request_data = JDKSerializeUtil.getBytes(evt);
			socket.send(request_data, 0);
			byte[] reply = socket.recv(0);
			LOG.debug(new String(reply));
		}
	}

}
