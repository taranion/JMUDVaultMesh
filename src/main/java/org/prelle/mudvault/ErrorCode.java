package org.prelle.mudvault;

/**
 * 
 */
public enum ErrorCode {
	INVALID_MESSAGE(1000),
	AUTHENTICATION_FAILED(1001),
	UNAUTHORIZED(1002),
	MUD_NOT_FOUND(1003),
	USER_NOT_FOUND(1004),
	CHANNEL_NOT_FOUND(1005),
	RATE_LIMITED(1006),
	INTERNAL_ERROR(1007),
	PROTOCOL_ERROR(1008),
	UNSUPPORTED_VERSION(1009),
	MESSAGE_TOO_LARGE(1010),
	;
	
	int code;
	ErrorCode(int value) {
		this.code=value;
	}
	
	//-------------------------------------------------------------------
	public static ErrorCode getByCode(int code) {
		for (ErrorCode tmp : ErrorCode.values()) {
			if (tmp.code==code) return tmp;
		}
		return null;
	}

}
