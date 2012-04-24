package ken;

import java.io.FileNotFoundException;
import java.io.IOException;

import ken.event.bus.Message;
import ken.event.bus.SocketID;
import ken.event.util.FileUtil;

public class FileUtilTest {

	public static void main(String... strings) throws Exception {
		// testIOwriteAppend();
		// testNIOwriteAppend();
		// testIOwriteNewFile();
		// testMoveFiles();
		// testNIOwriteNewFile();
		// testMoveFile();
		// testIsDirEmpty();
		// testMoveDirToDir();
		// testMoveFileToDir();
		// testMoveDirContent();
		testReadMsgFile();
	}
	
	private static void testReadMsgFile() throws IOException{
		Message msg = new Message();
		FileUtil.readMsgFile("/Users/KennyZJ/router/waiting/e099ce0c-b7c9-4b7c-b4f2-9f733f2730a4.msg", msg);
		System.out.println("msg id = "+new String(msg.getId()));
	}

	private static void testMoveFileToDir() throws IOException {
		FileUtil.moveFileToDir("/Users/KennyZJ/test/src/1.msg",
				"/Users/KennyZJ/test/dest");
	}

	private static void testMoveDirContent() throws IOException {
		FileUtil.moveDirContent("/Users/KennyZJ/test/src/", "/Users/KennyZJ/test/dest/",
				".msg");
	}

	private static void testMoveDirToDir() throws IOException {
		FileUtil
				.moveTo("/Users/KennyZJ/test/src", "/Users/KennyZJ/test/dest");
	}

	private static void testMoveFile() throws IOException {
		FileUtil.moveTo("/Users/KennyZJ/test/src/1",
				"/Users/KennyZJ/test/dest");
	}

	private static void testIsDirEmpty() throws FileNotFoundException {
		System.out
				.println(FileUtil.isDirEmpty("/Users/KennyZJ/test", ".msg"));
	}

	private static void testIOwriteAppend() throws IOException {
		SocketID input = new SocketID("fid", "tid");
		StringBuilder sb = new StringBuilder();
		long start = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			sb.append(input.getFollower_key());
			sb.append("|");
			sb.append(input.getThreadID());
			sb.append("|");
			sb.append("N");
			FileUtil.writeLine(sb.toString().getBytes(),
					"/Users/KennyZJ/test", "workerIO.txt");
			sb.setLength(0);
		}
		System.out.println("Java IO write 1000 workers to one file in ["
				+ (System.nanoTime() - start) + "] nanosec");
	}

	private static void testNIOwriteAppend() {
		SocketID input = new SocketID("fid", "tid");
		StringBuilder sb = new StringBuilder();
		long start = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			sb.append(input.getFollower_key());
			sb.append("|");
			sb.append(input.getThreadID());
			sb.append("|");
			sb.append("N");
			FileUtil.writeLineNIO(sb.toString().getBytes(),
					"/Users/KennyZJ/test", "workerNIO.txt");
			sb.setLength(0);
		}
		System.out.println("Java NIO write 1000 workers to one file in ["
				+ (System.nanoTime() - start) + "] nanosec");
	}

	private static void testIOwriteNewFile() throws IOException {
		SocketID input = new SocketID("fid", "tid");
		StringBuilder sb = new StringBuilder();
		long start = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			sb.append(input.getFollower_key());
			sb.append("|");
			sb.append(input.getThreadID());
			sb.append("|");
			sb.append("N");
			FileUtil.writeLine(sb.toString().getBytes(),
					"/Users/KennyZJ/test", "worker-io-" + i + ".txt");
			sb.setLength(0);
		}
		System.out.println("Java IO write 1000 workers to new file in ["
				+ (System.nanoTime() - start) + "] nanosec");
	}

	private static void testMoveFiles() throws IOException {
		long start = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			FileUtil.moveTo("/Users/KennyZJ/test/worker-io-" + i + ".txt",
					"/Users/KennyZJ/moved/");
		}
		System.out.println("move 1000 workers to new dir in ["
				+ (System.nanoTime() - start) + "] nanosec");
	}

	private static void testNIOwriteNewFile() {
		SocketID input = new SocketID("fid", "tid");
		StringBuilder sb = new StringBuilder();
		long start = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			sb.append(input.getFollower_key());
			sb.append("|");
			sb.append(input.getThreadID());
			sb.append("|");
			sb.append("N");
			FileUtil.writeLineNIO(sb.toString().getBytes(),
					"/Users/KennyZJ/test", "worker-nio-" + i + ".txt");
			sb.setLength(0);
		}
		System.out.println("Java NIO write 1000 workers to new file in ["
				+ (System.nanoTime() - start) + "] nanosec");
	}

}
