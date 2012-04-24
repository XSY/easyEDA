package ken;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ken.event.bus.SocketID;
import ken.event.util.JDKSerializeUtil;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class Router {

	public static Map<String, Queue<SocketID>> follower_threadpool = new HashMap<String, Queue<SocketID>>();

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Context context = ZMQ.context(1);

		// Prepare our context and sockets
		Socket frontend = context.socket(ZMQ.ROUTER);
		Socket backend = context.socket(ZMQ.ROUTER);
		frontend.bind("tcp://*:5588");
		backend.bind("tcp://*:5589");
		frontend.setIdentity("frontend".getBytes());
		backend.setIdentity("backend".getBytes());

		System.out
				.println("start router...with frontend port:5588 and backend port:5589");

		// build a queue for each kind of follower, work as a LRU pool

		String from;
		String to;
		String request;
		String reply;
		String empty_back;
		String empty_front;

		String worker_follower;
		SocketID worker_address;
		String client_addr;

		Poller items = context.poller(2);

		//   Always poll for backend and frontend
		items.register(backend, Poller.POLLIN);
		items.register(frontend, Poller.POLLIN);

		while (!Thread.currentThread().isInterrupted()) {
			System.out
					.println("follow_threads.size()=" + follower_threadpool.size());
//			if(follower_threadpool.isEmpty()){
//				items.unregister(frontend);
//			}
			if (follower_threadpool.get("worker_B") != null) {
				System.out.println("worker_B.size()="
						+ follower_threadpool.get("worker_B").size());
			} else {
				System.out.println("worker_B.size()=0");
			}

			items.poll();

			// serve backend
			if (items.pollin(0)) {
				System.out.println("backend...");

				worker_address = (SocketID) JDKSerializeUtil.getObject(backend
						.recv(0));

				System.out.println("part 1: worker_addr.tid = ["
						+ worker_address.getThreadID() + "]");
				worker_follower = worker_address.getFollower_key();

				// Least recently used algorithm
				addFollower(worker_follower, worker_address);

				empty_back = new String(backend.recv(0));
				System.out.println("part 2: empty = [" + empty_back + "]");

				client_addr = new String(backend.recv(0));// "READY" or client
															// address
				System.out.println("part 3: client_addr = [" + client_addr
						+ "]");

				if (!"READY".equals(client_addr)) {
					empty_back = new String(backend.recv(0));
					System.out.println("part 4: empty = [" + empty_back + "]");

					reply = new String(backend.recv(0));
					System.out.println("part 5: reply = [" + reply + "]");

					// forward reply to frontend
					// frontend.send(client_addr.getBytes(), ZMQ.SNDMORE);
					// frontend.send("".getBytes(), ZMQ.SNDMORE);
					// frontend.send(reply.getBytes(), 0);
				}
			}

			// handle frontend
			if (items.pollin(1)) {
				System.out.println("frontend...");
				
				//-------1: receive from frontend
				from = new String(frontend.recv(0)); // client address
				System.out.println("part 1: from = [" + from + "]");
				empty_front = new String(frontend.recv(0)); // empty
				System.out.println("part 2: empty_front = [" + empty_front
						+ "]");
				to = new String(frontend.recv(0)); // dest worker address
				System.out.println("part 3: to = [" + to + "]");
				empty_front = new String(frontend.recv(0)); // empty
				System.out.println("part 4: empty_front = [" + empty_front
						+ "]");
				request = new String(frontend.recv(0)); // request payload data
				System.out.println("part 5: request = [" + request + "]");

				
				//--------2: log the received msg before replying it
				
				
				//--------3: reply frontend
				frontend.send(from.getBytes(), ZMQ.SNDMORE);
				frontend.send("".getBytes(), ZMQ.SNDMORE);
				frontend.send("received by router".getBytes(), 0);

				//--------4: forward msg to backend
				if (follower_threadpool.get(to) != null) {
					SocketID curr_dest = follower_threadpool.get(to).poll();
					if (curr_dest != null) {
						byte[] real_to = JDKSerializeUtil.getBytes(curr_dest);
						backend.send(real_to, ZMQ.SNDMORE); // 1 set worker
						// address
						backend.send("".getBytes(), ZMQ.SNDMORE);
						backend.send(from.getBytes(), ZMQ.SNDMORE); // 2 set client address
						backend.send("".getBytes(), ZMQ.SNDMORE);
						backend.send(request.getBytes(), 0); // 3 set request payload
						
					} else {
						// currently, else drop the message, if no follower
						// later, will evolve HA capability by persisting state and msgs
						// to File
						
						follower_threadpool.remove(to);
					}
				}
			}
		}
		frontend.close();
		backend.close();
		context.term();

		System.exit(0);

	}

	public static void addFollower(String worker_follower,
			SocketID worker_address) {
		// Least recently used algorithm
		if (!follower_threadpool.containsKey(worker_follower)) {
			Queue<SocketID> q = new LinkedList<SocketID>();
			q.add(worker_address);
			follower_threadpool.put(worker_follower, q);
		} else {
			follower_threadpool.get(worker_follower).add(worker_address);
		}
	}

}
