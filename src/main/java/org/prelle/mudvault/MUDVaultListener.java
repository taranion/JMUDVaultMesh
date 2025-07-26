package org.prelle.mudvault;

import java.util.List;

/**
 * 
 */
public interface MUDVaultListener {
	
	public void meshStateChanged(MeshConnectionState state);
	
	public void meshReceivedWhoList(String fromMUD, PlayerInfo[] users);
	
	public void meshReceivedFingerReply(String fromMUD, String fromUser, String toUser, String info);

	public void meshOnTell(String fromMud, String fromPlayer, String toPlayer, String message);

	public void meshOnChannelMessage(String fromMud, String fromPlayer, String channel, String message);
	
	public PlayerInfo[] meshOnWho(String mud, String player);
	
	public PlayerInfo meshOnFinger(String fromMud, String fromPlayer, String player);
	
}
