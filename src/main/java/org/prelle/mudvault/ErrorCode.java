package org.prelle.mudvault;

/**
 * 
 */
public enum ErrorCode {
	GENERAL_ERROR(1000),
	MISSING_MUD_NAME(1001),
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
