package ken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileNIO {

	public static void main(String... args) throws InterruptedException {

		String filefullname = "/Users/KennyZJ/test/test.txt";
		try {
			// FileInputStream fis = new FileInputStream(filefullname);
			File file = new File(filefullname);
			File dir = file.getParentFile();

			dir.mkdirs();
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fout = new FileOutputStream(file);

			//
			FileChannel fc = fout.getChannel();
			FileLock lock = fc.tryLock();

			ByteBuffer buffer = ByteBuffer.allocate(1024);
			
			for (int i = 0; i < 10; i++) {
				buffer.put("test java.nio.ByteBuffer\n".getBytes());
			}
			buffer.flip();

			fc.write(buffer);
			buffer.clear();
			lock.release();
			fc.close();
			fout.close();

			// fc.read(buffer);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
