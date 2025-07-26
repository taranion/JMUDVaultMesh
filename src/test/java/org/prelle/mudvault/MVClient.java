package org.prelle.mudvault;

import java.net.URISyntaxException;
import java.util.List;

/**
 * 
 */
public class MVClient {

	//-------------------------------------------------------------------
	/**
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {
		if (args.length!=2) {
			System.err.println("Call with <mudname> <apikey>");
			System.exit(-1);
		}
		VaultMeshConfig config = VaultMeshConfig.builder()
				.mudName(args[0])
				.apiKey(args[1])
				.build();
		
		MUDVaultListener callback = new MUDVaultListener() {
			@Override
			public void meshStateChanged(MeshConnectionState state) {
			}
			@Override
			public void meshReceivedWhoList(String fromMUD, PlayerInfo[] users) {
				System.out.println("Received Who list");
//				users.forEach(user -> System.out.println(user));
			}
			@Override
			public PlayerInfo[] meshOnWho(String mud, String player) {
				PlayerInfo player1 = new PlayerInfo();
				player1.setUsername("testuser");
				player1.setDisplayName("Test User");
				player1.setLocation("Doesn't exist");
				return new PlayerInfo[] {player1};
			}
			@Override
			public void meshOnTell(String fromMud, String fromPlayer, String toPlayer, String message) {
			}
			@Override
			public void meshOnChannelMessage(String fromMud, String fromPlayer, String channel, String message) {
			}
			public void meshReceivedFingerReply(String fromMUD, String fromUser, String toUser, String info) {}
			public PlayerInfo meshOnFinger(String fromMud, String fromPlayer, String player) {
				PlayerInfo info = new PlayerInfo();
				info.setUsername(player);
				info.setTitle("Hands off. Stop fingering me");
				return info;
			}
		};
		
		MUDVaultMesh client = new MUDVaultMesh(config, callback);
		client.connect();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//client.requestWhoList("Gateway", "taranion");
		client.requestMUDs();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		//client.joinChannel("gossip");
		client.sendOnChannel("ooc", "GithubAction", "Unit Test");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Exit now");
	}

}
