package ken.event.repo;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class HBaseClient {
	public static Logger LOG = Logger.getLogger(HBaseClient.class);

	private String _tname;
	private String[] _cfs;// pf:profile, pl:payload

	private static Configuration _conf; // one single _conf per HBaseClient

	private HBaseAdmin admin;
	private HTable table;

	public HBaseClient(String tablename, String[] cfs) throws Exception {
		this(tablename, cfs, true);
	}

	public HBaseClient(String tablename, String[] cfs, boolean isAlone)
			throws Exception {
		_tname = tablename;
		_cfs = cfs;
		_conf = HBaseConfiguration.create();
		try {
			init();
		} catch (MasterNotRunningException e) {
			LOG.error("construct HBaseClient failed for master not running exception "
					+ e.getStackTrace());
			throw new Exception(e);
		} catch (ZooKeeperConnectionException e1) {
			LOG.error("construct HBaseClient failed for zookeeper connection exception "
					+ e1.getStackTrace());
			throw new Exception(e1);
		} catch (IOException ioe) {
			LOG.error("construct HBaseClient failed for ioexception "
					+ ioe.getStackTrace());
			throw new Exception(ioe);
		}
	}

	private void init() throws IOException {
		admin = new HBaseAdmin(_conf);
		checkTable();
		table = new HTable(_conf, _tname);
	}
	
	public void checkTable() throws IOException {
		if (admin.tableExists(_tname)) {
			LOG.info("table: [" + _tname + "] already exist. Checked OK!");
		} else {
			LOG.info("table: [" + _tname
					+ "] does not exist, now creating...");
			HTableDescriptor tableDesc = new HTableDescriptor(_tname);
			for (int i = 0; i < _cfs.length; i++) {
				HColumnDescriptor cf = new HColumnDescriptor(_cfs[i]);
				tableDesc.addFamily(cf);
			}
			admin.createTable(tableDesc);
			LOG.info("table:[" + _tname + "] created. ");
		}
	}
	
	public void dropTable() throws IOException {
		admin.disableTable(_tname);
		admin.deleteTable(_tname);
	}
	
	public void putData(byte[] rowKey, Map<String, byte[]> data)
			throws IOException {
		if (_tname == null || "".equals(_tname)) {
			LOG.warn("No Target table: " + _tname + " specified!");
			return;
		} else {
			Put put = new Put(rowKey);
			Set<String> cfs = data.keySet();
			for (String cf : cfs) {
				String[] col = cf.split(":");
				put.add(Bytes.toBytes(col[0]), Bytes.toBytes(col[1]),
						data.get(cf));
			}
			table.put(put);
		}
	}

	public Result getData(){
		Get get = new Get();
		Result result = new Result();
		return result;
	}
	
	

	public static void main(String[] args) throws Exception {
		// HBaseClient hbase = new HBaseClient();
		// hbase.initRepo("ee", new String[] { "pf", "pl" });
		// hbase.dropTable(EVENT_TABLE_NAME);
	}
}
