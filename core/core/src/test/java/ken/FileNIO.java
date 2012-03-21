package ken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileNIO {

	public static void main(String... args) {

		String filefullname = "/Users/KennyZJ/Develop/source/Java/easyEDA/core/core/testNIO.txt";
		try {
			// FileInputStream fis = new FileInputStream(filefullname);
			File file = new File(filefullname);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);

			//
			FileChannel fc = fos.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024000);

			for (int i = 0; i < 100; i++) {
				buffer.put("test java.nio.ByteBuffer\n".getBytes());
			}
			buffer.flip();

			fc.write(buffer);
			buffer.clear();
			
			fc.read(buffer);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
