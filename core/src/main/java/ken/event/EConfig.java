package ken.event;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import ken.event.redis.RedisClient;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * @author KennyZJ
 * 
 */
public class EConfig {

	public static Logger log = Logger.getLogger(EConfig.class);

	// public final static int PIVOT_INSIDE_PORT = 5559;
	// public final static int PIVOT_OUTSIDE_PORT = 5560;
	//
	// public final static int PUBLISHER_INSIDE_PORT = 5588;
	// public final static int PUBLISHER_OUTSIDE_PORT = 5589;

	public final static String EDA_PIVOT_INCOMING_HOST = "eda.pivot.incoming.host";
	public final static String EDA_PIVOT_INCOMING_PORT = "eda.pivot.incoming.port";

	public final static String EDA_PIVOT_OUTGOING_HOST = "eda.pivot.outgoing.host";
	public final static String EDA_PIVOT_OUTGOING_PORT = "eda.pivot.outgoing.port";

	public final static String EDA_ROUTER_DIR_HOME = "eda.router.dir.home";
	public final static String EDA_ROUTER_INCOMING_HOST = "eda.router.incoming.host";
	public final static String EDA_ROUTER_OUTGOING_HOST = "eda.router.outgoing.host";
	public final static String EDA_ROUTER_INCOMING_PORT = "eda.router.incoming.port";
	public final static String EDA_ROUTER_OUTGOING_PORT = "eda.router.outgoing.port";
	public final static String EDA_ROUTER_HAND_ASK = "eda.router.hand.ask";
	public final static String EDA_ROUTER_HAND_REPORT = "eda.router.hand.report";
	public final static String EDA_ROUTER_MODE = "eda.router.mode";
	public final static String EDA_ROUTER_SWAPALL_COUNT = "eda.router.swapall.count";
	public final static String EDA_ROUTER_SWAPDONE_COUNT = "eda.router.swapdone.count";
	public final static String EDA_ROUTER_DAEMON_PULSE = "eda.router.daemon.pulse";
	public final static String EDA_ROUTER_MSG_TIMEOUT = "eda.router.msg.timeout";

	public final static String EDA_CH_MASTER_NAME = "eda.channel.master.name";
	public final static String EDA_CH_MASTER_PARALLEL = "eda.channel.master.parallel";

	public final static String EDA_PROC_ARC_NAME = "eda.process.archive.name";
	public final static String EDA_PROC_ARC_PARALLEL = "eda.process.archive.parallel";

	public final static String EDA_PROC_ADDR_NAME = "eda.process.addressing.name";
	public final static String EDA_PROC_ADDR_PARALLEL = "eda.event.addressing.parallel";

	public final static String EDA_PROC_ROUT_NAME = "eda.process.router.name";
	public final static String EDA_PROC_ROUT_PARALLEL = "eda.process.router.parallel";

	public final static String EDA_THING_LISTENALL_NAME = "eda.thing.listenall.name";
	public final static String EDA_THING_LISTENALL_WORKER = "eda.thing.listenall.worker";

	private static String EDA_CONFIG_SELF = "config";
	// static{
	// loadAll();
	// }

	@SuppressWarnings("unchecked")
	public static Map<String, Object> loadAll(String filepath) {
		log.info("loading all configuration items of easyEDA...");
		return findAndReadConfigFile(filepath, true);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> loadAll() {
		RedisClient client = RedisClient.getClient();
		Map<String, Object> conf = client.getHashesAll(EDA_CONFIG_SELF);
		client.returnClient();
		return conf;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map findAndReadConfigFile(String name, boolean mustExist) {
		try {
			Enumeration resources = Thread.currentThread()
					.getContextClassLoader().getResources(name);
			if (!resources.hasMoreElements()) {
				if (mustExist)
					throw new RuntimeException(
							"Could not find config file on classpath " + name);
				else
					return new HashMap();
			}
			URL resource = (URL) resources.nextElement();
			Yaml yaml = new Yaml();
			Map ret = (Map) yaml.load(new InputStreamReader(resource
					.openStream()));
			if (ret == null)
				ret = new HashMap();

			if (resources.hasMoreElements()) {
				throw new RuntimeException("Found multiple " + name
						+ " resources");
			}
			return new HashMap(ret);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// test
//	public static void main(String... args) {
//		//log.debug((Integer) (findAndReadConfigFile("easyEDA.yaml", true)
//		//		.get(EDA_PIVOT_INCOMING_PORT)));
//		EConfig.loadAll();
//	}

}
