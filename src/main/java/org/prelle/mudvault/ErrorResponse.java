package org.prelle.mudvault;

import lombok.Getter;

/**
 * 
 */
public class ErrorResponse extends MeshMessage {
	
	public static record ErrorPayload(String message, int code, String originalMessageId) {
		public ErrorCode getErrorCode() {
			return ErrorCode.getByCode(code);
		}
	}
	
	@Getter
	private ErrorPayload payload;

}
