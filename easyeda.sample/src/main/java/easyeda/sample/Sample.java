package easyeda.sample;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

/**
 * @author KennyZJ
 *
 */
public class Sample {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
//		TopologyBuilder builder = new TopologyBuilder();
//		builder.setBolt("", new )
		
		Config conf = new Config();
		conf.setDebug(true);
		
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("sample_thing", conf,
				builder.createTopology());
		
		Thread.sleep(10000);
		cluster.killTopology("Test_ListenAllEvents");
		 cluster.shutdown();
		
	}

}
