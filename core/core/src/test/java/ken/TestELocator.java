package ken;

import java.util.Set;

import ken.event.management.ELocator;

public class TestELocator {

	public static void main(String...strings){
		ELocator locator = ELocator.getLocator(ELocator.Redis);
		Set<String> followers = locator.lookup("PatientAdmitting");
		for(String follower:followers){
			System.out.println(follower);
		}
		locator.release();
		locator.lookup("PatientAdmitting");
	}
}
