package Dispatch;

class Dispatchable {
	String exchangeName;
	byte[] body;

	public Dispatchable(String exchangeName, byte[] body) {
		this.exchangeName = exchangeName;
		this.body = body;
	}
}
