/**
 * 
 */
package ken.event.bus;

/**
 * @author KennyZJ
 *
 */
public class Message {
	
	public final static byte[] EMPTY = "".getBytes();
	
	private byte[] id;
	private byte[] part1;
	private byte[] part2;
	private byte[] part3;
	
	public Message(){
		
	}
	
	public Message(byte[] id) {
		super();
		this.id = id;
	}
	public byte[] getId() {
		return id;
	}
	public void setId(byte[] id) {
		this.id = id;
	}
	public byte[] getPart1() {
		return part1;
	}
	public void setPart1(byte[] part1) {
		this.part1 = part1;
	}
	public byte[] getPart2() {
		return part2;
	}
	public void setPart2(byte[] part2) {
		this.part2 = part2;
	}
	public byte[] getPart3() {
		return part3;
	}
	public void setPart3(byte[] part3) {
		this.part3 = part3;
	}
	
	

}
