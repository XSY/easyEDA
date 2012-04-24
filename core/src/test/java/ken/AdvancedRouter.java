/**
 * 
 */
package ken;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import ken.event.bus.SocketID;
import ken.event.util.JDKSerializeUtil;
import ken.event.util.ZMQUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

/**
 * @author KennyZJ
 * 
 */
public class AdvancedRouter {

	public static Logger LOG = Logger.getLogger(AdvancedRouter.class);

	private static Map<String, Queue<SocketID>> worker_threadpool = new HashMap<String, Queue<SocketID>>();
	private static LinkedBlockingQueue<MSG> all = new LinkedBlockingQueue<MSG>();
	private static Map<String, MSG> toSend = new HashMap<String, MSG>();
	private static LinkedBlockingQueue<String> finished = new LinkedBlockingQueue<String>(); // only
																								// store
																								// msgId

	private Context context;

	private Thread logAll_thread;
	private Thread logOK_thread;
	
	private Thread resend_thread;
	private Thread daemon_thread;

	private Socket frontend;
	private Socket backend;

	public AdvancedRouter() {
		this(true);
	}

	public AdvancedRouter(boolean logAll) {
		initZMQ();
		if (logAll) {
			logAll_thread = new Thread(new LogThread(all));
			logAll_thread.start();
			logOK_thread = new Thread(new LogThread(finished)); 
			logOK_thread.start();
		}
	}

	private void initZMQ() {
		// Prepare our context and sockets
		context = ZMQ.context(1);
		frontend = context.socket(ZMQ.ROUTER);
		backend = context.socket(ZMQ.ROUTER);
		frontend.bind("tcp://*:5588");
		backend.bind("tcp://*:5589");
		frontend.setIdentity("router".getBytes());
		backend.setIdentity("router".getBytes());
	}

	public void start() throws IOException, ClassNotFoundException,
			InterruptedException {
		String worker_follower;
		SocketID worker_address;

		Poller items = context.poller(2);
		// Always poll for backend and frontend
		items.register(backend, Poller.POLLIN);
		items.register(frontend, Poller.POLLIN);

		while (!Thread.currentThread().isInterrupted()) {
			// System.out.println("follow_threads.size()="
			// + follower_threadpool.size());
			// // if(follower_threadpool.isEmpty()){
			// // items.unregister(frontend);
			// // }
			// if (follower_threadpool.get("worker_B") != null) {
			// System.out.println("worker_B.size()="
			// + follower_threadpool.get("worker_B").size());
			// } else {
			// System.out.println("worker_B.size()=0");
			// }
			LOG.debug("toLog.size() = " + all.size());
			LOG.debug("toSend.size() = " + toSend.size());

			items.poll();

			// handle backend
			if (items.pollin(0)) {
				LOG.debug("backend...");
				MSG msg = new MSG();
				msg.setWorker(backend.recv(0));
				backend.recv(0);// empty
				msg.setClient(backend.recv(0));

				worker_address = (SocketID) JDKSerializeUtil.getObject(msg
						.getWorker());
				worker_follower = worker_address.getFollower_key();
				// Least recently used algorithm
				addFollower(worker_follower, worker_address);

				if (!"READY".equals(new String(msg.getClient()))) {
					// if part[2] is not 'READY', it must be msg_id, otherwise
					// exception occurred

					// backend.recv(0);//empty
					// msg.setMsg(backend.recv(0));// reply from recv-client
					String msgId = new String(msg.getClient());
					
					// if recv client successfully process the msg, then purge the msg
					LOG.debug("complete(" + msgId + ") return: " + complete(msgId));
				} else {
					// TODO if worker 'READY', first check if there are any unfinished jobs for the worker to do 
				}
			}

			// handle frontend
			if (items.pollin(1)) {
				LOG.debug("frontend...");
				// -------1: receive from frontend
				MSG msg = ZMQUtil.recvFromFront(frontend);// original msg from
															// sender
				// --------2: log the received msg before replying it
				all.put(msg);
				// --------3: waiting, until recv reply from recv-client
				toSend.put(new String(msg.get_id()), msg);
				// --------4: reply frontend
				replyFront(msg);
				// --------5: forward msg to backend
				fwdToBack(msg);
			}
		}
		frontend.close();
		backend.close();
		context.term();
		System.exit(0);
	}

	private boolean complete(String msgId) throws InterruptedException {
		if (toSend.remove(msgId) == null) {
			return false;
		} else {
			finished.put(msgId);
		}
		return true;
	}

	private void replyFront(MSG msg) {
		msg.setMsg("received by router.".getBytes());
		ZMQUtil.sendDirectly(frontend, msg);
	}

	private void fwdToBack(MSG msg) throws IOException {
		byte[] workerFamilyName = msg.getWorker();
		Queue<SocketID> workers = worker_threadpool.get(workerFamilyName);
		if (workers != null && !workers.isEmpty()) {
			SocketID curr_worker = workers.poll();
			// MSG tosend = new MSG();
			// tosend.set_id(msg.get_id());
			// tosend.setClient(msg.getClient());
			// tosend.setMsg(msg.getMsg());
			msg.setWorker(JDKSerializeUtil.getBytes(curr_worker));// convert the
																	// destination
																	// from
																	// abstract
																	// to real

			ZMQUtil.send(backend, msg);// sent ok, then waiting for reply
			return;
		} else {
			// currently, else drop the message, if no follower.
			// later, will evolve HA capability by persisting state
			// and msgs
			// to File
			removeFollower(new String(workerFamilyName));
			// TODO notify or alarm system administrator that there is no worker
			// to send to according to the workerFamilyName
		}
	}

	private void removeFollower(String worker_follower) {
		worker_threadpool.remove(worker_follower);
	}

	private void addFollower(String worker_follower, SocketID worker_address) {
		// Least recently used algorithm
		if (!worker_threadpool.containsKey(worker_follower)) {
			Queue<SocketID> q = new LinkedList<SocketID>();
			q.add(worker_address);
			worker_threadpool.put(worker_follower, q);
		} else {
			worker_threadpool.get(worker_follower).add(worker_address);
		}
	}

	class LogThread implements Runnable {
		Logger LOG = Logger.getLogger(LogThread.class);
		@SuppressWarnings("rawtypes")
		LinkedBlockingQueue _queue;
		
		final static int MAX_MSG = 100;

		@SuppressWarnings("rawtypes")
		LogThread(LinkedBlockingQueue queue) {
			_queue = queue;
		}

		@Override
		public void run() {
			LOG.debug("LogThread running...");
			int count = 0;

			while (true) {
				try {
					Object item = _queue.take();
					if (item instanceof MSG) {
						LOG.info("[logThread]" + new String(((MSG)item).getClient()));
						//TODO write all msg log
					} else if (item instanceof String) {
						LOG.info("[logThread]" + (String) item);
						//TODO write finished msg log
					}
					count++;
					LOG.debug("logged objs count:" + count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ResendThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}

	class DaemonThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}

	public static void main(String... args) throws IOException,
			ClassNotFoundException, InterruptedException {
		AdvancedRouter router = new AdvancedRouter();
		// Thread.sleep(100);//wait the logThread to start
		router.start();
	}
}
