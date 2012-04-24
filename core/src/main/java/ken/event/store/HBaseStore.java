package ken.event.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ken.event.Event;
import ken.event.repo.HBaseClient;
import ken.event.util.ByteUtil;
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

	private HBaseStore() {
		super();
		init();
	}

	private void init() {
		try {
			hbase = new HBaseClient(EVENT_TABLE_NAME, cfs, true);
		} catch (Exception e) {
			LOG.error(e.getStackTrace());
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void storeEvent(Event evt) {
		Map<String, byte[]> data = new HashMap<String, byte[]>();
		try {
			// TODO qualifier name
			data.put(cfs[0] + ":ats", ByteUtil.long2Bytes(evt.getAwaredTS()));
			if (evt.getLocation() != null && !"".equals(evt.getLocation())) {
				data.put(cfs[0] + ":lc", evt.getLocation().getBytes());
			}
			if (evt.getSource() != null && !"".equals(evt.getSource())) {
				data.put(cfs[0] + ":sc", evt.getSource().getBytes());
			}
			if (evt.getSecurityKey() != null
					&& !"".equals(evt.getSecurityKey())) {
				data.put(cfs[0] + ":sk", evt.getSecurityKey().getBytes());
			}

			data.put(cfs[1] + ":1", JDKSerializeUtil.getBytes(evt.getEvtData()));
			data.put(cfs[2] + ":1", evt.getEvtType().getBytes()); // tp(event
																	// type)
			hbase.putData(((String) evt.getEventID()).getBytes(), data);
		} catch (IOException e) {
			LOG.error(e.getStackTrace());
		}
	}
}
