package ken.event.bus;

import java.util.Map;

import ken.event.EConfig;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

/**
 * EventsPivot is the pivot of all events between event source and the process
 * centre. Use ZeroMQ's ROUTER-DEALER socket type to work as a event message
 * broker
 * 
 * @author KennyZJ
 * 
 */
public class EventPivot {
	public static Logger LOG = Logger.getLogger(EventPivot.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Object> conf = EConfig.loadAll();
		int front_port = (Integer) conf.get(EConfig.EDA_PIVOT_INCOMING_PORT);
		int back_port = (Integer) conf.get(EConfig.EDA_PIVOT_OUTGOING_PORT);

		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket frontend = context.socket(ZMQ.ROUTER);
		ZMQ.Socket backend = context.socket(ZMQ.DEALER);

		frontend.bind("tcp://*:" + front_port);
		backend.bind("tcp://*:" + back_port);
		LOG.info("Pivot Started. Listening on incoming port[" + front_port
				+ "] and outgoing port[" + back_port + "]...");

		// Initialize poll set
		Poller items = context.poller(2);
		items.register(frontend, Poller.POLLIN);
		items.register(backend, Poller.POLLIN);

		boolean more = false;
		byte[] message;

		// Switch messages between sockets
		while (!Thread.currentThread().isInterrupted()) {
			// poll and memorize multipart detection
			items.poll();
			if (items.pollin(0)) {
				LOG.debug("frontend received");
				while (true) {
					// receive message
					message = frontend.recv(0);
					more = frontend.hasReceiveMore();

					// System.out.println(new String(message));
					// Broker it
					backend.send(message, more ? ZMQ.SNDMORE : 0);
					if (!more) {
						break;
					}
				}
			}
			if (items.pollin(1)) {
				LOG.debug("backend received");
				while (true) {
					// receive message
					message = backend.recv(0);
					more = backend.hasReceiveMore();
					// Broker it
					frontend.send(message, more ? ZMQ.SNDMORE : 0);
					if (!more) {
						// System.out.println("back:"+more);
						break;
					}
				}
			}
		}
		// We never get here but clean up anyhow
		frontend.close();
		backend.close();
		context.term();
	}
}
