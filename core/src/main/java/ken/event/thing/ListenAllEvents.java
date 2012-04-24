package ken.event.thing;

import org.apache.log4j.Logger;

import ken.event.EConfig;
import ken.event.channel.MasterEventChannel;
import ken.event.processor.AddressingEvtProc;
import ken.event.processor.ArchiveEvtProc;
import ken.event.processor.RouteEvtProc;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

/**
 * Start event adoption process by executing this class' main method, before
 * that all relevant configuration shall be pre-set
 * 
 * @author KennyZJ
 */
public class ListenAllEvents {
	public static Logger LOG = Logger.getLogger(ListenAllEvents.class);

	public static void main(String[] args) throws Exception {
		Config conf = new Config();
		conf.setDebug(true);
		conf.putAll(EConfig.loadAll()); // add easyEDA's config in

		String thing_n = (String) conf.get(EConfig.EDA_THING_LISTENALL_NAME);
		int thing_p = ((Integer) conf
				.get(EConfig.EDA_THING_LISTENALL_WORKER)).intValue();
		
		String a_n = (String) conf.get(EConfig.EDA_CH_MASTER_NAME);
		int a_p = ((Integer) conf
				.get(EConfig.EDA_CH_MASTER_PARALLEL)).intValue();

		String a1_n = (String) conf.get(EConfig.EDA_PROC_ARC_NAME);
		int a1_p = ((Integer) conf
				.get(EConfig.EDA_PROC_ARC_PARALLEL)).intValue();

		String a2_n = (String) conf.get(EConfig.EDA_PROC_ADDR_NAME);
		int a2_p = ((Integer) conf
				.get(EConfig.EDA_PROC_ARC_PARALLEL)).intValue();

		String a21_n = (String) conf.get(EConfig.EDA_PROC_ROUT_NAME);
		int a21_p = ((Integer) conf
				.get(EConfig.EDA_PROC_ROUT_PARALLEL)).intValue();

		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(a_n, new MasterEventChannel(), a_p);
		//builder.setBolt(a1_n, new ArchiveEvtProc(), a1_p).shuffleGrouping(a_n);
		builder.setBolt(a2_n, new AddressingEvtProc(), a2_p).shuffleGrouping(a_n);
		builder.setBolt(a21_n, new RouteEvtProc(), a21_p).shuffleGrouping(a2_n);

		if (args != null && args.length > 0) {
			conf.setNumWorkers(thing_p);
			StormSubmitter.submitTopology(thing_n, conf,
					builder.createTopology());
		} else {
			LOG.debug("to run new localCluster...");
			LocalCluster cluster = new LocalCluster();
			LOG.debug("new localCluster finished...");
			LOG.debug("to run submitTopology...");
			cluster.submitTopology("Test_ListenAllEvents", conf,
					builder.createTopology());
			LOG.debug("submitTopology finished...");

			// Utils.sleep(10000);
			// cluster.killTopology("Test_ListenAllEvents");
			// LOG.debug("Test_ListenAllEvents was killed...");
			// cluster.shutdown();
			// LOG.debug("localcluster was shutdown...");
		}
	}
}
