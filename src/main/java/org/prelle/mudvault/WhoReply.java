package org.prelle.mudvault;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * 
 */
public class WhoReply extends MeshMessage {
	
	public static record WhoPayload(List<PlayerInfo> users) {
	}
	
	@Getter
	private WhoPayload payload = new WhoPayload(new ArrayList<>());

	//-------------------------------------------------------------------
	public WhoReply() {
		type=MessageType.WHO_REPLY;
	}

}
