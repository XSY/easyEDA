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
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class HBaseClient {
	public static Logger LOG = Logger.getLogger(HBaseClient.class);

	private static final String EVENT_TABLE_NAME = "eda_e";
	private static String[] cfs = { "pf", "pl", "tp" };// pf:profile, pl:payload

	private static Configuration _conf = new Configuration();

	private HBaseAdmin admin;
	private HTable tb_e;

	public static void main(String[] args) throws Exception {
		HBaseClient hbase = new HBaseClient();
		hbase.initRepo("ee", new String[] { "pf", "pl" });
		// hbase.dropTable("ee");
	}

	private HBaseClient() {
	}

	public HBaseClient(boolean isAlone) throws Exception {
		_conf = HBaseConfiguration.create();
		try {
			initRepo(EVENT_TABLE_NAME, cfs);
		} catch (MasterNotRunningException e) {
			LOG.error("construct HBaseClient failed! " + e.getStackTrace());
			throw new Exception(e);
		} catch (ZooKeeperConnectionException e1) {
			LOG.error("construct HBaseClient failed!" + e1.getStackTrace());
			throw new Exception(e1);
		} catch (IOException ioe) {
			LOG.error("construct HBaseClient failed!" + ioe.getStackTrace());
			throw new Exception(ioe);
		}
	}

	private void initRepo(String tablename, String[] cfs) throws IOException {
		admin = new HBaseAdmin(_conf);
		createTable(tablename, cfs);
		tb_e = new HTable(_conf, tablename);
	}

	public void putData(String tablename, byte[] rowKey,
			Map<String, byte[]> data) throws IOException {
		if (tablename == null || "".equals(tablename)) {
			LOG.warn("No Target table: specified!");
			return;
		} else if (tablename.equals(EVENT_TABLE_NAME)) {
			Put put = new Put(rowKey);
			Set<String> cfs = data.keySet();
			for (String cf : cfs) {
				String[] col = cf.split(":");
				put.add(Bytes.toBytes(col[0]), Bytes.toBytes(col[1]),
						data.get(cf));
			}
			tb_e.put(put);
		} else {
			// other table opt, implements later
		}
	}

	public void createTable(String tablename, String[] cfs) throws IOException {
		if (admin.tableExists(tablename)) {
			LOG.info("table: [" + tablename + "] already exist. Checked OK!");
		} else {
			LOG.info("table: [" + tablename
					+ "] does not exist, now creating...");
			HTableDescriptor tableDesc = new HTableDescriptor(tablename);
			for (int i = 0; i < cfs.length; i++) {
				HColumnDescriptor cf = new HColumnDescriptor(cfs[i]);
				tableDesc.addFamily(cf);
			}
			admin.createTable(tableDesc);
			LOG.info("table:[" + tablename + "] created. ");
		}
	}

	public void dropTable(String tablename) throws IOException {
		admin.disableTable(tablename);
		admin.deleteTable(tablename);
	}

}
