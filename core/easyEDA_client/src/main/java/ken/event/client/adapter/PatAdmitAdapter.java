package ken.event.client.adapter;

import java.util.Random;

import ken.event.meta.PatAdmitEvent;

import org.apache.log4j.Logger;

/**
 * @author KennyZJ
 * 
 */
public class PatAdmitAdapter extends Adapter {

	public static Logger LOG = Logger.getLogger(PatAdmitAdapter.class);

	private final String[] pNames = new String[] { "张三", "李四", "王二", "刘五", "赵六" };
	private final String[] pCard = new String[] { "YB000001", "YB000002",
			"SB000001", "ZF103098", "SB000002" };
	private final String[] depts = new String[] { "普内科", "普外科", "消化科", "眼科",
			"骨科" };

	@Override
	public void doAdapt() throws InterruptedException {
		PatAdmitEvent pae;
		while (true) {
			pae = mockPatientAdmitting();
			_box.put(pae);
			LOG.debug("--From feeder--patient[" + pae.getpName()
					+ "]has was just now admitted into this hospital.");
			Thread.sleep(1000);
		}
	}

	private PatAdmitEvent mockPatientAdmitting() {
		PatAdmitEvent event = new PatAdmitEvent();

		final Random rand = new Random();
		int indexP = rand.nextInt(pNames.length);
		int indexDept = rand.nextInt(depts.length);

		event.setpName(pNames[indexP]);
		event.setpCard(pCard[indexP]);
		event.setDept(depts[indexDept]);
		event.setOptID("tester");
		event.setAdmitTS(System.currentTimeMillis());

		return event;
	}
}
