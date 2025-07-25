# JMUDVaultMesh
A Java implementation to access the MUDVault Mesh Intermud

```java
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
		
        // Implement a listener
		MUDVaultListener callback = new MUDVaultListener() {
			@Override
			public void meshStateChanged(MeshConnectionState state) {
			}
			@Override
			public void meshReceivedWhoList(String fromMUD, List<PlayerInfo> users) {
				System.out.println("Received Who list");
				users.forEach(user -> System.out.println(user));
			}
			@Override
			public List<PlayerInfo> meshOnWho(String mud, String player) {
				return List.of();
			}
			@Override
			public void meshOnTell(String fromMud, String fromPlayer, String toPlayer, String message) {
			}
			@Override
			public void meshOnChannelMessage(String fromMud, String fromPlayer, String channel, String message) {
			}
			public void meshReceivedFingerReply(String fromMUD, String fromUser, String toUser, String info) {}
			public String meshOnFinger(String fromMud, String fromPlayer, String player) {
				return "Hands off. Stop fingering me";
			}
		};
		
		MUDVaultMesh client = new MUDVaultMesh(config, callback);
		client.connect();
        // Initial connection takes a moment
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        // Request a who list - and get the result in the listener above
		client.requestWhoList("Dark Wizardry", "taranion");
		// Send a message on the intermud gossip as user "taranion"
		client.sendOnChannel("gossip", "taranion", "I hope this works");
	}

}

```

