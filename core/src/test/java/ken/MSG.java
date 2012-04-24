/**
 * 
 */
package ken;

/**
 * @author KennyZJ
 * 
 */
public class MSG {

	private byte[] _id;

	private byte[] client; // Sometimes this is for signal use

	private byte[] worker;

	private byte[] msg;

	public MSG() {
		super();
	}

	public MSG(byte[] id) {
		_id = id;
	}

	public byte[] get_id() {
		return _id;
	}

	public void set_id(byte[] _id) {
		this._id = _id;
	}

	public byte[] getClient() {
		return client;
	}

	public void setClient(byte[] client) {
		this.client = client;
	}

	public byte[] getWorker() {
		return worker;
	}

	public void setWorker(byte[] worker) {
		this.worker = worker;
	}

	public byte[] getMsg() {
		return msg;
	}

	public void setMsg(byte[] msg) {
		this.msg = msg;
	}

}
