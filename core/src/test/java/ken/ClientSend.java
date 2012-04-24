package ken;

import java.io.IOException;
import java.net.UnknownHostException;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.meta.PatAdmitEvent;
import ken.event.util.EventBuilder;
import ken.event.util.JDKSerializeUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

public class ClientSend extends Thread {
	public static Logger LOG = Logger.getLogger(ClientSend.class);
	
	ZMQ.Context context;
	ZMQ.Socket socket;
	final static String router_host = "tcp://localhost:"+EConfig.loadAll().get(EConfig.EDA_ROUTER_INCOMING_PORT);
	final static int MAX_RETRY = 3;
	final static int ITVL_RETRY = 200; // millisecond
	String _dest;
	String _id;

	public ClientSend(String id, String dest) {
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
		_dest = dest;
		_id = id;
		socket.setIdentity(_id.getBytes());

		

	}

	public void run() {
		LOG.debug("to connect");
		socket.connect(router_host);
		LOG.debug("connect ok");
		
		//TODO upgrade the reliability
		
//		while (!Thread.currentThread().isInterrupted()) {
//			socket.send(_dest.getBytes(), ZMQ.SNDMORE);
//			socket.send("".getBytes(), ZMQ.SNDMORE);
//			socket.send("Hello".getBytes(), 0);
//			
//			String reply;
//
//			for (int i = 0; i < MAX_RETRY; i++) {
//				try {
//					reply = new String(socket.recv(ZMQ.NOBLOCK));
//					if (reply != null) {
//						System.out.println("[Client-" + _id
//								+ "] received reply:[" + reply + "]");
//						break;
//					}
//				} catch (NullPointerException nullex) {
//					nullex.printStackTrace();
//					System.out.println("[Client-" + _id + "] left retry time: "
//							+ (MAX_RETRY - i - 1));
//					if (i == (MAX_RETRY - 1)) {
//						
//						socket.setLinger(0);
//						socket.close();
//						socket = context.socket(ZMQ.REQ);
//						socket.setIdentity(_id.getBytes());
//						socket.connect(router_host);
//						break;
//					}
//				} finally {
//					try {
//						Thread.sleep(ITVL_RETRY);
//					} catch (InterruptedException ie) {
//						ie.printStackTrace();
//					}
//				}
//			}
//
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException ie) {
//				ie.printStackTrace();
//			}
//		}
		while (!Thread.currentThread().isInterrupted()) {
			LOG.debug("to send");
			socket.send(_dest.getBytes(), ZMQ.SNDMORE);
			socket.send("".getBytes(), ZMQ.SNDMORE);
			
			PatAdmitEvent pae = new PatAdmitEvent();
			pae.setpName("张三");
			pae.setAdmitTS(System.currentTimeMillis()-1000);
			pae.setpCard("医保卡0000001");
			try {
				@SuppressWarnings("unchecked")
				Event<PatAdmitEvent> e = EventBuilder.buildEvent(pae);
				socket.send(JDKSerializeUtil.getBytes(e), 0);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//socket.send("测试中文".getBytes(), 0);
			LOG.debug("send ok");
			LOG.debug("to recv");
			String reply;

			reply = new String(socket.recv(0));
			LOG.debug("recv ok");
			if (reply != null) {
				LOG.debug("[Client-" + _id
						+ "] received reply:[" + reply + "]");
			}

			try {
				Thread.sleep(4000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		Thread c1 = new ClientSend("c1", "worker_A");
		Thread c2 = new ClientSend("c2", "worker_B");
		Thread c3 = new ClientSend("c3", "worker_C");
		c1.start();
		c2.start();
		c3.start();
	}
}
