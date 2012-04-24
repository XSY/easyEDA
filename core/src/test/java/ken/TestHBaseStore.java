package ken;

import java.net.UnknownHostException;

import junit.framework.TestCase;
import ken.event.Event;
import ken.event.meta.PatAdmitEvent;
import ken.event.store.EStore;
import ken.event.store.IEStore;
import ken.event.util.EventBuilder;

public class TestHBaseStore extends TestCase {
	IEStore store;

	protected void setUp() throws Exception {
		super.setUp();
		store = EStore.getStore(EStore.HBASE);
	}

	public void testStoreEvent() {
		PatAdmitEvent pae = new PatAdmitEvent();
		pae.setpName("testPatient");
		Event e;
		try {
			e = EventBuilder.buildEvent(pae);
			store.storeEvent(e);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
