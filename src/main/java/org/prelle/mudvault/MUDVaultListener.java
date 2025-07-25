package org.prelle.mudvault;

import java.util.List;

/**
 * 
 */
public interface MUDVaultListener {
	
	public void meshStateChanged(MeshConnectionState state);
	
	public void meshReceivedWhoList(String fromMUD, List<PlayerInfo> users);

	public void meshOnTell(String fromMud, String fromPlayer, String toPlayer, String message);

	public void meshOnChannelMessage(String fromMud, String fromPlayer, String channel, String message);
	
	public List<PlayerInfo> meshOnWho(String mud, String player);
	
}
