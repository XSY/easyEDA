package ken.event.bus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import ken.event.EConfig;
import ken.event.util.FileUtil;
import ken.event.util.JDKSerializeUtil;
import ken.event.util.ZMQUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

/**
 * HA capable Router which provide highly automatic recovery function by
 * persisting messages to file.
 * 
 * @author KennyZJ
 * 
 */
public class SuperRouter implements Runnable {

	public static Logger LOG = Logger.getLogger(SuperRouter.class);

	private static Map<String, Object> conf = EConfig.loadAll();

	public final static String DIR_ALL = "waiting";
	public final static String DIR_DONE = "done";
	public final static String DIR_STATE = "state";
	public final static String DIR_FORK = "fork-history";
	public final static String EXT = ".msg";
	public final static String SPLITTER = "|";
	public final static String SPLITTER_REGEX = "\\" + SPLITTER;
	// public final static String FILE_WORKER = "worker";
	// public final static String FILE_SENDER = "sender";

	private String dirhome;

	private Map<String, Queue<SocketID>> worker_threadpool;

	/**
	 * all workers for keeping connection state by swap file, redundant to
	 * worker_threadpool
	 */
	// private LinkedBlockingQueue<SocketID> workers;

	/**
	 * all senders for keeping connection state by swap file
	 */
	// private LinkedBlockingQueue<String> senders;

	/**
	 * only for swap all new received msgs to disk
	 */
	private LinkedBlockingQueue<Message> all;
	private ConcurrentHashMap<String, String> logged;

	/**
	 * just received, but not sent yet
	 */
	private LinkedBlockingQueue<Message> pending;

	/**
	 * tried, but not sent for no available receiver
	 */
	private ConcurrentHashMap<String, Queue<WaitingMessage>> suspended;

	/**
	 * already sent, but no reply yet
	 */
	private ConcurrentHashMap<String, WaitingMessage> waitingReply;

	/**
	 * msgId is enough for identify a well-processed msg, mv the msg from
	 * folder-waiting to folder-done
	 */
	private LinkedBlockingQueue<String> done;

	private Thread[] swapAll_threads;
	private Thread[] swapDone_threads;

	// private Thread swapWorker_thread;
	// private Thread swapSender_thread;
	private Thread daemon;
	private int pulse;
	private Thread history_thread;

	private Context context;
	private Poller poller;
	private Socket frontend;
	private Socket backend;

	// private String askword;
	private String reportword;
	private String workmode;
	private int swapall;
	private int swapdone;

	private int msgTimeout;

	/**
	 * construct a Super Router
	 * 
	 * @param isAgile
	 *            if true, swap done msg straightway; otherwise swap
	 *            periodically
	 */
	public SuperRouter() {
		dirhome = (String) conf.get(EConfig.EDA_ROUTER_DIR_HOME);
		// askword = (String) conf.get(EConfig.EDA_ROUTER_HAND_ASK);
		reportword = (String) conf.get(EConfig.EDA_ROUTER_HAND_REPORT);
		workmode = (String) conf.get(EConfig.EDA_ROUTER_MODE);
		swapall = ((Integer) conf.get(EConfig.EDA_ROUTER_SWAPALL_COUNT))
				.intValue();
		swapdone = ((Integer) conf.get(EConfig.EDA_ROUTER_SWAPDONE_COUNT))
				.intValue();
		pulse = ((Integer) conf.get(EConfig.EDA_ROUTER_DAEMON_PULSE))
				.intValue();
		msgTimeout = ((Integer) conf.get(EConfig.EDA_ROUTER_MSG_TIMEOUT))
				.intValue();

		worker_threadpool = new HashMap<String, Queue<SocketID>>();
		// workers = new LinkedBlockingQueue<SocketID>();
		// senders = new LinkedBlockingQueue<String>();
		all = new LinkedBlockingQueue<Message>();
		logged = new ConcurrentHashMap<String, String>();
		pending = new LinkedBlockingQueue<Message>();
		suspended = new ConcurrentHashMap<String, Queue<WaitingMessage>>();
		waitingReply = new ConcurrentHashMap<String, WaitingMessage>();
		done = new LinkedBlockingQueue<String>();

		initZMQ();
		if (needRecover()) {
			recover();
		} else {
			LOG.info("Lucky for you! It seems there is nothing to recover.");
		}
	}

	private void recover() {
		// 1. mv folder wait to pending-history
		// 2. recover workers & senders from state file and ask them to report,
		// after that clear file[no need any more]
		// 3. start history_thread
		LOG.info("It seems like the Router has just crashed or restarted. Now start recovering...");

		String waitingfileDir = dirhome + File.separator + DIR_ALL;
		String pendinghisDir = dirhome + File.separator + DIR_FORK;
		// String workercache = dirhome + File.separator + DIR_STATE
		// + File.separator + FILE_WORKER;
		// String sendercache = dirhome + File.separator + DIR_STATE
		// + File.separator + FILE_SENDER;

		LOG.info("moving msg files in file type[" + EXT + "] from ["
				+ waitingfileDir + "] to [" + pendinghisDir + "]...");
		try {
			FileUtil.moveDirContent(waitingfileDir, pendinghisDir, EXT);
		} catch (IOException e) {
			LOG.error("move msgs from " + waitingfileDir + " to "
					+ pendinghisDir + " failed with exception:\n" + e);
		}
		LOG.info("move ok!");
		// LOG.info("reconnecting workers...");
		// List<String> workers = new ArrayList<String>();
		// try {
		// FileUtil.readFileInLine(workercache, workers);
		// if (workers != null && workers.size() > 0) {
		// for (String worker : workers) {
		// String key = worker.split(SPLITTER_REGEX)[0];
		// String id = worker.split(SPLITTER_REGEX)[1];
		// LOG.info("connecting worker...");
		// LOG.info("key=" + key + "|length=" + key.length());
		// LOG.info("id=" + id + "|length=" + id.length());
		// askWorker(new SocketID(worker.split(SPLITTER_REGEX)[0],
		// worker.split(SPLITTER_REGEX)[1]));
		// }
		// }
		// FileUtil.clearFile(workercache);
		// } catch (IOException e) {
		// LOG.error("reload workers failed with exception:\n" + e);
		// }
		// LOG.info("reconnecting senders...");
		// List<String> senders = new ArrayList<String>();
		// try {
		// FileUtil.readFileInLine(sendercache, senders);
		// if (senders != null && senders.size() > 0) {
		// for (String sender : senders) {
		// LOG.info("connecting sender:" + sender);
		// askSender(sender);
		// }
		// }
		// FileUtil.clearFile(sendercache);
		// } catch (IOException e) {
		// LOG.error("reload senders failed with exception:\n" + e);
		// }
		history_thread = new Thread(new HistoryJobs());
		history_thread.setDaemon(true);
		history_thread.start();
	}

	// private void askSender(String sender) throws IOException {
	// ZMQHelper.ask(frontend, sender, askword);
	// }
	//
	// private void askWorker(SocketID socketID) throws IOException {
	// ZMQHelper.ask(backend, socketID, askword);
	// }

	/**
	 * check if folder:waiting is empty, true return false
	 * 
	 * @return
	 */
	private boolean needRecover() {
		LOG.info("checking if Router need to recover...");
		return !FileUtil.isDirEmpty(dirhome + File.separator + DIR_ALL, EXT);
	}

	private void initZMQ() {
		// Prepare router context and sockets
		context = ZMQ.context(1);
		frontend = context.socket(ZMQ.ROUTER);
		backend = context.socket(ZMQ.ROUTER);

		frontend.bind("tcp://*:" + conf.get(EConfig.EDA_ROUTER_INCOMING_PORT));
		backend.bind("tcp://*:" + conf.get(EConfig.EDA_ROUTER_OUTGOING_PORT));

		poller = context.poller(2);
		// Always poll for both backend and frontend
		poller.register(backend, Poller.POLLIN);
		poller.register(frontend, Poller.POLLIN);
	}

	private void startDaemon() throws InterruptedException {
		daemon = new Thread(new RouterDaemon(pulse));
		daemon.setDaemon(true);
		daemon.start();
		Thread.sleep(10);
	}

	@Override
	public void run() {
		SocketID workerId;
		String workerGroup;
		String clientId;

		try {
			startDaemon();
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOG.error("start daemon thread error " + e);
		}

		while (!Thread.currentThread().isInterrupted()) {

			LOG.debug("all-----------------------size() = [" + all.size() + "]");
			LOG.debug("logged--------------------size() = [" + logged.size()
					+ "]");
			LOG.debug("pending-------------------size() = [" + pending.size()
					+ "]");
			LOG.debug("waitingReply--------------size() = ["
					+ waitingReply.size() + "]");
			LOG.debug("done----------------------size() = [" + done.size()
					+ "]");
			LOG.debug("suspended-----------------size() = [" + suspended.size()
					+ "]");

			// Set<String> keylogged = logged.keySet();
			// Iterator<String> itl = keylogged.iterator();
			// while (itl.hasNext()) {
			// String key = itl.next();
			// LOG.debug("logged--------------------key[" + key + "] value = "
			// + logged.get(key));
			// }

			Set<String> keys = suspended.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				LOG.debug("suspended-----------------key[" + key + "] size = "
						+ suspended.get(key).size());
			}

			LOG.debug("worker_threadpool---------size() = ["
					+ worker_threadpool.size() + "]");
			Set<String> keyw = worker_threadpool.keySet();
			Iterator<String> itw = keyw.iterator();
			while (itw.hasNext()) {
				String key = itw.next();
				LOG.debug("worker_threadpool---------" + key);
				LOG.debug("worker_threadpool---------key[" + key + "] size = "
						+ worker_threadpool.get(key).size());
			}

			poller.poll();
			if (poller.pollin(0)) {
				try {
					workerId = (SocketID) JDKSerializeUtil.getObject(backend
							.recv(0));
					backend.recv(0);// empty
					clientId = new String(backend.recv(0));
					workerGroup = workerId.getFollower_key();
					activateWorker(workerGroup, workerId);
					LOG.debug("clientId = " + clientId);
					if (!reportword.equalsIgnoreCase(clientId)) {
						// continue getting reply
						gotReply(clientId); // clientId.value == msgId.value
					}
					emit();
				} catch (IOException e) {
					LOG.error(e);
				} catch (ClassNotFoundException e) {
					LOG.error(e);
				} catch (InterruptedException e) {
					LOG.error(e);
					LOG.error("Router system is going down......");
					System.exit(0);
					// Thread.currentThread().interrupt();
				}
			}
			if (poller.pollin(1)) {
				try {
					recvNewMsg();
					emit();
				} catch (IOException e) {
					LOG.error(e);
				} catch (InterruptedException e) {
					LOG.error(e);
				}
			}
		}
	}

	/**
	 * receive a new message from frontend
	 * 
	 * @throws InterruptedException
	 */
	private void recvNewMsg() throws InterruptedException {
		Message msg = ZMQUtil.receive(frontend);
		// must reply frontend immediately
		frontend.send(msg.getPart1(), ZMQ.SNDMORE);
		frontend.send(Message.EMPTY, ZMQ.SNDMORE);
		frontend.send("ok".getBytes(), 0);
		// senders.put(new String(msg.getPart1()));
		startMsg(msg);
	}

	/**
	 * start a new message processing, probably when receive a new message from
	 * frontend or recover an unwell-processed message file
	 * 
	 * @param msg
	 * @throws InterruptedException
	 */
	private void startMsg(Message msg) throws InterruptedException {
		all.put(msg);
		pending.put(msg);
	}

	/**
	 * resume a suspended msg by offering it into pending list
	 * 
	 * @param suspendedMsgs
	 * @throws InterruptedException
	 */
	private void resume(Queue<WaitingMessage> suspendedMsgs)
			throws InterruptedException {
		WaitingMessage wmsg = suspendedMsgs.poll();
		if (wmsg == null) {
			throw new InterruptedException("the suspended msg queue is empty!");
		}
		if (!pending.offer(wmsg)) {
			throw new InterruptedException(
					"the queue of waiting to send is full!");
		}
	}

	private void doHomework(String workerGroup) throws InterruptedException {
		if (!suspended.containsKey(workerGroup)) {
			return;
		} else {
			Queue<WaitingMessage> suspendedMsgs = suspended.get(workerGroup);
			if (suspendedMsgs.isEmpty()) {
				suspended.remove(workerGroup);
			} else {
				resume(suspendedMsgs);
			}
		}
	}

	private void emit() throws IOException {
		Message msg = pending.poll();// unblock
		if (msg != null) {
			tryToSend(msg);
		}
	}

	private void tryToSend(Message msg) throws IOException {
		byte[] part1 = msg.getPart1();
		byte[] part2 = msg.getPart2();
		String workerGroup = new String(part2);
		Queue<SocketID> workers = worker_threadpool.get(workerGroup);
		if (workers != null && !workers.isEmpty()) {
			SocketID worker = workers.poll();
			msg.setPart1(JDKSerializeUtil.getBytes(worker));// reset real worker
			msg.setPart2(msg.getId());
			// part3 is payload, and never changed
			ZMQUtil.send(msg, backend);
			// revert the msg, for resend
			msg.setPart1(part1);
			msg.setPart2(part2);
			waitReply(msg);
		} else {
			suspend(workerGroup, msg);
		}
	}

	/**
	 * suspend the message which have no worker to send to, and then transform
	 * to waitingmessage which have a specific timeout
	 * 
	 * @param workerGroup
	 *            actually means follower's key
	 * @param msg
	 */
	private void suspend(String workerGroup, Message msg) {
		worker_threadpool.remove(workerGroup);
		WaitingMessage wmsg = new WaitingMessage(msg, msgTimeout);
		if (suspended.get(workerGroup) != null) {
			suspended.get(workerGroup).offer(wmsg);
		} else {
			Queue<WaitingMessage> q = new LinkedList<WaitingMessage>();
			q.offer(wmsg);
			suspended.put(workerGroup, q);
		}
	}

	/**
	 * After send to the receiver and start waiting reply, the original message
	 * should be converted to waiting message, and set a timeout or by default.
	 * 
	 * @param msg
	 */
	private void waitReply(Message msg, long timeout) {
		WaitingMessage wmsg = new WaitingMessage(msg, timeout);
		waitingReply.put(new String(msg.getId()), wmsg);
	}

	/**
	 * After send to the receiver and start waiting reply, the original message
	 * should be converted to waiting message, and set a timeout or by default.
	 * 
	 * @param msg
	 */
	private void waitReply(Message msg) {
		WaitingMessage wmsg = new WaitingMessage(msg);
		waitingReply.put(new String(msg.getId()), wmsg);
	}

	private void gotReply(String replyMsgId) throws InterruptedException {
		waitingReply.remove(replyMsgId);
		LOG.debug("putting msg[" + replyMsgId + "] to done queue...");
		done.put(replyMsgId);// if full, block the thread
	}

	private void activateWorker(String workerGroup, SocketID workerId)
			throws InterruptedException {
		// workers.put(workerId);
		LOG.debug("worker--fkey=" + workerId.getFollower_key() + "|length="
				+ workerId.getFollower_key().length());
		LOG.debug("worker--tid=" + workerId.getThreadID() + "|length="
				+ workerId.getThreadID().length());
		// LRU policy
		Queue<SocketID> q;
		if (!worker_threadpool.containsKey(workerGroup)) {
			q = new LinkedList<SocketID>();
			q.offer(workerId);
			worker_threadpool.put(workerGroup, q);
		} else {
			q = worker_threadpool.get(workerGroup);
			if (q.contains(workerId)) {
				return;
			}
			worker_threadpool.get(workerGroup).offer(workerId);
		}
		doHomework(workerGroup);
	}

	class Swap implements Runnable {
		@SuppressWarnings("rawtypes")
		LinkedBlockingQueue _queue;
		int _mode = -1;
		final static int MODE_ALL = 0;
		final static int MODE_DONE = 1;
		final static int MODE_WORKER = 2;
		final static int MODE_SENDER = 3;
		String dir;
		String filename;

		@SuppressWarnings("rawtypes")
		Swap(LinkedBlockingQueue queue, int mode) {
			_queue = queue;
			_mode = mode;
		}

		@Override
		public void run() {
			switch (_mode) {
			case MODE_ALL:
				dir = dirhome + File.separator + DIR_ALL;
				LOG.info("Swap is running to swap all message...");
				all();
			case MODE_DONE:
				dir = dirhome + File.separator + DIR_DONE;
				LOG.info("Swap is running to move every single message which is done...");
				done();
				// case MODE_WORKER:
				// dir = dirhome + File.separator + DIR_STATE;
				// filename = FILE_WORKER;
				// LOG.info("Swap is running to swap changing workers of the process...");
				// worker();
				// case MODE_SENDER:
				// dir = dirhome + File.separator + DIR_STATE;
				// filename = FILE_SENDER;
				// LOG.info("Swap is running to swap changing senders of the process...");
				// sender();
			default:
				LOG.info("invalid mode, going to shut done the Swap thread...");
				return;
			}
		}

		// private void sender() {
		// int count = 0;
		// String item;
		// Map<String, String> checklist = new HashMap<String, String>();
		// // read sender into checklist
		// while (!Thread.currentThread().isInterrupted()) {
		// try {
		// item = (String) _queue.take();
		// if (!checklist.containsKey(item)) {
		// checklist.put(item, "N");
		// // append the sender ID in file sender
		// FileUtil.writeLine(item.getBytes(), dir, filename);
		// count++;
		// LOG.debug("[" + count
		// + "] senders has been stored to file.");
		// }
		// } catch (InterruptedException e) {
		// LOG.error(e);
		// } catch (IOException e) {
		// LOG.error("flush sender to file failed with exception:\n"
		// + e);
		// }
		// }
		// }
		//
		// private void worker() {
		// int count = 0;
		// SocketID item;
		// Map<SocketID, String> checklist = new HashMap<SocketID, String>();
		// // read worker into checklist
		//
		// while (!Thread.currentThread().isInterrupted()) {
		// try {
		// item = (SocketID) _queue.take();
		// if (!checklist.containsKey(item)) {
		// checklist.put(item, "N");
		// // append the worker socketID in file worker
		// FileUtil.writeLine(
		// (item.getFollower_key() + SPLITTER + item
		// .getThreadID()).getBytes(), dir,
		// filename);
		// count++;
		// LOG.debug("[" + count
		// + "] workers has been stored to file.");
		// }
		// } catch (InterruptedException e) {
		// LOG.error(e);
		// } catch (IOException e) {
		// LOG.error("flush worker to file failed with exception:\n"
		// + e);
		// }
		// }
		// }

		@SuppressWarnings("unchecked")
		private void done() {
			int count = 0;
			String item;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					item = (String) _queue.take();
					LOG.debug("logged.get(" + item + ")=" + logged.get(item));

					if (logged.get(item) != null) {
						FileUtil.moveFileToDir(dirhome + File.separator
								+ DIR_ALL + File.separator + item + EXT, dir);
						logged.remove(item);
						count++;
						LOG.debug("["
								+ count
								+ "] messages has done and moved to folder:done");
					} else {
						LOG.debug("Seems the msg have not been logged, requeue the done message with id["
								+ item + "]...");
						_queue.put(item);// reEnqueue
						Thread.sleep(1000);// lower CPU load
					}

				} catch (InterruptedException e) {
					LOG.error(e);
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}

		private void all() {
			int count = 0;
			Message item;
			List<byte[]> bin_item = new ArrayList<byte[]>();

			while (!Thread.currentThread().isInterrupted()) {
				try {
					item = (Message) _queue.take();
					String msgid = new String(item.getId());
					bin_item.add(item.getId());
					bin_item.add(item.getPart1());
					bin_item.add(item.getPart2());
					bin_item.add(item.getPart3());
					FileUtil.writeLines(bin_item, dir, msgid + EXT);
					if (logged.putIfAbsent(msgid, "Y") != null) {
						LOG.warn("Seems like that the msg with id[" + msgid
								+ "] has already been put into the logged map!");
					}
					bin_item.clear();
					LOG.debug("[" + Thread.currentThread().getName()
							+ "] has wroten [" + count++ + "] messages!");
				} catch (InterruptedException e) {
					LOG.error(e);
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

	class HistoryJobs implements Runnable {
		String dir;
		File[] jobs;
		Message msg;
		int count = 0;

		@Override
		public void run() {
			// read all msgs in history folder into memory, then clear
			LOG.info("running history jobs...");
			dir = dirhome + File.separator + DIR_FORK;
			if (!FileUtil.isDirEmpty(dir, EXT)) {
				jobs = FileUtil.listFiles(dir, EXT);
				for (File job : jobs) {
					try {
						msg = new Message();
						FileUtil.readMsgFile(job.getAbsolutePath(), msg);
						startMsg(msg);
						FileUtil.deleteFile(job.getAbsolutePath());
						count++;
						LOG.debug("read historymsg-" + count + " with id="
								+ new String(msg.getId()));
					} catch (IOException e) {
						LOG.error("readMsgFile() error!\n" + e);
					} catch (InterruptedException e) {
						LOG.error("startMsg() error!\n" + e);
					}
				}
			}
			LOG.info("cleared all left messages[" + count
					+ "] from last crash.");
		}
	}

	class RouterDaemon implements Runnable {

		final static int DEFAULT_PULSE = 2000; // in millisecond
		int _pulse;

		RouterDaemon() {
			this(DEFAULT_PULSE);

		}

		RouterDaemon(int pulse) {
			_pulse = pulse;
		}

		public void run() {
			LOG.info("running daemon thread...");
			startAll();
			while (!Thread.currentThread().isInterrupted()) {
				try {
					LOG.debug("heartbeating...");
					resendWaitingReplyMsgs();
					detect();
					Thread.sleep(_pulse);
				} catch (InterruptedException e) {
					LOG.error(e);
					Thread.currentThread().interrupt();
				}
			}
		}

		private void detect() {
			for (Thread t : swapAll_threads) {
				if (!t.isAlive()) {
					LOG.info("swapAll thread:" + t.getName() + " is dead!!");
				}
			}
			for (Thread t : swapDone_threads) {
				if (!t.isAlive()) {
					LOG.info("swapDone thread:" + t.getName() + " is dead!!");
				}
			}
			// if (!swapWorker_thread.isAlive()) {
			// LOG.info("swapWorker thread:" + swapWorker_thread.getName()
			// + " is dead!!");
			// }
			// if (!swapSender_thread.isAlive()) {
			// LOG.info("swapSender thread:" + swapSender_thread.getName()
			// + " is dead!!");
			// }
			// LOG.debug("Thread.currentThread().getThreadGroup()="
			// + Thread.currentThread().getThreadGroup().list());
		}

		private void startAll() {
			swapAll_threads = new Thread[swapall];
			for (int i = 0; i < swapall; i++) {
				swapAll_threads[i] = new Thread(new Swap(all, Swap.MODE_ALL));
				swapAll_threads[i].start();
			}

			if ("HARD".equalsIgnoreCase(workmode.trim())) {
				swapDone_threads = new Thread[swapdone];
				for (int i = 0; i < swapdone; i++) {
					swapDone_threads[i] = new Thread(new Swap(done,
							Swap.MODE_DONE));
					swapDone_threads[i].start();
				}
			}
			// swapWorker_thread = new Thread(new Swap(workers,
			// Swap.MODE_WORKER));
			// swapSender_thread = new Thread(new Swap(senders,
			// Swap.MODE_SENDER));
			// swapWorker_thread.start();
			// swapSender_thread.start();
		}

		private void resendWaitingReplyMsgs() throws InterruptedException {
			if (!waitingReply.isEmpty()) {
				Set<Entry<String, WaitingMessage>> entryset = waitingReply
						.entrySet();
				for (Entry<String, WaitingMessage> e : entryset) {
					WaitingMessage wmsg = new WaitingMessage();
					wmsg = e.getValue();
					if (wmsg.isExpired()) {
						pending.put((Message) wmsg);
						waitingReply.remove(e.getKey());
					}
				}
			}
		}
	}

	public static void main(String... args) {
		Thread t = new Thread(new SuperRouter());
		t.start();
	}

}
