package org.prelle.mudvault;

import lombok.Getter;

/**
 * 
 */
public class Channel extends MeshMessage {
	
	public static record ChannePayload(String message) {
	}
	
	@Getter
	private ChannePayload payload;

	//-------------------------------------------------------------------
	public Channel() {
		type=MessageType.CHANNEL;
	}

}
