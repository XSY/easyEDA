package easyeda.sample;

import ken.event.client.adapter.IAdapter;
import ken.event.client.adapter.PatAdmitAdapter;
import ken.event.client.feeder.Feeders;
import ken.event.client.feeder.IFeeder;

/**
 * Event client main process to start an event feeder or follower, allow
 * multiple-type event's feeding each as a runnable thread separately 
 * TODO refactor: use daemon mode manage and control the custom thread by daemon
 * thread
 * 
 * @author KennyZJ
 * 
 */
public class ClientFeeder {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		newClient();
	}

	private static void newClient() {
		IFeeder fe = Feeders.get("HIS_PAT_ADMIT");
		IAdapter adapter = new PatAdmitAdapter();
		fe.setAdapter(adapter);
		
		try {
			fe.startFeeding();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
