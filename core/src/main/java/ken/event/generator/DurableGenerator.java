package ken.event.generator;

import java.io.IOException;
import java.net.UnknownHostException;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.meta.AtomicE;
import ken.event.util.EventBuilder;
import ken.event.util.JDKSerializeUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

/**
 * DurableGenerator is for event happening in high-frequency way, so that the
 * connection is not close each time it ends, but must manually call method done().
 * @author KennyZJ
 * 
 */
public class DurableGenerator extends EGenerator {
	public static Logger LOG = Logger.getLogger(DurableGenerator.class);

	public DurableGenerator() {
		conf = EConfig.loadAll();
		dest = "tcp://" + conf.get(EConfig.EDA_PIVOT_INCOMING_HOST) + ":"
				+ conf.get(EConfig.EDA_PIVOT_INCOMING_PORT);
		context = ZMQ.context(1);
		conn = context.socket(ZMQ.REQ);
		conn.connect(dest);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean generate(AtomicE ae) {
		try {
			Event evt = EventBuilder.buildEvent(ae);
			conn.send(JDKSerializeUtil.getBytes(evt), 0);
			conn.recv(0);
			return true;
		} catch (UnknownHostException e) {
			LOG.debug(e);
		} catch (IOException e) {
			LOG.debug(e);
		}
		return false;
	}

	/**
	 * must be invoked, if use DurableGenerator
	 */
	public void done() {
		conf = null;
		dest = null;
		conn.close();
		context.term();
	}

}
