package org.prelle.mudvault;

import lombok.Getter;

/**
 * 
 */
public class Who extends MeshMessage {
	
	public static record WhoPayload() {
	}
	
	@Getter
	private WhoPayload payload = new WhoPayload();

	//-------------------------------------------------------------------
	public Who() {
		type=MessageType.WHO;
	}

}
