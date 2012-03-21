package ken.event.client.feeder;

import ken.event.client.adapter.IAdapter;

/**
 * @author KennyZJ
 * 
 */
public interface IFeeder {
	
	public void startFeeding() throws Exception;

	public void stopFeeding();

	public void setAdapter(IAdapter a);

}
