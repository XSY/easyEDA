/**
 * 
 */
package ken;

import java.io.IOException;
import java.util.Random;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.bus.SocketID;
import ken.event.meta.PatAdmitEvent;
import ken.event.util.JDKSerializeUtil;
import ken.event.util.ZMQUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

/**
 * @author KennyZJ
 * 
 */
public class ClientRecv extends Thread {
	public static Logger LOG = Logger.getLogger(ClientRecv.class);
	ZMQ.Context context;
	ZMQ.Socket socket;
	final static String router_host = "tcp://localhost:"
			+ EConfig.loadAll().get(EConfig.EDA_ROUTER_OUTGOING_PORT);
	String fkey;
	String tid;
	SocketID sid;

	public ClientRecv(String key) {
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);

		fkey = key;
		Random random = new Random();
		tid = random.nextInt() + "";
		sid = new SocketID(key, tid);
		try {
			socket.setIdentity(JDKSerializeUtil.getBytes(sid));
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket.connect(router_host);
	}

	public void run() {
		int count = 0;
		String ask = (String) EConfig.loadAll()
				.get(EConfig.EDA_ROUTER_HAND_ASK);
		String report = (String) EConfig.loadAll().get(
				EConfig.EDA_ROUTER_HAND_REPORT);
		ZMQUtil.report(socket, report);
		Poller poller = context.poller(1);

		while (!Thread.currentThread().isInterrupted()) {

			poller.register(socket, Poller.POLLIN);
			poller.poll(3000 * 1000);
			if (poller.pollin(0)) {
				// received
				LOG.debug(Thread.currentThread().getName() + "[Worker]-[fkey:"
						+ fkey + "|tid=" + tid + "] is waiting to recv...");
				String client_addr = new String(socket.recv(0));
				if (ask.equals(client_addr)) {
					LOG.debug(Thread.currentThread().getName()
							+ "[Worker]-[fkey:" + fkey + "|tid=" + tid
							+ "] I am asked! Going to report I am alive...");
					ZMQUtil.report(socket, report);
				} else if (socket.hasReceiveMore()) {
					count++;
					String msgId = client_addr; // part1 is auto-matched with my
												// socketID, part2 is msg_id

					String empty = new String(socket.recv(0));
					assert empty.length() == 0 | true;

					//String request = new String(socket.recv(0));// payload, the
																// real
																// target
					
					Event<PatAdmitEvent> e;
					try {
						e = (Event)JDKSerializeUtil.getObject(socket.recv(0));
					
					LOG.debug(Thread.currentThread().getName()
							+ "[Worker]-[fkey:" + fkey + "|tid=" + tid
							+ "] recv ok");

					socket.send(msgId.getBytes(), 0);// send msgId for
														// representing
														// recv ok!

					LOG.debug(Thread.currentThread().getName()
							+ "[Worker]-[fkey:" + fkey + "|tid=" + tid
							+ "] processed request:[" + e.getEvtData().getpName()
							+ "] from ClientSender[" + client_addr + "]");
					LOG.debug(Thread.currentThread().getName()
							+ "[Worker]-[fkey:" + fkey + "|tid=" + tid
							+ "] since client started, received [" + count
							+ "] msgs!");
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				}
				poller.unregister(socket);
			} else {
				// no response from router
				LOG.debug(Thread.currentThread().getName() + "[Worker]-[fkey:"
						+ fkey + "|tid=" + tid
						+ "] no response from router server.");
				LOG.debug(Thread.currentThread().getName() + "[Worker]-[fkey:"
						+ fkey + "|tid=" + tid + "] reconnecting...");
				socket.setLinger(0);
				socket.close();
				poller.unregister(socket);
				socket = context.socket(ZMQ.REQ);
				try {
					socket.setIdentity(JDKSerializeUtil.getBytes(sid));
				} catch (IOException e) {
					e.printStackTrace();
				}
				socket.connect(router_host);
				ZMQUtil.report(socket, report);
			}
		}
		socket.close();
		context.term();
	}

	public static void main(String[] args) {
		Thread wa = new ClientRecv("worker_A");
		Thread wb = new ClientRecv("worker_B");

		wa.start();
		LOG.debug(wa.getName() + " started!");
		wb.start();
		LOG.debug(wb.getName() + " started!");
		Thread wc = new ClientRecv("worker_C");
		wc.start();
		LOG.debug(wc.getName() + " started!");
	}
}
