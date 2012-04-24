package ken.event.util;

import java.io.IOException;

import ken.MSG;
import ken.event.bus.Message;
import ken.event.bus.SocketID;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * @author KennyZJ
 * 
 */
public class ZMQUtil {
	public static void send(Message msg, Socket socket) {
		socket.send(msg.getPart1(), ZMQ.SNDMORE);
		socket.send(Message.EMPTY, ZMQ.SNDMORE);
		socket.send(msg.getPart2(), ZMQ.SNDMORE);
		socket.send(Message.EMPTY, ZMQ.SNDMORE);
		socket.send(msg.getPart3(), 0);
	}

	public static Message receive(Socket socket) {
		Message msg = new Message(genMsgID());
		msg.setPart1(socket.recv(0));
		socket.recv(0);
		msg.setPart2(socket.recv(0));
		socket.recv(0);
		msg.setPart3(socket.recv(0));
		return msg;
	}

//	public static MSG recvFromFront(Socket socket) {
//		MSG msg = new MSG(genMsgID());
//		msg.setClient(socket.recv(0));
//		socket.recv(0);
//		msg.setWorker(socket.recv(0));
//		socket.recv(0);
//		msg.setMsg(socket.recv(0));
//		return msg;
//	}

//	/**
//	 * This is used for the input socket which has set identity by ZMQ automatically
//	 * 
//	 * @param socket
//	 * @param msg
//	 */
//	public static void sendDirectly(Socket socket, MSG msg) {
//		socket.send(msg.getClient(), ZMQ.SNDMORE);
//		socket.send("".getBytes(), ZMQ.SNDMORE);
//		socket.send(msg.getMsg(), 0);
//	}
//
//	public static void send(Socket socket, MSG msg) {
//		// 1.set worker address
//		socket.send(msg.getWorker(), ZMQ.SNDMORE);
//		socket.send("".getBytes(), ZMQ.SNDMORE);
//		// 2.set client address
//		socket.send(msg.getClient(), ZMQ.SNDMORE);
//		socket.send("".getBytes(), ZMQ.SNDMORE);
//		// 3.set msg id
//		socket.send(msg.get_id(), ZMQ.SNDMORE);
//		socket.send("".getBytes(), ZMQ.SNDMORE);
//		// 4.set request payload
//		socket.send(msg.getMsg(), 0);
//	}

	public static void report(Socket socket, String word) {
		if (word == null) {
			throw new NullPointerException("no hand word passed in!");
		}
		socket.send(word.getBytes(), 0);
	}

	private static byte[] genMsgID() {
		return java.util.UUID.randomUUID().toString().getBytes();
	}

	public static void ask(Socket socket, SocketID socketID, String ask)
			throws IOException {
		if (socket.getType() != ZMQ.ROUTER) {
			throw new IOException("you cannot ask because of the SocketType:"
					+ socket.getType());
		}
		
		System.out
				.println("asking sockketID:fkey=" + socketID.getFollower_key()
						+ "|tid=" + socketID.getThreadID());

		System.out.println(socketID.getFollower_key() + ".length="
				+ socketID.getFollower_key().length());
		System.out.println(socketID.getThreadID() + ".length="
				+ socketID.getThreadID().length());

		socket.send(JDKSerializeUtil.getBytes(socketID), ZMQ.SNDMORE);
		socket.send("".getBytes(), ZMQ.SNDMORE);
		socket.send(ask.getBytes(), 0);

	}

	public static void ask(Socket socket, String socketID, String ask)
			throws IOException {
		if (socket.getType() != ZMQ.ROUTER) {
			throw new IOException("you cannot ask because of the SocketType:"
					+ socket.getType());
		}
		socket.send(socketID.getBytes(), ZMQ.SNDMORE);
		socket.send("".getBytes(), ZMQ.SNDMORE);
		socket.send(ask.getBytes(), 0);
	}

}
