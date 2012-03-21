package ken;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ken.event.util.JDKSerializeUtil;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class Router {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Context context = ZMQ.context(1);

		// Prepare our context and sockets
		Socket frontend = context.socket(ZMQ.ROUTER);
		Socket backend = context.socket(ZMQ.ROUTER);
		frontend.bind("tcp://*:5588");
		backend.bind("tcp://*:5589");

		System.out
				.println("start router...with frontend port:5588 and backend port:5589");

		//build a queue for each kind of follower, work as a LRU pool 
		Map<String, Queue<SocketID>> follow_threads = new HashMap<String, Queue<SocketID>>();

		while (!Thread.currentThread().isInterrupted()) {
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

			//   Always poll for worker activity on backend
			items.register(backend, Poller.POLLIN);
			if(!follow_threads.isEmpty()){
				items.register(frontend, Poller.POLLIN);
			}
			

			items.poll();

			// handle backend
			if (items.pollin(0)) {
				System.out.println("backend...");

				worker_address = (SocketID) JDKSerializeUtil.getObject(backend
						.recv(0));
				worker_follower = worker_address.getFollower_key();
				System.out.println("part 1: worker_addr.tid = ["
						+ worker_address.getThreadID() + "]");
				
				//Least recently used algorithm 
				if (!follow_threads.containsKey(worker_follower)) {
					Queue<SocketID> q = new LinkedList<SocketID>();
					q.add(worker_address);
					follow_threads.put(worker_follower, q);
				} else {
//					if (!follow_threads.get(worker_follower).contains(
//							worker_address)) {
//						follow_threads.get(worker_follower).add(
//								worker_address);
//					}
					follow_threads.get(worker_follower).add(
							worker_address);
				}
				
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

				// First: reply to front once recv from it
				frontend.send(from.getBytes(), ZMQ.SNDMORE);
				frontend.send("".getBytes(), ZMQ.SNDMORE);
				frontend.send("received by router".getBytes(), 0);

				// Second: forward request to backend
				if (follow_threads.get(to) != null) {
					SocketID curr_dest = follow_threads.get(to).poll();
					if (curr_dest != null) {
						byte[] real_to = JDKSerializeUtil.getBytes(curr_dest);
						backend.send(real_to, ZMQ.SNDMORE); // 1 set worker
						// address
						backend.send("".getBytes(), ZMQ.SNDMORE);
						backend.send(from.getBytes(), ZMQ.SNDMORE); // 2 set
																	// client
						// address
						backend.send("".getBytes(), ZMQ.SNDMORE);
						backend.send(request.getBytes(), 0); // 3 set request
						// payload
					}
					//else drop the message
				}
			}
		}
		frontend.close();
		backend.close();
		context.term();

		System.exit(0);

	}
}
