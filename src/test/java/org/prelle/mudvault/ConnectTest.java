package org.prelle.mudvault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ConnectTest {

	private static MUDVaultListener callback = new MUDVaultListener() {
		public void meshStateChanged(MeshConnectionState state) {
			ConnectTest.state = state;
		}
		public void meshReceivedWhoList(String fromMUD, PlayerInfo[] users) {}
		public PlayerInfo[] meshOnWho(String mud, String player) {return null;}
		public void meshOnTell(String fromMud, String fromPlayer, String toPlayer, String message) {}
		public void meshOnChannelMessage(String fromMud, String fromPlayer, String channel, String message) {}
		public void meshReceivedFingerReply(String fromMUD, String fromUser, String toUser, String info) {}
		public PlayerInfo meshOnFinger(String fromMud, String fromPlayer, String player) {
			PlayerInfo info = new PlayerInfo();
			info.setUsername(player);
			info.setTitle("Hands off. Stop fingering me");
			return info;
		}
	};

	
	private String apiKey;
	private String mudName;
	private static MeshConnectionState state;
	
	@Before
	public void readCredentials() {
		apiKey = System.getenv("TEST_API_KEY");
		mudName = System.getenv("TEST_MUDNAME");
		assertNotNull("TEST_API_KEY not set", apiKey);
		assertNotNull("TEST_MUDNAME not set", mudName);
	}
	
	@Test
	public void test() throws URISyntaxException {
		assertNotNull("TEST_API_KEY not set", apiKey);
		assertNotNull("TEST_MUDNAME not set", mudName);
		System.out.println("Use "+apiKey.length()+" long key "+apiKey);
		System.out.println("Env "+System.getenv().toString());
		VaultMeshConfig config = VaultMeshConfig.builder()
				.mudName(mudName)
				.apiKey(apiKey)
				.build();
		MUDVaultMesh client = new MUDVaultMesh(config, callback);
		client.connect();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("Failed to authenticate",MeshConnectionState.CONNECTED, state);
	}

}
