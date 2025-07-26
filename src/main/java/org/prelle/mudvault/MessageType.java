package org.prelle.mudvault;

import com.google.gson.annotations.SerializedName;

/**
 * 
 */
public enum MessageType {
	
	@SerializedName("auth")     AUTH,
	@SerializedName("tell")	    TELL,
	@SerializedName("channel")  CHANNEL,
	@SerializedName("channels") CHANNELS,
	@SerializedName("who")    	WHO,
	@SerializedName("emoteto")  EMOTE_TO,
	@SerializedName("finger")   FINGER,
	@SerializedName("error")   	ERROR,
	@SerializedName("mudlist") 	MUDLIST,
	@SerializedName("ping")   	PING,
	@SerializedName("pong")     PONG

}
