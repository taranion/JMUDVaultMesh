package org.prelle.mudvault;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class FingerReply extends MeshMessage {
	
	public static record FingerReplyPayload(String player, String info) {
	}
	
	@Getter
	@Setter
	private FingerReplyPayload payload = new FingerReplyPayload(null,null);

	//-------------------------------------------------------------------
	public FingerReply() {
		type=MessageType.FINGER_REPLY;
	}

}
