package idawi.messaging;

public class ProgressMessage extends ProgressInformation {
	final public String msg;

	public ProgressMessage(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return msg;
	}
}