package ken.event.processor;

import java.util.Map;
import java.util.Set;

import ken.event.Event;
import ken.event.management.ELocator;

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

	//private ConcurrentHashMap<String, List<Follower>> eventMap;

	private ELocator locator;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		locator = ELocator.getLocator(ELocator.Redis);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Tuple input) {
		Event evt = (Event) input.get("eventdata");
		if (evt != null) {
			String evtType = evt.getEvtType();
			Set<String> followers = locator.lookup(evtType);
			// _collector.emit(input, new Values(evt, followers));

			// untouched the anchor tuple, if addressing complete and tuple
			// has been sent to routerevtProc, then let it be sent to router
			if (followers != null && followers.size() > 0) {
				_collector.emit(new Values(evt, followers));
				log.debug("AddressingEvtProc is emitting [" + evtType
						+ "] event to [" + followers.size() + "] followers");
			} else {
				log.warn("event type [" + evtType + "] has no follower.");
			}
			followers.clear();
			_collector.ack(input);
		} else {
			log.warn("No event passed here!");
			_collector.fail(input);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("eventdata", "followers"));
	}

	@Override
	public void cleanup() {
		super.cleanup();
		locator.release();
	}

//	private void loadEventMap() {
//		// TODO This is for test, needs talk to actual data
//		ConcurrentHashMap<String, List<Follower>> result = new ConcurrentHashMap<String, List<Follower>>();
//		ArrayList<Follower> followers = new ArrayList<Follower>();
//		followers.add(new Follower("MPI_FO", "mpifollower", "EMR_platform"));
//		result.put(EventConstants.PA_E_TYPE, followers);
//		eventMap = result;
//	}

}
