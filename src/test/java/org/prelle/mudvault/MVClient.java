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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void meshReceivedWhoList(String fromMUD, List<PlayerInfo> users) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public List<PlayerInfo> meshOnWho(String mud, String player) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void meshOnTell(String fromMud, String fromPlayer, String toPlayer, String message) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void meshOnChannelMessage(String fromMud, String fromPlayer, String channel, String message) {
				// TODO Auto-generated method stub
				
			}
		};
		
		MUDVaultMesh client = new MUDVaultMesh(config, callback);
		client.connect();	}

}
