package easyeda.sample.channel;

import org.apache.log4j.Logger;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import ken.event.channel.FollowerChannel;

@SuppressWarnings("serial")
public class SampleChannel extends FollowerChannel {
	
	public static Logger LOG = Logger.getLogger(SampleChannel.class);
	
	public SampleChannel(String key) {
		super(key);
	}

	public SampleChannel(boolean isDistributed) {
		super(isDistributed);
	}
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("e-data"));
	}
}
