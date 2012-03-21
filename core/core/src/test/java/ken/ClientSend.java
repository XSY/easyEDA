package ken;

import org.zeromq.ZMQ;

public class ClientSend extends Thread {

	ZMQ.Context context;
	ZMQ.Socket socket;
	final static String router_host = "tcp://localhost:5588";
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
		socket.connect(router_host);
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
			socket.send(_dest.getBytes(), ZMQ.SNDMORE);
			socket.send("".getBytes(), ZMQ.SNDMORE);
			socket.send("Hello".getBytes(), 0);
			
			String reply;

			reply = new String(socket.recv(0));
			if (reply != null) {
				System.out.println("[Client-" + _id
						+ "] received reply:[" + reply + "]");
			}

			try {
				Thread.sleep(2000);
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
