package ken.event.channel;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;


/**
 * @author KennyZJ
 * 
 */

public class BaseEventChannel extends BaseRichSpout{

	private static final long serialVersionUID = -3794595135982363568L;
	
	public static Logger LOG = Logger.getLogger(BaseEventChannel.class);
	boolean _isDistributed;
	
	SpoutOutputCollector _collector;
	
	public BaseEventChannel(){
		this(true);
	}
	
	public BaseEventChannel(boolean isDistributed){
		_isDistributed = isDistributed;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {}
	
	@Override
	public void nextTuple() {}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		_collector = collector;
	}
	
	@Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> ret = new HashMap<String, Object>();
        if(!_isDistributed) {
            ret.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);
        }
        return ret;
    }    
	
	
}
