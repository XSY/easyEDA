package ken.event.client;

import ken.event.meta.AtomicE;

/**
 * @author KennyZJ
 * 
 */
public interface EventBox  {
	
	public AtomicE take() throws InterruptedException;
	
	public void put(AtomicE e) throws InterruptedException,	NullPointerException;
	
}
