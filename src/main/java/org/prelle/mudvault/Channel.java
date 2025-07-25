package org.prelle.mudvault;

import org.prelle.mudvault.Tell.TellPayload;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class Channel extends MeshMessage {
	
	public static record ChannelPayload(String message) {
	}
	
	@Getter
	@Setter
	private ChannelPayload payload = new ChannelPayload(null);

	//-------------------------------------------------------------------
	public Channel() {
		type=MessageType.CHANNEL;
	}

}
