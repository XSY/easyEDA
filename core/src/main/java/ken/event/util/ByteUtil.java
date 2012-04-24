package ken.event.util;

import java.nio.ByteBuffer;

public class ByteUtil {

	public static byte[] long2Bytes(long value) {
		byte b[] = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(value);
		buf.clear();
		return b;
	}

	public static long bytes2long(byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b);
		return buf.getLong();
	}

	public static void main(String... strings) {
		long now = System.currentTimeMillis();
		System.out.println(now);
		byte[] b = ByteUtil.long2Bytes(now);
		long newlong = ByteUtil.bytes2long(b);
		System.out.println(newlong);
	}
}
