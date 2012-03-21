package ken.event.client.adapter;

import ken.event.client.EventBox;

/**
 * @author KennyZJ
 *
 */
public interface IAdapter {
	
	/**
	 * Generally you are not supposed to use this method manually. 
	 */
	public void startAdapter();
	
	/**
	 * Just perform as a producer producing local events or a consumer consuming events that others made.
	 * This method supposes to be running continuously.
	 * @throws InterruptedException
	 */
	public void doAdapt() throws InterruptedException;

	/**
	 * Generally you are not supposed to use this method manually.
	 * @param box
	 */
	public void setFollowStream(EventBox box);
	
	/**
	 * Generally you are not supposed to use this method manually.
	 * @param box
	 */
	public void setFeedStream(EventBox box);
	
}
