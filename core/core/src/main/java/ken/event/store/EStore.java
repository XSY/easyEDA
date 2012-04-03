package ken.event.store;

/**
 * @author KennyZJ
 * 
 */
public class EStore {

	public static final int HBASE = 0;
	public static final int MYSQL = 1;

	public static IEStore getStore(int storeType) {
		IEStore store;
		switch (storeType) {
		case 0:
			store = HBaseStore.create();
			break;
		case 1:
			store = null;
			break;
		default:
			store = HBaseStore.create();
			break;
		}
		return store;
	}

}
