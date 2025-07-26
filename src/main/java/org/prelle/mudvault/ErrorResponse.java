package org.prelle.mudvault;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class ErrorResponse extends MeshMessage {
	
	public static record ErrorPayload(String message, int code, UUID originalMessageId, String details) {
		public ErrorCode getErrorCode() {
			return ErrorCode.getByCode(code);
		}
	}
	
	@Getter
	@Setter
	private ErrorPayload payload;

	//-------------------------------------------------------------------
	public ErrorResponse() {
		type=MessageType.ERROR;
	}

}
