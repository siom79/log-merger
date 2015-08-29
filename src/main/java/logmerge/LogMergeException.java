package logmerge;

public class LogMergeException extends RuntimeException {
	private final Reason reason;

	public enum Reason {
		FileNotFound, IOException, UnsupportedEncoding, CliParser
	}

	public LogMergeException(Reason reason, String msg, Throwable t) {
		super(msg, t);
		this.reason = reason;
	}

	public LogMergeException(Reason reason, String msg) {
		super(msg);
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}
}
