package ken.event.channel;

import java.io.IOException;
import java.util.Map;

import ken.event.EConfig;
import ken.event.Event;
import ken.event.meta.AtomicE;
import ken.event.util.JDKSerializeUtil;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

/**
 * This is the main channel of event adopting, and it has multiple processing
 * ability. Default use ZMQ REQ-REP type to setup a connection to the client
 * Within the instance of this class, the whole ZMQ environment including
 * context and socket is separated from the one in another event source
 * 
 * @author KennyZJ
 * 
 */
@SuppressWarnings("serial")
public class MasterEventChannel extends BaseEventChannel {

	public static Logger LOG = Logger.getLogger(MasterEventChannel.class);

	private ZMQ.Socket _socket;
	private ZMQ.Context _ctx;

	@SuppressWarnings("rawtypes")
	Event evt;

	// private LinkedBlockingQueue<Event> queue;

	int i = 0; // event receive counter

	public MasterEventChannel() {
		super();
	}

	public MasterEventChannel(boolean isDistributed) {
		super(isDistributed);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void nextTuple() {

		LOG.debug("start recv...");

		byte[] request = _socket.recv(0);
		_socket.send(("received by [Thread-"
				+ Thread.currentThread().getId() + "]").getBytes(), 0);

		i++;
		try {
			evt = (Event<AtomicE>) JDKSerializeUtil.getObject(request);
			LOG.debug("No.[" + i + "] event received...with type["
					+ evt.getEvtType() + "]");
			String msgId = (String) evt.getEventID();
			LOG.debug("msgid:" + msgId);
			_collector.emit(new Values(evt), msgId);
		} catch (IOException ioe) {
			LOG.error("easyEDA cannot deserialize input event for reason: "
					+ ioe.getCause());
		} catch (ClassNotFoundException cnfe) {
			LOG.error("easyEDA cannot deserialize input event for reason: "
					+ cnfe.getCause());
		}

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("eventdata"));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		if (_ctx == null) {
			_ctx = ZMQ.context(1);
		}
		if (_socket == null) {
			_socket = _ctx.socket(ZMQ.REP);
		}

		String dest = "tcp://" + conf.get(EConfig.EDA_PIVOT_OUTGOING_HOST)
				+ ":" + conf.get(EConfig.EDA_PIVOT_OUTGOING_PORT);
		LOG.debug("masterEventChannel connecting to: " + dest + "...");
		_socket.connect(dest);
	}

	@Override
	public void ack(Object msgId) {
		// TODO HA work
		LOG.debug(msgId + " acked!");
	}

	@Override
	public void fail(Object msgId) {
		// TODO HA work
	}

	@Override
	public void close() {
		if (_socket != null) {
			_socket.close();
			_socket = null;
		}
		if (this._ctx != null) {
			this._ctx.term();
			this._ctx = null;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage());
		}
	}

}
