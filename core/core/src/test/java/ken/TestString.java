package ken;

public class TestString {
	
	public static void main(String...strings){
		
		String longstr = "1111|111-222222";
		System.out.println(longstr.split("\\|")[0]);
		
		System.out.println(longstr.split("\\-")[1]);
		System.out.printf(System.getProperty("file.encoding"));
	}

}
