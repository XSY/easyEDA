package ken;

import java.io.IOException;

import ken.event.bus.SocketID;
import ken.event.util.JDKSerializeUtil;

public class TestHashcode {

	public static void main(String... strings) throws IOException,
			ClassNotFoundException {

		SocketID obj = new SocketID("fid", "tid");
		byte[] bin_obj = JDKSerializeUtil.getBytes(obj);

		System.out.println("obj.hashCode() = " + obj.hashCode());

		SocketID newobj = (SocketID) JDKSerializeUtil.getObject(bin_obj);
		byte[] bin_newobj = JDKSerializeUtil.getBytes(newobj);
		System.out.println("newobj.hashCode() = " + newobj.hashCode());

		System.out.println("obj.equals(newobj) = " + obj.equals(newobj));
		boolean result = (bin_obj.length == bin_newobj.length);
		if (result) {
			for (int i = 0; i < bin_obj.length; i++) {
				result = result && (bin_obj[i] == bin_newobj[i]);
				System.out.println(bin_obj[i]);
				if (!result) {
					break;
				}
			}
		}
		
		System.out.println("\n"+bin_obj.toString());
		System.out.println(bin_newobj.toString());
		System.out.println("bin_obj equals bin_newobj? " + result);
		
	}
}
