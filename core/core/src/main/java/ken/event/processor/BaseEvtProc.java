/**
 * 
 */
package ken.event.processor;

import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

/**
 * @author KennyZJ
 * 
 */
@SuppressWarnings("serial")
public class BaseEvtProc extends BaseRichBolt {
	
	public static Logger log = Logger.getLogger(BaseEvtProc.class);

	OutputCollector _collector;

	@Override
	@SuppressWarnings("rawtypes")
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		_collector = collector;
	}

	@Override
	public void execute(Tuple input) {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

}
