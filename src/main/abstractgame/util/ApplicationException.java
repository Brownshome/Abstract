package abstractgame.util;

public class ApplicationException extends RuntimeException {
	public String section = "UNKNOWN";
	
	public ApplicationException(String message, Throwable throwable, String section) {
		super(message, throwable);
		this.section = section;
	}

	public ApplicationException(String message, String section) {
		super(message);
		this.section = section;
	}

	public ApplicationException(Throwable throwable, String section) {
		super("An unexpected error has occured", throwable);
		this.section = section;
	}

	public ApplicationException() {
		super();
	}

	public ApplicationException(String message, Throwable t) {
		super(message, t);
	}
}
