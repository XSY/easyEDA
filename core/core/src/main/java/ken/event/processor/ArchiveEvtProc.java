package ken.event.processor;

import java.util.Map;

import ken.event.Event;
import ken.event.store.EStore;
import ken.event.store.IEStore;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

/**
 * @author KennyZJ
 * 
 */
@SuppressWarnings("serial")
public class ArchiveEvtProc extends BaseEvtProc {

	public static Logger log = Logger.getLogger(ArchiveEvtProc.class);
	private IEStore store;

	// private enum StoreType{
	// HBase,MySQL
	// }

	@Override
	@SuppressWarnings("rawtypes")
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		// TODO initialize persistence environment, eg. DB, file system
		store = EStore.getStore(EStore.HBASE);
	}

	@Override
	public void execute(Tuple input) {
		log.info("going to store the event...");
		@SuppressWarnings("rawtypes")
		Event evt = (Event) input.get("eventdata");
		log.debug("This event is a [" + evt.getEvtType() + "]");
		try {
			// do persist event
			log.debug("tuple.msgid = " + input.getMessageId());
			store.storeEvent(evt);
			_collector.ack(input);
		} catch (Exception e) {
			_collector.fail(input);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("opt_result"));
	}

	@Override
	public void cleanup() {
		super.cleanup();
		// TODO clean the persistence environment, eg. release DB connection

	}

}
