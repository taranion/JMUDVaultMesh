package org.prelle.mudvault;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class Channel extends MeshMessage {
	
	public static enum ChannelAction {
		@SerializedName("message")
		MESSAGE,
		@SerializedName("join")
		JOIN,
		@SerializedName("leave")
		LEAVE
	}
	
	public static record ChannelPayload(String channel, String message, ChannelAction action, String formatted) {
		public ChannelPayload(String channel, String message) {
			this(channel,message,ChannelAction.MESSAGE,null);
		}
	}
	
	@Getter
	@Setter
	private ChannelPayload payload = new ChannelPayload(null,null);

	//-------------------------------------------------------------------
	public Channel() {
		type=MessageType.CHANNEL;
	}

}
