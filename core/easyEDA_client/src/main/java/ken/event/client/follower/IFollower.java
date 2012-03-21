/**
 * 
 */
package ken.event.client.follower;

import ken.event.client.adapter.IAdapter;

/**
 * @author KennyZJ
 *
 */
public interface IFollower {
	
	public void startFollowing() throws Exception;
	
	public void stopFollowing();

	public void setAdapter(IAdapter a);
	

}
