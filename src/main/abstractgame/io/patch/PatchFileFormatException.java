package abstractgame.io.patch;

import abstractgame.util.ApplicationException;

public class PatchFileFormatException extends ApplicationException {
	PatchFileFormatException(String error) {
		super(error, "MODEL IO");
	}
	
	PatchFileFormatException(String error, Throwable throwable) {
		super(error, throwable, "MODEL IO");
	}
	
	PatchFileFormatException(Throwable throwable) {
		super(throwable, "MODEL IO");
	}
}
