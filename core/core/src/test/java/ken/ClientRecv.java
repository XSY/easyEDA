/**
 * 
 */
package ken;

import java.io.IOException;
import java.util.Random;

import ken.event.util.JDKSerializeUtil;

import org.zeromq.ZMQ;

/**
 * @author KennyZJ
 * 
 */
public class ClientRecv extends Thread {

	ZMQ.Context context;
	ZMQ.Socket socket;
	final static String router_host = "tcp://localhost:5589";
	String _id;

	public ClientRecv(String key) {
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.REQ);
		_id = key;
		Random random = new Random();
		String tid = random.nextInt()+"";
		SocketID sid = new SocketID(key, tid);
		try {
			socket.setIdentity(JDKSerializeUtil.getBytes(sid));
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket.connect(router_host);

	}
	

	public void run() {

		socket.send("READY".getBytes(), 0);

		while (!Thread.currentThread().isInterrupted()) {
			String client_addr = new String(socket.recv(0));
			String empty = new String(socket.recv(0));
			assert empty.length() == 0 | true;

			String request = new String(socket.recv(0));// payload, the real
														// target

			socket.send(client_addr.getBytes(), ZMQ.SNDMORE);
			socket.send(empty.getBytes(), ZMQ.SNDMORE);
			socket.send(("I am worker-" + _id).getBytes(), 0);

			System.out.println(Thread.currentThread().getName() + "-[Worker-"
					+ _id + "] processed request:[" + request
					+ "] from ClientSender[" + client_addr + "]");
		}
	}

	public static void main(String[] args) {
		Thread wa = new ClientRecv("worker_A");
		Thread wb = new ClientRecv("worker_B");

		wa.start();
		System.out.println(wa.getName() + " started!");
		wb.start();
		System.out.println(wb.getName() + " started!");
		Thread wc = new ClientRecv("worker_B");
		wc.start();
		System.out.println(wc.getName() + " started!");
	}
}
