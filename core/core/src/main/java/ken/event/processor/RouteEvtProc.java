package ken.event.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.Follower;
import ken.event.util.JDKSerializeUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

/**
 * @author KennyZJ
 * 
 */
@SuppressWarnings("serial")
public class RouteEvtProc extends BaseEvtProc {

	public static Logger log = Logger.getLogger(RouteEvtProc.class);

	ZMQ.Context zmq_ctx;
	ZMQ.Socket sender;
	private static String pub_dest;

	private Event evt;
	private List<Follower> followers = new ArrayList<Follower>();
	private String _sender_id;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		pub_dest = "tcp://" + stormConf.get(EConfig.EDA_ROUTER_INCOMING_HOST)
				+ ":" + stormConf.get(EConfig.EDA_ROUTER_INCOMING_PORT);
		initZMQ(stormConf, context);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void execute(Tuple input) {
		log.debug("routerevtProc start execute...");
		try {
			followers.addAll((List<Follower>) input.get("followers"));
			log.debug("followers.size() = " + followers.size());
			evt = (Event) input.get("eventdata");
			if (sender != null && followers != null && followers.size() > 0) {
				for (Follower fo : followers) {
					sendToRouter(evt, fo.getId());
				}
			}
			followers.clear();
			_collector.ack(input);
		} catch (Exception e) {
			log.error(e.getMessage());
			_collector.fail(input);
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();
		if (sender != null) {
			sender.close();
			sender = null;
		}
		if (this.zmq_ctx != null) {
			this.zmq_ctx.term();
			this.zmq_ctx = null;
		}
		try {
			Thread.sleep(1000); // need time to release
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	@SuppressWarnings("rawtypes")
	private void initZMQ(Map stormConf, TopologyContext context) {
		zmq_ctx = ZMQ.context(1);
		sender = zmq_ctx.socket(ZMQ.REQ);
		// generate sender identity with format
		// [storm_ID]-[component_ID]-[task_ID]
		_sender_id = String.format("%s-%s-%d", context.getStormId(),
				context.getThisComponentId(), context.getThisTaskId());
		sender.setIdentity(_sender_id.getBytes());
		sender.connect(pub_dest);
		log.info("event publish bus connected to" + pub_dest);
	}

	@SuppressWarnings("rawtypes")
	private void sendToRouter(Event data, String dest) throws IOException {
		sender.send(dest.getBytes(), ZMQ.SNDMORE);
		sender.send("".getBytes(), ZMQ.SNDMORE);
		byte[] to_send = JDKSerializeUtil.getBytes(data);

		log.debug("to_send.length = " + to_send.length);
		sender.send(to_send, 0);
		log.debug("in RouteEvtProc: receive from router: "
				+ (new String(sender.recv(0))));
	}

}