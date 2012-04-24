package ken.event.generator;

import java.util.Map;

import ken.event.meta.AtomicE;

import org.zeromq.ZMQ;

/**
 * @author KennyZJ
 *
 */
public abstract class EGenerator {
	
	protected static String dest;
	protected static Map<String, Object> conf;
	
	protected static ZMQ.Context context;
	protected static ZMQ.Socket conn;
	
	/**
	 * build an well-formatted event and send it to event pivot
	 * @param ae
	 * @return
	 */
	public abstract boolean generate(AtomicE ae);
}
