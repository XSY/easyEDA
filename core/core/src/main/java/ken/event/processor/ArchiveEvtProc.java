package ken.event.processor;

import java.util.Map;

import ken.event.Event;

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

	@Override
	@SuppressWarnings("rawtypes")
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		// TODO initialize persistence environment, eg. DB, file system

	}

	@Override
	public void execute(Tuple input) {
		// TODO Auto-generated method stub
		log.info("going to store the event...");

		@SuppressWarnings("rawtypes")
		Event evt = (Event) input.get("eventdata");

		log.info("This event is a [" + evt.getEvtType() + "]");

		
		try{
			// TODO do persist event
			//_collector.emit(input, new Values("Archived!"));
			
			_collector.ack(input);
			
		}catch(Exception e){
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
