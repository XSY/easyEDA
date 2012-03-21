package ken.event.client;

import ken.event.client.adapter.EchoAdapter;
import ken.event.client.adapter.IAdapter;
import ken.event.client.follower.Followers;
import ken.event.client.follower.IFollower;

/**
 * simply perform an echo of each event routed to client
 * 
 * @author KennyZJ
 * 
 */
public class ClientFollow {

	public static void main(String... args) {
		// String[] keys = args;
		// if (args == null || args.length < 1) {
		// keys = new String[] { "MPI_FO" };
		// }else{
		// IFollower[] fs;
		// }
		// for(String s:args){
		//
		// }

		IFollower f1 = Followers.get("MPI_FO");// MPI follower by EMR platform,
												// will be got from client local
												// config file
		IAdapter a1 = new EchoAdapter();

		try {
			f1.setAdapter(a1);
			f1.startFollowing();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
