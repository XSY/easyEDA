package ken.event.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ken.event.Event;
import ken.event.Follower;
import ken.event.meta.EventConstants;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * @author KennyZJ
 * 
 */
@SuppressWarnings("serial")
public class AddressingEvtProc extends BaseEvtProc {

	public static Logger log = Logger.getLogger(AddressingEvtProc.class);

	private ConcurrentHashMap<String, List<Follower>> eventMap;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		loadEventMap();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Tuple input) {
		Event evt = (Event) input.get("eventdata");
		if (evt != null) {
			String evtType = evt.getEvtType();
			List<Follower> followers = eventMap.get(evtType);
			_collector.emit(input, new Values(evt, followers));
			log.info("in AddressingEvtProc emitted: " + evt.getEvtType());
			_collector.ack(input);
		} else {
			log.warn("event can't be handled with unknown eventType");
			_collector.fail(input);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("eventdata", "followers"));
	}

	private void loadEventMap() {
		// TODO This is for test, needs talk to real data
		ConcurrentHashMap<String, List<Follower>> result = new ConcurrentHashMap<String, List<Follower>>();
		ArrayList<Follower> followers = new ArrayList<Follower>();
		followers.add(new Follower("MPI_FO", "mpifollower", "EMR_platform"));
		result.put(EventConstants.PA_E_TYPE, followers);
		eventMap = result;
	}

}
