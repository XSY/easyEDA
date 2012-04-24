package ken;

import java.io.IOException;

import ken.event.bus.SocketID;
import ken.event.util.JDKSerializeUtil;

import org.zeromq.ZMQ;

public class TestZMQHA {
	public ZMQ.Context context;
	public ZMQ.Socket socket;
	
	
	public void test() throws IOException{
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.ROUTER);
		socket.bind("tcp://*:5589");
		
		SocketID dest = new SocketID("worker_A","-856742982");
		socket.send(JDKSerializeUtil.getBytes(dest), ZMQ.SNDMORE);
		socket.send("".getBytes(), ZMQ.SNDMORE);
		socket.send("hello".getBytes(), 0);
		System.out.println("sent hello to A");
		System.out.println(socket.recv(0));
		System.out.println(new String(socket.recv(0)));
		System.out.println(new String(socket.recv(0)));
		
		dest = new SocketID("worker_B","-51114146");
		socket.send(JDKSerializeUtil.getBytes(dest), ZMQ.SNDMORE);
		socket.send("".getBytes(), ZMQ.SNDMORE);
		socket.send("hello".getBytes(), 0);
		System.out.println("sent hello to B");
		System.out.println(socket.recv(0));
		System.out.println(new String(socket.recv(0)));
		System.out.println(new String(socket.recv(0)));
//		
		dest = new SocketID("worker_C","-1519369097");
		socket.send(JDKSerializeUtil.getBytes(dest), ZMQ.SNDMORE);
		socket.send("".getBytes(), ZMQ.SNDMORE);
		socket.send("hello".getBytes(), 0);
		System.out.println("sent hello to C");
		System.out.println(socket.recv(0));
		System.out.println(new String(socket.recv(0)));
		System.out.println(new String(socket.recv(0)));
		
		
	}
	
	public static void main(String...args) throws IOException{
		TestZMQHA tester = new TestZMQHA();
		tester.test();
	}
	
}
