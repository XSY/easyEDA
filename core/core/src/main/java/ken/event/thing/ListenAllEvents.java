/**
 * 
 */
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
import backtype.storm.utils.Utils;

/**
 * Start event adoption process by executing this class' main method, before
 * that all relevant configuration can be pre-set
 * 
 * @author KennyZJ
 */
public class ListenAllEvents {
	public static Logger LOG = Logger.getLogger(ListenAllEvents.class);

	public static void main(String[] args) throws Exception {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("Recv_All_Events", new MasterEventChannel(), 2);
		builder.setBolt("Archive_All_Events", new ArchiveEvtProc(), 3)
				.shuffleGrouping("Recv_All_Events");
		builder.setBolt("Address_All_Events", new AddressingEvtProc(), 3)
				.shuffleGrouping("Recv_All_Events");
		builder.setBolt("Route_All_Events", new RouteEvtProc(), 2)
				.shuffleGrouping("Address_All_Events");

		Config conf = new Config();
		conf.setDebug(true);
		conf.putAll(EConfig.loadAll()); //add easyEDA's config in

		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopology("easyEDA_ListenAllEvents", conf,
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
