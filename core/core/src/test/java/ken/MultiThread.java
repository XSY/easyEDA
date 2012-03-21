package ken;

import java.util.concurrent.LinkedBlockingQueue;

public class MultiThread extends Thread {

	public LinkedBlockingQueue<String> _queue;
	public String _myname;

	public MultiThread(LinkedBlockingQueue<String> queue, String myname) {
		super();
		_queue = queue;
		_myname = myname;
	}

	@Override
	public void run() {

		String next;
		while (true) {
			
			try {
				next = _queue.take();
				if (next == null) {
					System.out.println("childThread-[" + _myname
							+ "] is waiting...");
				} else {
					System.out.println("childThread-[" + _myname
							+ "]-(" + _queue.size() + ")" + next);
				}

			} catch (Exception ie) {
				ie.printStackTrace();
			}

		}
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		LinkedBlockingQueue<String> main_queue = new LinkedBlockingQueue<String>();
		MultiThread t1 = new MultiThread(main_queue,"t1");
		t1.start();
		
		MultiThread t2 = new MultiThread(main_queue,"t2");
		t2.start();
		
		
		Thread.sleep(100);

		while (true) {
			main_queue.offer("hello world");
			
			System.out.println("main thread-["+Thread.currentThread().getName()+"] just put!");
			Thread.sleep(1000); // offer much slower than poll
		}

	}

}
