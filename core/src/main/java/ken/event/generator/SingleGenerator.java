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
 * @author KennyZJ
 * 
 */
public class SingleGenerator extends EGenerator {
	public static Logger LOG = Logger.getLogger(SingleGenerator.class);

	@SuppressWarnings("rawtypes")
	@Override
	public boolean generate(AtomicE ae) {
		conf = EConfig.loadAll();
		dest = "tcp://" + conf.get(EConfig.EDA_PIVOT_INCOMING_HOST) + ":"
				+ conf.get(EConfig.EDA_PIVOT_INCOMING_PORT);
		context = ZMQ.context(1);
		conn = context.socket(ZMQ.REQ);
		conn.connect(dest);
		try {
			Event evt = EventBuilder.buildEvent(ae);
			conn.send(JDKSerializeUtil.getBytes(evt), 0);
			conn.recv(0);
			return true;
		} catch (UnknownHostException e) {
			LOG.debug(e);
		} catch (IOException e) {
			LOG.debug(e);
		} finally {
			conn.close();
			context.term();
		}
		return false;
	}

}
