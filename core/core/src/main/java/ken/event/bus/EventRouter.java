package ken.event.bus;

import java.util.Map;

import ken.event.EConfig;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

/**
 * Simple router which only support plain follower in non-HA way
 * @author KennyZJ
 * 
 */
public class EventRouter {

	public static Logger log = Logger.getLogger(EventRouter.class);

	public static void main(String[] args) {

		Map<String, Object> conf = EConfig.loadAll();
		int front_port = (Integer) conf.get(EConfig.EDA_ROUTER_INCOMING_PORT);
		int back_port = (Integer) conf.get(EConfig.EDA_ROUTER_OUTGOING_PORT);

		Context context = ZMQ.context(1);

		// Prepare our context and sockets
		Socket frontend = context.socket(ZMQ.ROUTER);
		Socket backend = context.socket(ZMQ.ROUTER);
		frontend.bind("tcp://*:" + front_port);
		backend.bind("tcp://*:" + back_port);

		log.info("Router started. Listening on incoming port[" + front_port
				+ "] and outgoing port[" + back_port + "]...");

		String from;
		String to;
		byte[] request;
		String reply;
		String empty_back;
		String empty_front;

		String worker_addr;
		String client_addr;

		while (!Thread.currentThread().isInterrupted()) {

			Poller items = context.poller(2);

			//   Always poll for worker activity on backend
			items.register(backend, Poller.POLLIN);
			items.register(frontend, Poller.POLLIN);

			items.poll();

			// handle backend
			if (items.pollin(0)) {
				log.debug("backend...");

				worker_addr = new String(backend.recv(0));
				log.debug("part 1: worker_addr = [" + worker_addr + "]");

				empty_back = new String(backend.recv(0));
				log.debug("part 2: empty = [" + empty_back + "]");

				client_addr = new String(backend.recv(0));// "READY" or client
															// address
				log.debug("part 3: client_addr = [" + client_addr + "]");
				if (!"READY".equals(client_addr)) {
					empty_back = new String(backend.recv(0));
					log.debug("part 4: empty = [" + empty_back + "]");

					reply = new String(backend.recv(0));
					log.debug("part 5: reply = [" + reply + "]");

					frontend.send(client_addr.getBytes(), ZMQ.SNDMORE);
					frontend.send("".getBytes(), ZMQ.SNDMORE);
					frontend.send(reply.getBytes(), 0);
				}
			}

			// handle frontend
			if (items.pollin(1)) {
				log.debug("frontend...");

				from = new String(frontend.recv(0)); // client address
				log.debug("part 1: from = [" + from + "]");

				empty_front = new String(frontend.recv(0)); // empty
				log.debug("part 2: empty_front = [" + empty_front + "]");

				to = new String(frontend.recv(0)); // dest worker address
				log.debug("part 3: to = [" + to + "]");

				empty_front = new String(frontend.recv(0)); // empty
				log.debug("part 4: empty_front = [" + empty_front + "]");

				request = frontend.recv(0); // request payload data
				log.debug("part 5: request.length = [" + request.length + "]");

				// forward to backend
				backend.send(to.getBytes(), ZMQ.SNDMORE); // 1 set worker address
				backend.send("".getBytes(), ZMQ.SNDMORE);
				backend.send(from.getBytes(), ZMQ.SNDMORE); // 2 set client address
				backend.send("".getBytes(), ZMQ.SNDMORE);
				backend.send(request, 0); // 3 set request payload
			}
		}

		// we never get here if not interrupted
		frontend.close();
		backend.close();
		context.term();

		System.exit(0);

	}

}
