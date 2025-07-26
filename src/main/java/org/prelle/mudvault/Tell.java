package org.prelle.mudvault;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class Tell extends MeshMessage {
	
	public static record TellPayload(String message, String formatted) {
	}
	
	@Getter
	@Setter
	private TellPayload payload = new TellPayload(null,null);

	//-------------------------------------------------------------------
	public Tell() {
		type=MessageType.TELL;
	}

}
