package org.prelle.mudvault;

import com.google.gson.annotations.SerializedName;

/**
 * 
 */
public enum MessageType {
	
	@SerializedName("auth")
	AUTH,
	@SerializedName("tell")
	TELL,
	@SerializedName("channel")
	CHANNEL,
	@SerializedName("who")
	WHO,
	@SerializedName("who_reply")
	WHO_REPLY,
	@SerializedName("finger")
	FINGER,
	@SerializedName("finger_reply")
	FINGER_REPLY,
	@SerializedName("error")
	ERROR,
	@SerializedName("ping")
	PING,
	@SerializedName("pong")
	PONG

}
