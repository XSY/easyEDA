package ken.event.client.adapter;

import org.apache.log4j.Logger;

import ken.event.meta.PatAdmitEvent;

/**
 * @author KennyZJ
 * 
 */
public class EchoAdapter extends Adapter {

	public static Logger LOG = Logger.getLogger(EchoAdapter.class);

	@Override
	public void doAdapt() throws InterruptedException {

		while (true) {
			PatAdmitEvent e = (PatAdmitEvent) _box.take();
			// do sth to e
			LOG.info("[From follower]----patient[" + e.getpName()
					+ "] was just now admitted into this hospital.");
		}
	}

}
