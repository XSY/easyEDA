package ken.event.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ken.event.Event;
import ken.event.repo.HBaseClient;
import ken.event.util.JDKSerializeUtil;

/**
 * @author KennyZJ
 * 
 */
public class HBaseStore implements IEStore {

	public static Logger LOG = Logger.getLogger(HBaseStore.class);
	private HBaseClient hbase;
	private static final String EVENT_TABLE_NAME = "eda_e";
	private static String[] cfs = { "pf", "pl", "tp" };// pf:profile,
														// pl:payload, tp:event
														// type

	public static IEStore create() {
		return new HBaseStore();
	}

	protected HBaseStore() {
		super();
		init();
	}

	private void init() {
		try {
			hbase = new HBaseClient(true);
		} catch (Exception e1) {
			LOG.error(e1.getStackTrace());
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void storeEvent(Event evt) {
		Map<String, byte[]> data = new HashMap<String, byte[]>();
		try {
			data.put(cfs[0] + ":1", evt.getEvtType().getBytes());// just for demo
			data.put(cfs[1] + ":1", JDKSerializeUtil.getBytes(evt.getEvtData()));
			hbase.putData(EVENT_TABLE_NAME,
					((String) evt.getEventID()).getBytes(), data);
		} catch (IOException e) {
			LOG.error(e.getStackTrace());
		}
	}
}
